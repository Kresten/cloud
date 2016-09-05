package cloud.cave.domain;

/** The result of a logout operation.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public enum LogoutResult {
  SUCCESS, // The logout was correct       
  PLAYER_NOT_IN_CAVE, // Attempted logout of player that is not in the cave
  SERVER_FAILURE // Some error happened on the server side, review logs
}
