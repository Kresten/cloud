package cloud.cave.client;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.*;
import static org.hamcrest.CoreMatchers.*;

import cloud.cave.broker.*;
import cloud.cave.common.*;
import cloud.cave.config.*;
import cloud.cave.domain.*;
import cloud.cave.doubles.*;
import cloud.cave.extension.InspectCommand;
import cloud.cave.server.SimpleInspector;

/**
 * TDD of the Inspector usage for IPC and CFG in SkyCave. Central information,
 * such as the request and reply message passing through the Broker into the
 * SkyCave are logged by the Inspector - these tests demonstrate it.
 * 
 * @author Henrik Baerbak Christensen, Computer Science, Aarhus University
 *
 */
public class TestInspector {

  private Cave cave;
  private Player player;

  @Before
  public void setup() {
    CaveServerFactory factory = new AllTestDoubleFactory() {
      // Turn on the simple inspector
      @Override
      public Inspector createInspector(ObjectManager objMgr) {
        return new SimpleInspector();
      };
    };
    ObjectManager objMgr = new StandardObjectManager(factory);
    
    ClientRequestHandler crh = new LocalMethodCallClientRequestHandler(objMgr.getInvoker());
    
    // Create the cave proxy, and login mikkel
    cave = new CaveProxy(crh);
    Login loginResult = cave.login( "mikkel_aarskort", "123");
    
    player = loginResult.getPlayer();
  }

  @Test
  public void shouldDescribeCaveConfigurationThroughInspector() {
    // Ask player to read the CFG topic of the inspector
    JSONObject reply = player.execute(InspectCommand.INSPECT_COMMAND_CLASSNAME, 
        InspectCommand.READ, Inspector.CFG_TOPIC);

    List<String> cfgLog;
    cfgLog = extractReplyTailAsList(reply);
    
    assertThat(cfgLog, hasItem("  CaveStorage: cloud.cave.doubles.FakeCaveStorage"));
    assertThat(cfgLog, hasItem("  SubscriptionService: cloud.cave.doubles.TestStubSubscriptionService"));
    assertThat(cfgLog, hasItem("  WeatherService: cloud.cave.doubles.TestStubWeatherService"));
    assertThat(cfgLog, hasItem("  PlayerSessionCache: cloud.cave.server.SimpleInMemoryCache"));
    assertThat(cfgLog, hasItem("  ServerRequestHandler: cloud.cave.doubles.NullServerRequestHandler"));
    assertThat(cfgLog, hasItem("   - cfg: null"));
  }

  /** The inspector is operated solely through a Command pattern to keep the
   * cmd interface small. Therefore the following tests verify the inspector on
   * the cave through the InspectCommand.
   */
  @Test
  public void shouldValidateInspectorCommand() {
    JSONObject reply;

    // Write to topic exercise1
    reply = player.execute(InspectCommand.INSPECT_COMMAND_CLASSNAME, 
        InspectCommand.WRITE, "exercise1", "---seperator added---");
    assertThat(reply.get(MarshalingKeys.RETURNVALUE_HEAD_KEY).toString(), 
        containsString("Wrote on topic exercise1"));
    
    reply = player.execute(InspectCommand.INSPECT_COMMAND_CLASSNAME, 
        InspectCommand.WRITE, "exercise1", "Useful information here");
    
    // Try reading back again
    reply = player.execute(InspectCommand.INSPECT_COMMAND_CLASSNAME, 
        InspectCommand.READ, "exercise1");
    String innerReply = reply.get(MarshalingKeys.RETURNVALUE_HEAD_KEY).toString(); 
    
    assertThat(innerReply, is("Inspector log on topic exercise1 is:"));
    
    JSONArray returnValueArray = (JSONArray) reply.get(MarshalingKeys.RETURNVALUE_TAIL_KEY);

    String item1 = returnValueArray.get(0).toString();
    String item2 = returnValueArray.get(1).toString();
    
    assertThat(item1, is("---seperator added---"));
    assertThat(item2, is("Useful information here"));

    // and read is idempotent
    reply = player.execute(InspectCommand.INSPECT_COMMAND_CLASSNAME, 
        InspectCommand.READ, "exercise1");
    returnValueArray = (JSONArray) reply.get(MarshalingKeys.RETURNVALUE_TAIL_KEY);
    assertThat(returnValueArray.size(), is(2));
    
    // System.out.println("---> "+reply);

    // try resetting inspection log
    reply = player.execute(InspectCommand.INSPECT_COMMAND_CLASSNAME, 
        InspectCommand.RESET, "exercise1");
    assertThat(reply.get(MarshalingKeys.RETURNVALUE_HEAD_KEY).toString(), 
        containsString("Reset log on topic exercise1"));

    // Empty log then on read
    reply = player.execute(InspectCommand.INSPECT_COMMAND_CLASSNAME, 
        InspectCommand.READ, "exercise1");
    returnValueArray = (JSONArray) reply.get(MarshalingKeys.RETURNVALUE_TAIL_KEY);
    assertThat(returnValueArray.size(), is(0));

  }

  /** The server side broker abstractions track request/reply
   * objects passing through the Invoker to allow deep inspection
   * of message traffic.
   */
  @Test
  public void shouldValidateInspectorIPCTracking() {
    // Reset the IPC log to avoid all previous 'gossip'
    JSONObject reply = player.execute(InspectCommand.INSPECT_COMMAND_CLASSNAME, 
        InspectCommand.RESET, Inspector.IPC_TOPIC);

    // Try a few commands
    player.getShortRoomDescription();
    player.getPlayersHere();
    
    // And get the IPC log
    reply = player.execute(InspectCommand.INSPECT_COMMAND_CLASSNAME, 
        InspectCommand.READ, Inspector.IPC_TOPIC);

    // And validate it...
    List<String> cfgLog;
    cfgLog = extractReplyTailAsList(reply);

    // cfgLog.stream().forEach(System.out::println);

    assertThat(cfgLog.size(), is(6));
    assertThat(cfgLog.get(1), containsString("[REQUEST] {\"method\":\"player-get-short-room-description\","));
    assertThat(cfgLog.get(2), containsString("\"reply\":\"You are standing at the end of a road before"));
    assertThat(cfgLog.get(3), containsString("[REQUEST] {\"method\":\"player-get-players-here\","));
    assertThat(cfgLog.get(4), containsString("\"reply-tail\":[\"Mikkel\"],"));
  }
  
  @Test
  public void shouldValidateInspectorCommandOnUnhappyPath() {
    // Try an unknown subcommand
    JSONObject reply = player.execute(InspectCommand.INSPECT_COMMAND_CLASSNAME, 
        "bimse-subcommand", Inspector.IPC_TOPIC);
    
    // System.out.println("--> "+reply);
    
    assertThat(reply.get(MarshalingKeys.ERROR_CODE_KEY), is(StatusCode.SERVER_FAILED_TO_INSTANTIATE_COMMAND));
    assertThat(reply.get(MarshalingKeys.ERROR_MSG_KEY), is("The subcommand 'bimse-subcommand' is not known."));
  }

  @SuppressWarnings("unchecked")
  private List<String> extractReplyTailAsList(JSONObject reply) {
    List<String> cfgLog;
    // Extract the log from the reply tail of the returned Json object
    JSONArray jsonarray =  (JSONArray) reply.get("reply-tail");
    assertThat(jsonarray, notNullValue());

    // Have to convert the json array into list of string to use JUnit matchers
    cfgLog = new ArrayList<>( jsonarray.size());
    jsonarray.stream().forEach( obj -> { cfgLog.add(obj.toString()); } );
    return cfgLog;
  }

}
