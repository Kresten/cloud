package cloud.cave.common;

/** This exception is thrown by the access proxy on the player object
 * to indicate that an attempt has been made to call a method on
 * a player object whose session is no longer valid.
 * <p>
 * This happens if the same loginName is logging into the cave,
 * one after the other. This is valid to do, but only the last
 * login is allowed to invoke methods on the player object.
 * <p>
 * A similar approach is taken by Blizzard games (last login
 * is the valid one, all previous ones are disconnected), as is
 * probably the case for many other systems.
 *
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class PlayerSessionExpiredException extends CaveException {

  private static final long serialVersionUID = 3713468163867063301L;

  public PlayerSessionExpiredException(String reason) {
    super(reason);
  }

}
