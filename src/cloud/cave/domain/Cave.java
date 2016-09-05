package cloud.cave.domain;

/**
 * The Cave role defines the context for the user experience. A cave consists of
 * a 3 dimensional matrix/lattice of 'rooms'. A room has a textual description
 * and can be visited by any number of 'players'. In addition a room has a single
 * wall on which players can post messages as well as read all messages posted.
 * <p>
 * The main responsibility of the Cave is to log-in and log-out any registered
 * player to start/stop the (collaborative) cave exploration experience.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public interface Cave {

  /**
   * Try to login a user with the given loginName and password into the cave.
   * The returned LoginResult will tell if the login was successful or not. If
   * success, the LoginResult will also contain a reference to the player object
   * that the user must use to navigate the cave.
   * 
   * @param loginName
   *          the name used for login
   * @param password
   *          the password
   * @return the result of the login, which is a data structure that contains
   *         the result of the login, and if success a reference to the player
   *         object
   */
  Login login(String loginName, String password);

  /**
   * Logout a player.
   * 
   * @param playerID
   *          id of the player
   * @return the result of the logout operation.
   */
  LogoutResult logout(String playerID);

  /**
   * Return a string containing the configuration of all internally used
   * delegates. Mostly for inspection and validation by course teachers.
   * 
   * @return description of internal configuration of delegates
   */
  String describeConfiguration();
}
