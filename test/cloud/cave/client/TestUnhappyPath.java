package cloud.cave.client;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.*;

import cloud.cave.broker.*;
import cloud.cave.client.CaveProxy;
import cloud.cave.common.CommonCaveTests;
import cloud.cave.config.*;
import cloud.cave.domain.*;
import cloud.cave.doubles.*;
import cloud.cave.server.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;

/**
 * Testing unhappy paths, ie. scenarios where there are network problems.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class TestUnhappyPath {

  private Cave cave;
  private SaboteurCRHDecorator saboteur;

  @Before
  public void setup() {
    // Create the server tier
    ObjectManager om = CommonCaveTests.createTestDoubledConfiguredCave();

    Invoker srh = new StandardInvoker(om);

    ClientRequestHandler properCrh = new LocalMethodCallClientRequestHandler(srh);
    
    // Decorate the proper CRH with one that simulate errors, i.e. a Saboteur
    saboteur = new SaboteurCRHDecorator(properCrh);

    cave = new CaveProxy(saboteur);
  }
  
  @Test
  public void shouldThrowIPCExceptionForTimeOut() {
    // One player
    Login loginResult = cave.login( "mikkel_aarskort", "123");

    Player p = loginResult.getPlayer();
    assertThat(p.getName(), is("Mikkel"));
    
    boolean isValid = p.move(Direction.NORTH);
    assertThat(isValid, is(true));
    
    // Tell the saboteur to throw exception on next IPC
    saboteur.throwNextTime("Could Not Connect to server");
    
    // And check that the exception is propagated all the way
    // to the top level so a client user interface can do
    // something that makes sense to the user...
    try {
      isValid = p.move(Direction.SOUTH);
      fail("p.move should have thrown an CaveIPCException, but did not.");
    } catch (CaveIPCException e) {
      // Correct...
    }
  }
  
  // Make the server unstable internally
  @Test
  public void shouldReportOnTimeoutErrorOnSubscriptionService() {

    CaveServerFactory factory = new AllTestDoubleFactory() {
    
      public SubscriptionService createSubscriptionServiceConnector(ObjectManager objMgr) {

        // Heavy setup to introduce errors on the server side using
        // a Meszaros Saboteur
        SubscriptionService saboteurSubscriptionService = new SubscriptionService() {
          @Override
          public SubscriptionRecord lookup(String loginName, String password) {
            throw new CaveIPCException("SubscriptionService: Timeout in connecting to the service", null);
          }
          @Override
          public void initialize(ObjectManager objMgr, ServerConfiguration config) {
          }
          @Override
          public ServerConfiguration getConfiguration() {
            return null;
          }
          @Override
          public void disconnect() {
          }
        };
    
        return saboteurSubscriptionService;
      }
    };
    ObjectManager objMgr = new StandardObjectManager(factory);
    cave = new CaveServant(objMgr);
    
    // Try out the login, will result in a internal server error as
    // the connection to the subscription fails
    Login loginResult = cave.login( "mathilde_aarskort", "321");
    Player p2 = loginResult.getPlayer();
    assertNull(p2);
    assertThat(loginResult.getResultCode(), is(LoginResult.LOGIN_FAILED_SERVER_ERROR));
  }


}
