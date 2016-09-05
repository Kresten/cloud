package cloud.cave.domain;

/** The various types of results of logging in.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public enum LoginResult {
  LOGIN_SUCCESS, // The login was successful
  LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN, // The login was conditionally successful, this is a warning that the player is already logged in

  LOGIN_FAILED_UNKNOWN_SUBSCRIPTION, // The login failed, as the player with given id has no subscription
  LOGIN_FAILED_SERVER_ERROR; // The login failed due to some error on the server side, review server logs

  /** Return true in case the login result represents a valid
   * login
   * @param loginResult one of the login result enums
   * @return true if the code represents a valid login
   */
  public static boolean isValidLogin(LoginResult loginResult) {
    boolean isValidLogin =
        loginResult == LoginResult.LOGIN_SUCCESS ||
        loginResult == LoginResult.LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN;

    return isValidLogin;
  } 
}
