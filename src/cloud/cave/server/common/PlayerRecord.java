package cloud.cave.server.common;
import cloud.cave.domain.Region;

/**
 * This is a record type (struct / PODO (Plain Old Data Object)) representing
 * the core data of a player like name, id, position, etc.
 * <p>
 * A record is a pure data object without any behavior, very suitable for
 * networking and persistence as it only contains data.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class PlayerRecord {
  private String playerID;
  private String playerName;
  private String groupName; 
  private Region region;
  
  private String positionAsString;
  private String sessionID;

  public PlayerRecord(SubscriptionRecord subscription, 
      String positionString, 
      String sessionID) {
    super();
    this.playerID = subscription.getPlayerID();
    this.playerName = subscription.getPlayerName();
    this.groupName = subscription.getGroupName();
    this.region = subscription.getRegion();
    this.positionAsString = positionString;
    this.sessionID = sessionID;
  }

  public String getPlayerID() {
    return playerID;
  }
  public String getPlayerName() {
    return playerName;
  }
  public String getGroupName() {
    return groupName;
  }
  public String getPositionAsString() {
    return positionAsString;
  }
  public Region getRegion() {
    return region;
  }
  /**
   * get the session ID; if it is null
   * then the player is not presently in the cave
   * @return the session ID or null in case
   * no session exists for the given player
   */
  public String getSessionId() {
    return sessionID;
  }
  public boolean isInCave() {
    return sessionID != null;
  }

  public void setPositionAsString(String positionAsString) {
    this.positionAsString = positionAsString;
  }
  public void setSessionId(String sessionId) {
    this.sessionID = sessionId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((playerID == null) ? 0 : playerID.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PlayerRecord other = (PlayerRecord) obj;
    if (playerID == null) {
      if (other.playerID != null)
        return false;
    } else if (!playerID.equals(other.playerID))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "PlayerRecord [playerID=" + playerID + ", playerName=" + playerName
        + ", groupName=" + groupName + ", region=" + region
        + ", positionAsString=" + positionAsString + ", sessionID=" + sessionID
        + "]";
  }



}
