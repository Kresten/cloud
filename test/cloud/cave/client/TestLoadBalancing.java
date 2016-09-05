package cloud.cave.client;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.json.simple.JSONObject;
import org.junit.*;

import cloud.cave.broker.*;
import cloud.cave.common.Inspector;
import cloud.cave.config.*;
import cloud.cave.domain.*;
import cloud.cave.doubles.*;
import cloud.cave.server.*;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.*;

/**
 * Test scalability of servers, what happens when a request hits a server that
 * has never handled any previous requests from the player before, because the
 * session was initialized on some other (load balanced) server.
 * <p>
 * To scale from one server to several handling load, each server has to handle
 * session state in some well defined manner across the entire server cluster.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class TestLoadBalancing {
  

  private Player player;
  private LoadBalancedLocalMethodCallClientRequestHandler crh;
  private CaveProxy caveProxy;

  @Before
  public void setup() {
    // In a multi server scenario that facilitate testing a
    // 'Session Database' handling of session data, we of course
    // must have two servers that access the same underlying
    // storage system. Thus we create one FakeStorage, and
    // let the two servers both share that.
    CaveStorage storage = new FakeCaveStorage();
    storage.initialize(null, null);
    
    // In a multi server scenario, each server creates its own
    // cave object during initialization; we mimic the multi server
    // configuration by creating two caves, assigned to different
    // servers but with the same underlying storage
    CaveServerFactory factory = new FactoryWithSharedStorage(storage);
    ObjectManager objMgr = new StandardObjectManager(factory);

    Cave caveServer1 = new CaveServant(objMgr);
    Cave caveServer2 = new CaveServant(objMgr);

    // Create the server side invokers
    Invoker srh1 = new StandardInvoker(objMgr);
    Invoker srh2 = new StandardInvoker(objMgr);
    
    // and here comes the trick; we create a client side
    // requester that takes the two server invokers as parameters, and
    // provides a method for setting which one the
    // next requests will be forwarded to.
    crh = new LoadBalancedLocalMethodCallClientRequestHandler(srh1,srh2);
    
    // Create the cave proxy, and login mikkel; server communication
    // will be made with server 1 as this is the default for the
    // load balancing requester.
    caveProxy = new CaveProxy(crh);
    Login loginResult = caveProxy.login( "mikkel_aarskort", "123");

    player = loginResult.getPlayer();
  }

  @Test
  public void shouldProveThatOurServerIsStatefulAndThusDoesNotScale() {
    // Verify that requests are forwarded to server 1
    assertThat(crh.toString(), containsString("server 1"));
    // Verify that method calls runs smoothly to the server
    assertThat(player.getShortRoomDescription(), containsString("brick building"));
    
    // Move Mikkel three times where we simulate that all client-server
    // requests hit server 1, and thus the session cache has noted
    // the traveled route: east, west, west
    assertThat(player.move(Direction.EAST), is(true));
    assertThat(player.move(Direction.WEST), is(true));
    assertThat(player.move(Direction.WEST), is(true));
    
    // now we simulate that the next request will be forwarded to server 2
    // by the load balancing of the SkyCave system
    crh.setWhichServerToUse(2);
    // Verify that requests are forwarded to server 2
    assertThat(crh.toString(), containsString("server 2"));
    
    // Verify that the shit hits the fan now as we try
    // to backtrack but server 2's cache has no idea of
    // any stack of backtracking movements
    try {
      // This next test will actually pass if a
      // really stupid player session cache implementation is made
      // which just creates a new player object upon
      // each call to PlayerSessionCache.get(playerID);
      // This is because all data related to player can
      // actually be recreated in each server without
      // any problems.
      String d = player.getLongRoomDescription();
      assertThat(d, containsString("[0] Mikkel"));
      
      // But the following will NOT, as the stack of
      // visited positions during the current session
      // is NOT information a single server in a cluster
      // can compute, only the cluster of servers knows that
      boolean canBacktrack;
      
      // backtrack from last WEST move, back to entry room
      canBacktrack = player.backtrack();
      assertThat(canBacktrack, is(true));
      assertThat(player.getPosition(), is("(0,0,0)"));
      
    } catch(NullPointerException exc) {
      // In a proper scalable implementation, session state
      // must be available across all servers. This is a
      // future exercise. Enable the fail below to demonstrate
      // the problem
      
      // TODO: fail("The server is stateful which disallows scaling!");
    }
  }
}

/** A factory that creates test doubles but in case of the
 * storage, returns the SAME storage to allow different caves
 * access to the same underlying storage.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
class FactoryWithSharedStorage implements CaveServerFactory {

  private CaveStorage storage;

  public FactoryWithSharedStorage(CaveStorage storage) {
    this.storage = storage;
  }

  @Override
  public CaveStorage createCaveStorageConnector(ObjectManager objMgr) {
    // Return the SAME for both caves
    return storage;
  }

  @Override
  public SubscriptionService createSubscriptionServiceConnector(ObjectManager objMgr) {
    return new TestStubSubscriptionService();
  }

  @Override
  public WeatherService createWeatherServiceConnector(ObjectManager objectManager) {
    return new TestStubWeatherService();
  }

  @Override
  public ServerRequestHandler createServerRequestHandler(ObjectManager objMgr) {
    // Not used...
    return new NullServerRequestHandler();
  }

  @Override
  public PlayerSessionCache createPlayerSessionCache(ObjectManager objMgr) {
    return new SimpleInMemoryCache();
  }

  @Override
  public Inspector createInspector(ObjectManager objMgr) {
    return new NullInspector();
  }

}


/**
 * A test double request handler which simulates load balancing
 * requests over two servers, by allowing to choose which
 * of two request handlers to forward the next requests to.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
class LoadBalancedLocalMethodCallClientRequestHandler implements ClientRequestHandler {
  private Invoker requesterServer1;
  private Invoker requesterServer2;
  
  private int whichOne;
  

  public LoadBalancedLocalMethodCallClientRequestHandler(
      Invoker srh1, Invoker srh2) {
    this.requesterServer1 = srh1;
    this.requesterServer2 = srh2;
    this.whichOne = 1;
  }

  public void setWhichServerToUse(int no) {
    assert no == 1 || no == 2;
    whichOne = no;
  }
  
  @Override
  public void initialize(ServerConfiguration config) {
    // Not relevant, as this request handler is only used in
    // testing and under programmatic control
  }
  
  @Override
  public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson) {
    Invoker requestHandler = null;
    if (whichOne==1) requestHandler = requesterServer1; else requestHandler = requesterServer2;
    // System.out.println("--> CRH("+whichOne+"): "+ requestJson);
    JSONObject reply = requestHandler.handleRequest(requestJson);
    // System.out.println("<-- CRH("+whichOne+"): "+ reply);
    return reply;
  }

  @Override
  public String toString() {
    return "LoadBalancedLocalMethodCallClientRequestHandler, dispatching to server "+whichOne;
  }
}