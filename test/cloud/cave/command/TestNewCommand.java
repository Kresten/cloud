package cloud.cave.command;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.json.simple.JSONObject;
import org.junit.*;

import cloud.cave.common.*;
import cloud.cave.config.*;
import cloud.cave.domain.*;
import cloud.cave.doubles.AllTestDoubleFactory;
import cloud.cave.server.SimpleInspector;

/** TDD the Command pattern.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class TestNewCommand {

  private Player player;
  
  @Before
  public void setup() {
    Cave cave;
    CaveServerFactory factory = new AllTestDoubleFactory() {
      // Turn on the simple inspector
      @Override
      public Inspector createInspector(ObjectManager objMgr) {
        return new SimpleInspector();
      };
    };
    ObjectManager objMgr = new StandardObjectManager(factory);
    cave = objMgr.getCave();

    Login loginResult = cave.login( "mikkel_aarskort", "123");
    player = loginResult.getPlayer();
  }

  @Test
  public void shouldExecuteHomeCommand() {
    Common.shouldExecuteHomeCommand(player);
  }

  @Test
  public void shouldExecuteJumpCommand() {
    Common.shouldExecuteJumpCommand(player);
  }

  @Test
  public void shouldNotExecuteUnknownCommand() {
    Common.shouldExecuteUnknownCommand(player);
  }
  
  // E2016 extension is the 'back' feature - ensure
  // that you can back over home and jump commands
  @Test
  public void shouldHandleBacktrackingOverHomeCommands() {
    // First, move n, s, e, w, w to push a trace into the session cache
    player.move(Direction.NORTH);
    player.move(Direction.SOUTH);
    player.move(Direction.EAST);
    String p2 = player.getPosition();
    player.move(Direction.WEST);
    String p1 = player.getPosition();
    player.move(Direction.WEST);
    String p0 = player.getPosition();
    
    // Move home using home command
    JSONObject result = player.execute("HomeCommand", "null");
    String pos = player.getPosition();
    assertThat(pos, is("(0,0,0)"));
    
    // Now, back-tracking should move back to p0, p1, p2
    boolean canDo;

    // p0
    canDo = player.backtrack();
    assertThat(canDo, is(true));
    assertThat(player.getPosition(), is(p0));

    // p1
    canDo = player.backtrack();
    assertThat(canDo, is(true));
    assertThat(player.getPosition(), is(p1));

    // p2
    canDo = player.backtrack();
    assertThat(canDo, is(true));
    assertThat(player.getPosition(), is(p2));
  }
  
  @Test
  public void shouldHandleBacktrackingOverJumpCommands() {
    // First, move n, s, e, w, w to push a trace into the session cache
    player.move(Direction.NORTH);
    player.move(Direction.SOUTH);
    player.move(Direction.EAST);
    String p2 = player.getPosition();
    player.move(Direction.WEST);
    String p1 = player.getPosition();
    player.move(Direction.WEST);
    String p0 = player.getPosition();
    
    // Move home using Jump command
    JSONObject result = player.execute("JumpCommand", "(0,0,0)");
    assertNotNull("The execute did not return a reply which it must do.", result);

    // Now, back-tracking should move back to p0, p1, p2
    boolean canDo;

    // p0
    canDo = player.backtrack();
    assertThat(canDo, is(true));
    assertThat(player.getPosition(), is(p0));

    // p1
    canDo = player.backtrack();
    assertThat(canDo, is(true));
    assertThat(player.getPosition(), is(p1));

    // p2
    canDo = player.backtrack();
    assertThat(canDo, is(true));
    assertThat(player.getPosition(), is(p2));
  }
}
