package cloud.cave.common;

import cloud.cave.domain.*;

/**
 * Record / PODO defining the result of a login (attempt) of a player.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class LoginRecord implements Login {

  private Player player;
  private LoginResult errorCode;

  /**
   * Create a login result representing a CORRECT login
   * 
   * @param player a valid instance of player
   * @param code the result of the login
   */
  public LoginRecord(Player player, LoginResult code) {
    this.player = player;
    errorCode = code;
  }

  /**
   * Create a login result representing an INVALID login
   * 
   * @param errorCode
   *          the code representing what is the cause of the invalid login.
   */
  public LoginRecord(LoginResult errorCode) {
    player = null;
    this.errorCode = errorCode;
  }

  @Override
  public Player getPlayer() {
    return player;
  }

  @Override
  public LoginResult getResultCode() {
    return errorCode;
  }

  @Override
  public String toString() {
    String playername = "UNDEFINED";
    if ( getPlayer() != null ) { playername = getPlayer().getName(); }
    return "(LoginResult: "+playername+"/"+getResultCode()+")";
  }
}
