package cloud.cave.command;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.json.simple.*;

import cloud.cave.broker.*;
import cloud.cave.domain.*;

public class Common {

  public static void shouldExecuteHomeCommand(Player player) {
    // move north
    player.move(Direction.NORTH);
    String pos;
    String desc = player.getShortRoomDescription();
    
    // validate new position
    pos = player.getPosition();
    assertThat(pos, is("(0,1,0)"));
    
    // execute dynamic command 'home'
    JSONObject result = player.execute("HomeCommand", "null");
    assertNotNull("The execute did not return a reply which it must do.", result);
    boolean isError = Boolean.parseBoolean(result.get(MarshalingKeys.ERROR_CODE_KEY).toString());
    assertFalse("The home command ought to be executed correctly", isError);
    
    // and validate home behaviour = position is reset to 0,0,0
    pos = player.getPosition();
    assertThat(pos, is("(0,0,0)"));
    
    // and the room description is not identical to the old one
    assertThat(player.getShortRoomDescription(), not(is(desc)));
    
    // and that it is the 'You are standing...'
    assertThat(player.getShortRoomDescription(), containsString("You are standing at the end of a road"));
  }

  public static void shouldExecuteJumpCommand(Player player) {
    String pos;
    // validate current position
    pos = player.getPosition();
    assertThat(pos, is("(0,0,0)"));
    
    // execute dynamic command 'jump'
    JSONObject result = player.execute("JumpCommand", "(0,1,0)");
    assertNotNull("The execute did not return a reply which it must do.", result);
    
    // verify that the jump method was executed
    boolean isError = Boolean.parseBoolean(result.get(MarshalingKeys.ERROR_CODE_KEY).toString());
    assertFalse("The jump command executed correctly", isError);

    // and verify that the result of the jump was also successful
    boolean isSuccess = Boolean.parseBoolean(result.get(MarshalingKeys.RETURNVALUE_HEAD_KEY).toString());
    assertTrue("The jump to 0,1,0 should be allowed", isSuccess);
    
    // validate new position is also updated!
    pos = player.getPosition();
    assertThat(pos, is("(0,1,0)"));
    // and that indeed the description is of (0,1,0)
    assertThat(player.getShortRoomDescription(), containsString("open forest, with a deep valley"));
    
    // Validate that a jump to unknown locations is caught
    result = player.execute("JumpCommand", "(700,10,-42)");
    assertNotNull("The execute did not return a reply which it must do.", result);
    
    // the execution should work correctly
    isError = Boolean.parseBoolean(result.get(MarshalingKeys.ERROR_CODE_KEY).toString());
    assertFalse("The jump command executed correctly", isError);

    // but the command should returned an error as the jump was illegal
    // System.out.println(" Reply is: "+result);
    
    isSuccess = Boolean.parseBoolean(result.get(MarshalingKeys.RETURNVALUE_HEAD_KEY).toString());
    assertFalse("The jump to 700,10,-42 should NOT be allowed", isSuccess);
    
    JSONArray tail = (JSONArray)result.get(MarshalingKeys.RETURNVALUE_TAIL_KEY);
    
    String errMsg = tail.get(0).toString();
    assertThat(errMsg,
        is("JumpCommand failed, room (700,10,-42) does not exist in the cave."));
  }

  public static void shouldExecuteUnknownCommand(Player player) {
    JSONObject result = player.execute("BimseCommand", "really has not clue here", "more nonsense");
    assertNotNull("The execute did not return a reply which it must do.", result);
    
    String errorCode = result.get(MarshalingKeys.ERROR_CODE_KEY).toString();
    assertThat(errorCode, is(StatusCode.SERVER_FAILED_TO_LOAD_COMMAND));
    
    String errMsg = result.get(MarshalingKeys.ERROR_MSG_KEY).toString();
    assertThat(errMsg,
        is("Player.execute failed to load Command class: BimseCommand"));
  }

}
