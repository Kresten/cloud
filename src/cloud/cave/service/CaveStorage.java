package cloud.cave.service;

import java.util.*;

import cloud.cave.domain.*;
import cloud.cave.server.common.*;

/**
 * The storage service for the cave as a set of rooms and a set of players in
 * positions in the cave.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public interface CaveStorage extends ExternalService {
  
  // === Room related

  /**
   * Get the room for the given position
   * 
   * @param positionString
   *          the (x,y,z) of the position encoded as a position string.
   * @return the room's properties in a record
   */
  RoomRecord getRoom(String positionString);

  /**
   * Add a room in a given position or fail if there is already a room there.
   * 
   * @param positionString
   *          the (x,y,z) of the position encoded as a position string.
   * @param description
   *          the text description of the room to be added
   * @return true if the room was added, false in case there was already a room
   *         present.
   */
  boolean addRoom(String positionString, RoomRecord description);

  /**
   * Compute the set of valid exits leading out from a given position
   * 
   * @param positionString
   *          position of the room whose exits is wanted
   * @return set of directions that leads to another room
   */
  List<Direction> getSetOfExitsFromRoom(String positionString);

  // === Player record related

  /**
   * Given a player ID get the record of that player's main attributes. Note
   * that a record is provide even if the player is not presently in the cave (=
   * has no active session)
   * 
   * @param playerID
   *          id of the player
   * @return the record for the player or null in case the player is not
   *         registered in the cave
   */
  PlayerRecord getPlayerByID(String playerID);

  /**
   * Given a player record, update the existing stored record with the contents
   * of the provided one, or create a record if none already exists.
   * 
   * @param record
   *          the record to insert/overwrite the old one
   */
  void updatePlayerRecord(PlayerRecord record);

  /**
   * Compute a list of players that are located in a given room
   * 
   * @param positionString
   *          position of the room
   * @return list of player records identify the players presently in the room
   */
  List<PlayerRecord> computeListOfPlayersAt(String positionString);

  /**
   * Compute the number of players that are present in the cave ('has an active
   * session')
   * 
   * @return the number of active players in the cave
   */
  int computeCountOfActivePlayers();

}
