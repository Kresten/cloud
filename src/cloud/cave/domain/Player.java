package cloud.cave.domain;

import java.util.*;

import org.json.simple.JSONObject;

/**
 * The Player role is a the avatar that does the interaction on behalf of the
 * user in the cave.
 * <p>
 * A player is characterized by his/her
 * <ul>
 * <li>'loginName': the name (which MUST be your student ID in the CloudArch
 * course in order to give you your proper score) used to identify the player to
 * the subscription service; typically only used during login.</li>
 * 
 * <li>'playerName': the name by which the player is known in the cave; this is
 * the name that other players see during the cave exploration experience.</li>
 * 
 * <li>'playerID': a unique ID assigned by the subscription system to identify
 * the player; used throughout the code to identify a player but rarely used in
 * the user interface (similar to battle-tags in Blizzard systems.)</li>
 * </ul>
 * 
 * A player is always associated with specific room in the cave through his/her
 * 'position'.
 * <p>
 * Accessors include methods to view the room that the player is in, see the
 * list of other players in the same room, etc.
 * <p>
 * Some accessors are volatile, that is, two consecutive executions may return
 * different results, as other players may modify the state of the room the
 * player is in.
 * <p>
 * Mutators include methods to move a player in the cave, 'dig' new rooms, and
 * an 'execute' method that may execute command objects installed on the server.
 * The latter allows new behavior to be added at run-time.
 * <p>
 * Class invariants:
 * <p>
 * A player object shall never be instantiated directly but only returned as the
 * result of a successful cave.login().
 * <p>
 * A player object's method shall never be called after the cave.logout()
 * <p>
 * Note: The domain objects of the cave system are Player = the avatar with a
 * specific position in the cave, Cave = the matrix of rooms, and Room = the
 * location with a description, a set of exits, and a list of players currently
 * in this room.
 * <p>
 * However, from the perspective of the player, there is a one-to-one relation
 * to the room which the player is currently in. Therefore the present interface
 * acts as a facade to the room related behavior, i.e.
 * player.getShortRoomDescription() in order to avoid
 * player.getRoom().getShortDescription(). (Which also obeys Law of Demeter / Do
 * not talk to strangers).
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */

public interface Player {

  /**
   * Return the in-game player name, a synonym under which the player is known
   * in the cave.
   * 
   * @return in-game player name
   */
  String getName();
  
  /**
   * Return the unique ID of this player
   * 
   * @return unique system generated ID of player, assigned by the server side
   *         at registration time.
   */
  String getID();

  /**
   * Get the position of the player in the grid, as a string in format (x,y,z).
   * The entry room is (0,0,0).
   * 
   * @return the coordinates of the player's position as a string (x,y,z)
   */
  public String getPosition();

  /**
   * Get a short description of the room this player is currently in.
   * 
   * @return the description of the room the player is in.
   */
  String getShortRoomDescription();

  /**
   * Get the region (real physical region, not virtual in the cave) that this
   * player is subscribed in.
   * 
   * @return the region of the player
   */
  Region getRegion();

  /**
   * Get the ID of the current session this player is involved in. A session
   * lasts from when a login of the player is made until he/she logs out again.
   * Every session is assigned a unique id for that particular session. If a
   * player object is 'dead' i.e. not logged in, the session ID is null.
   * 
   * <br>
   * 
   * The session id is not that interesting for the end user, but used
   * internally to handle multiple sessions on the same loginName.
   * 
   * @return unique id of this session or null in case no session
   */
  String getSessionID();

  // === Volatile accessors

  /**
   * Get a list of in-game player names of all players in the same room as this
   * player is in.
   * 
   * @return list of player data transfer objects
   */
  List<String> getPlayersHere();

  /**
   * Get a long description of the current room, which includes a textual list
   * of exits, and textual list of players in this room.
   * 
   * @return long description of the room the player is in
   */
  String getLongRoomDescription();

  /**
   * Get the set of exits that lead to new rooms from the players current
   * position.
   * 
   * @return set of exits from the room the player is located in
   */
  List<Direction> getExitSet();

  /**
   * Get the list of messages currently on the wall of the room that this player
   * is located in. Messages should be sorted such that last entered
   * message appears at index 0.<br>
   * 
   * Each entry in the list is on the form <br>
   * 
   * <code>[playerName] message</code> <br>
   * 
   * For example: <br>
   * 
   * <code>[Mathilde] I like the description of this room.</code>
   * 
   * @return list of messages on the 'wall', sorted with latest
   * messages appearing at lower indices
   * 
   */
  List<String> getMessageList();

  /**
   * Return the physical weather situation right now in the location that this
   * player is registered in (one of the REGION values). <br>
   * Example output: <br>
   * <code>
   * The weather in AARHUS is Clear, temperature 27,4C (feelslike -2,7C). Wind: 1,2 m/s, direction West.
   * This report is dated: Thu, 05 Mar 2015 09:38:37 +0100.   
   * </code>
   * 
   * @return the weather outside in a specific format
   * 
   */
  String getWeather();

  // === Mutators
  /**
   * Move this player in the given direction.
   * 
   * @param direction
   *          the direction to move
   * @return true in case there is a room in the given direction, false
   *         otherwise
   */
  boolean move(Direction direction);

  /**
   * Execute a backtracking move, i.e. move this player back to the room he/she
   * last came from. A player can 'backtrack' through all rooms visited since
   * the last log-in, but cannot back any further than that.
   * 
   * @return true if the player backtracked into the room he/she last visited,
   *         or false in case he/she is in the room he/she was in when the
   *         session started.
   */
  boolean backtrack();

  /**
   * Create a new room with the given description in the given direction. The
   * player is NOT moved into the room created.
   * 
   * @param direction
   *          geographical direction to 'dig' the room in
   * @param description
   *          the description of the room
   * 
   * @return true in case the room was created; false if a room already exists
   *         in the given direction.
   */
  boolean digRoom(Direction direction, String description);

  /**
   * Add a message to this room's wall. Each message is added to the list of
   * messages on the wall.
   * 
   * @param message
   *          the message that this player adds to the wall to this room.
   */
  void addMessage(String message);

  /**
   * Execute a command object with the given name.
   * 
   * @param commandName
   *          the name of a Java class implementing the Command interface and
   *          located in the classpath defined by Constants.EXTENSION_CLASSPATH.
   *          (This is an application of the Command Pattern, see e.g.
   *          "Flexible, Reliable Software" p. 308, CRC Press 2010.)
   * @param parameters
   *          the (variable number of) parameters for the command; all must be
   *          strings
   * @return A JSONObject that contains the result of executing the command, or
   *         error codes and messages in case the command execution failed.
   */
  JSONObject execute(String commandName, String... parameters);
}
