package cloud.cave.domain;

/** A login operation results in an instance of this
 * interface which represents the result of the login.
 * 
 * If successful then getPlayer() will return the player
 * object.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public interface Login {

  Player getPlayer();

  LoginResult getResultCode();

}
