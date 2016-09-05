package cloud.cave.client;

import java.io.*;

import org.json.simple.*;

import cloud.cave.broker.*;
import cloud.cave.common.PlayerSessionExpiredException;
import cloud.cave.domain.*;

/**
 * The client interpreter, implementing a classic shell based read-eval-loop
 * command line tool to log a player into the cave and allow him/her to explore
 * it.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class CmdInterpreter {

  private Cave cave;
  private Player player;
  
  private PrintStream systemOut;
  private InputStream systemIn;

  /**
   * Construct the interpreter.
   * 
   * @param cave
   *          the cave to log into
   * @param loginName
   *          the loginName of the player
   * @param pwd
   *          the password of the player
   * @param systemOut
   *          the print stream that acts as shell output
   * @param systemIn
   *          the input stream that act as keyboard input
   */
  public CmdInterpreter(Cave cave, String loginName, String pwd, PrintStream systemOut, InputStream systemIn) {
    this.cave = cave;

    this.systemOut = systemOut;
    this.systemIn = systemIn;
    
    Login loginResult = null;

    systemOut.println("Trying to log in player with loginName: " + loginName);

    loginResult = cave.login(loginName, pwd);

    boolean success = LoginResult.isValidLogin(loginResult.getResultCode());

    if (!success) {
      systemOut.println("*** SORRY! The login failed. Reason: "
          + loginResult.getResultCode());
      System.exit(-1);
    }
    if (loginResult.getResultCode() == LoginResult.LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN) {
      systemOut.println("*** WARNING! User '"
          + loginResult.getPlayer().getName() + "' is ALREADY logged in! ***");
      systemOut
          .println("*** The previous session will be disconnected. ***");
    }
    player = loginResult.getPlayer();
  }

  /**
   * The classic 'read command, evaluate command, loop' of a shell. Issue your
   * command, and see the result in the shell.
   */
  public void readEvalLoop() {
    systemOut.println("\n== Welcome to SkyCave, player " + player.getName()
        + " ==");

    BufferedReader bf = new BufferedReader(new InputStreamReader(systemIn));

    systemOut
        .println("Entering command loop, type \"q\" to quit, \"h\" for help.");

    // and enter the command processing loop
    String line;
    try {
      do {
        line = bf.readLine();
        if (line.length() > 0) {
          // split into into tokens on whitespace
          String[] tokens = line.split("\\s");

          // First handle the 'short hand' notation for movement
          if (tokens[0].length() == 1) {
            char primaryCommand = line.charAt(0);
            handleSingleCharCommand(primaryCommand);

          } else {
            handleMultipleCharCommand(tokens[0], tokens);
          }
          systemOut.println();
        }
      } while (!line.equals("q"));
    } catch (PlayerSessionExpiredException exc) {
      systemOut
          .println("**** Sorry! Another session has started with the same loginID. ***");
      systemOut
          .println("**** You have been logged out.                                 ***");
      System.exit(0);
    } catch (IOException e) {
      systemOut.println("Exception caught: " + e);
    }
    systemOut.println("Leaving SkyCave - Goodbye.");
  }

  /**
   * Interpret and execute a command.
   * 
   * @param command potential command to execute
   * @param tokens arguments split into token array
   */
  private void handleMultipleCharCommand(String command, String[] tokens) {
    if (command.equals("dig") && tokens.length > 2) {
      Direction direction = getDirectionFromChar(tokens[1].charAt(0));
      // Compile the room description by putting the tokens back into a single string again
      String roomDescription = "";
      roomDescription = mergeTokens(tokens, 2);
      
      boolean isValid = player.digRoom(direction, roomDescription);
      if (isValid) {
        systemOut.println("You dug a new room in direction " + direction);
      } else {
        systemOut
            .println("You cannot dig there as there is already a room in direction "
                + direction);
      }

    } else if (command.equals("who")) {
      systemOut.println("You are: " + player.getName()+ "/"+ player.getID()+ " in Region "+player.getRegion());
      systemOut.println("   in local session: " + player.getSessionID());

    } else if (command.equals("weather")) {
      String weather = player.getWeather();
      systemOut.println("The weather at: " + player.getRegion());
      systemOut.println(weather);
      
    } else if (command.equals("back")) {
      boolean isBacktracked = player.backtrack();
      if (isBacktracked) {
        systemOut.println("You backtracked to position " + player.getPosition());
        systemOut.println(player.getShortRoomDescription());
      } else {
        systemOut.println("You cannot backtrack - you are back where you started in this session");
      }

    } else if (command.equals("post") && tokens.length > 1) {
      systemOut.println("POST awaits implementation");

    } else if (command.equals("read")) {
      systemOut.println("READ awaits implementation");

    } else if (command.equals("sys")) {
      systemOut.println("System information:");
      systemOut.println(cave.describeConfiguration());
      systemOut.println(player.toString());
    
    } else if (command.equals("exec")) {
      if (tokens.length > 2) {
        JSONObject response = null;
        // Create the parameter array
        String[] parameters = new String[tokens.length - 2];
        for (int i = 2; i < tokens.length; i++) {
          parameters[i - 2] = tokens[i];
        }

        response = player.execute(tokens[1], parameters);
        
        prettyPrintResponse(systemOut, response);
        
      } else {
        systemOut
            .println("Exec commands require at least one parameter. Set it to null if irrelevant");
      }
    } else {
      systemOut.println("I do not understand that long command. (Type 'h' for help)");
    }
  }

  private void prettyPrintResponse(PrintStream systemOut2, JSONObject reply) {
    // Handle error situations
    String errorCode = reply.get(MarshalingKeys.ERROR_CODE_KEY).toString();
    
    if (errorCode.equals(StatusCode.OK)) {
      String header = (String) reply.get(MarshalingKeys.RETURNVALUE_HEAD_KEY);
      JSONArray returnValueArray = (JSONArray) reply.get(MarshalingKeys.RETURNVALUE_TAIL_KEY);

      // Print all returned strings in the head and tail
      systemOut.println(header);
      if (returnValueArray != null) {
        for (Object s : returnValueArray) {
          systemOut.println(s.toString());
        }
      }
    } else {
      // Error situation
      String errMsg = reply.get(MarshalingKeys.ERROR_MSG_KEY).toString();
      systemOut.println("Failed. Error message: " + errMsg);
    }
  }

  /**
   * Merge the tokens in array 'tokens' from
   * index 'from' into a space separated string.
   * @param tokens the array of tokens
   * @param from the starting index 
   * @return a merged string with all tokens
   * separated by space
   */
  private String mergeTokens(String[] tokens, int from) {
    String mergedString="";
    for (int i=from; i < tokens.length; i++) { mergedString += " "+tokens[i]; }
    // fix bug in E15 version, remove first space
    return mergedString.substring(1);
  }

  private void handleSingleCharCommand(char primaryCommand) {
    switch (primaryCommand) {
    // look
    case 'l': {
      systemOut.println(player.getLongRoomDescription());
      break;
    }
    // The movement commands
    case 'n': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 's': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 'e': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 'w': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 'u': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 'd': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }

    // position
    case 'p': {
      systemOut.println("Your position in the cave is: "
          + player.getPosition());
      break;
    }
    // Help
    case 'h': {
      showHelp();
      break;
    }
    // Quit
    case 'q': {
      LogoutResult logoutResult = cave.logout(player.getID());
      systemOut.println("Logged player out, result = " + logoutResult);
      break;
    }
    default: {
      systemOut.println("I do not understand that command. (Type 'h' for help)");
    }
    }
  }

  private Direction getDirectionFromChar(char commandChar) {
    switch (commandChar) {
    case 'n':
      return Direction.NORTH;
    case 's':
      return Direction.SOUTH;
    case 'e':
      return Direction.EAST;
    case 'w':
      return Direction.WEST;
    case 'u':
      return Direction.UP;
    case 'd':
      return Direction.DOWN;
    default:
      throw new RuntimeException("getDirectionFromChar got wrong parameter: "
          + commandChar);
    }
  }

  private void tryToMove(Direction direction) {
    if ( player.move(direction) ) {
      systemOut.println("You moved "+direction);
      systemOut.println(player.getShortRoomDescription());
    } else {
      systemOut.println("There is no exit going " + direction);
    }
  }

  private void showHelp() {
    systemOut.println("=== Help on the SkyCave commands. ===");
    systemOut.println("  Many commonly used commands are single-character");
    systemOut.println("Commands:");
    systemOut.println(" n,s,e,w,d,u    :  MOVE north, south, etc;");
    systemOut.println(" q              :  QUIT sky cave;");
    systemOut.println(" h              :  HELP, print this help instructions;");
    systemOut.println(" l              :  LOOK, print long description of present room;");
    systemOut.println(" p              :  POSITION, print your (x,y,z) position");
    systemOut.println("Longer Commands:");
    systemOut.println(" who            :  WHO, print info on your avatar;");
    systemOut.println(" dig [d] [desc] :  DIG room in direction [d] with description [desc];");
    systemOut.println(" post [msg]     :  POST [msg] on this room's wall;");
    systemOut.println(" read           :  READ messages on this room's wall;");
    systemOut.println(" weather        :  WEATHER in the players region;");
    systemOut.println(" back           :  BACKTRACK to previous room;");
    systemOut.println(" sys            :  SYStem and configuration information;");

    systemOut.println(" exec [cmd] [param]* :  EXEC [cmd] with 1 or more [param]s;");
    
  }

}