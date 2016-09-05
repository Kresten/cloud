package cloud.cave.doubles;

import java.util.*;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;

/**
 * Fake object implementation of storage. Map data structures are used to
 * simulate SQL tables / NoSQL collections. The used data structures are not
 * synchronized, do not use in multi-threaded server!
 * <p>
 * The cave is initialized with five rooms in a fixed layout, vaguely inspired
 * by the original Colossal Cave layout. These rooms serve the test cases as
 * well as populate newly created caves.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class FakeCaveStorage implements CaveStorage {
  
  // The table/collection of rooms in the cave.
  // The positionString is the primary key and the value object
  // for a room the rest of the tuple
  Map<String,RoomRecord> roomMap;

  @Override
  public void initialize(ObjectManager objMgr, ServerConfiguration config) {
    this.serverConfiguration = config;
    roomMap = new HashMap<String, RoomRecord>();
    playerId2PlayerSpecs = new HashMap<String, PlayerRecord>(5);

    RoomRecord entryRoom = new RoomRecord(
        "You are standing at the end of a road before a small brick building.");
    this.addRoom(new Point3(0, 0, 0).getPositionString(), entryRoom);
    this.addRoom(new Point3(0, 1, 0).getPositionString(), new RoomRecord(
        "You are in open forest, with a deep valley to one side."));
    this.addRoom(new Point3(1, 0, 0).getPositionString(), new RoomRecord(
        "You are inside a building, a well house for a large spring."));
    this.addRoom(new Point3(-1, 0, 0).getPositionString(), new RoomRecord(
        "You have walked up a hill, still in the forest."));
    this.addRoom(new Point3(0, 0, 1).getPositionString(), new RoomRecord(
        "You are in the top of a tall tree, at the end of a road."));
  }
  
  @Override
  public void disconnect() {
    roomMap = null;
  }

  @Override
  public RoomRecord getRoom(String positionString) {
    return roomMap.get(positionString);
  }

  @Override
  public boolean addRoom(String positionString, RoomRecord newRoom) {
    // if there is already a room, return false
    if ( roomMap.containsKey(positionString) ) { return false; }
    roomMap.put(positionString, newRoom);
    return true;
  }

  @Override
  public List<Direction> getSetOfExitsFromRoom(String positionString) {
    List<Direction> listOfExits = new ArrayList<Direction>();
    Point3 pZero = Point3.parseString(positionString);
    Point3 p;
    for ( Direction d : Direction.values()) {
      p = new Point3(pZero.x(), pZero.y(), pZero.z());
      p.translate(d);
      String position = p.getPositionString();
      if ( roomMap.containsKey(position)) {
        listOfExits.add(d);
      }
    }
    return listOfExits;
  }

  // === The table with primary key playerID whose columns are the
  // specifications of a given player. The private datastructure PlayerSpecs
  // represents the
  // remaining tuple values.
  
  Map<String,PlayerRecord> playerId2PlayerSpecs;

  @Override
  public PlayerRecord getPlayerByID(String playerID) {
    PlayerRecord ps = playerId2PlayerSpecs.get(playerID);
    return ps;
  }
  
  @Override
  public void updatePlayerRecord(PlayerRecord record) {
    playerId2PlayerSpecs.put(record.getPlayerID(), record);
  }

  @Override
  public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
    List<PlayerRecord> theList = new ArrayList<PlayerRecord>();
    for ( String id : playerId2PlayerSpecs.keySet() ) {
      PlayerRecord ps = playerId2PlayerSpecs.get(id);
      if (ps.isInCave() && ps.getPositionAsString().equals(positionString)) {
        theList.add(ps);
      }
    }
    return theList;
  }
  
  @Override
  public int computeCountOfActivePlayers() {
    return getPlayerList().size();
  }

  /** Compute the list of players in the cave.
   * 
   * @return list of all players in the cave.
   */
  private List<PlayerRecord> getPlayerList() {
    List<PlayerRecord> theList = 
        new ArrayList<PlayerRecord>();
    for ( String id : playerId2PlayerSpecs.keySet() ) {
      PlayerRecord ps = playerId2PlayerSpecs.get(id);
      if (ps.isInCave()) {
        theList.add(ps);
      }
    }
    return theList;
  }

  /**
   * Debugging method to dump contents of internal data structures
   * 
   * @return the contents of the map(player id, player records)
   */
  public String dumpContents() {
    return "FakeStorage: "+playerId2PlayerSpecs+".";
  }

  public String toString() {
    return "FakeCaveStorage (" + roomMap.keySet().size() + " rooms. " +
        playerId2PlayerSpecs.keySet().size() + " players)";
  }

  ServerConfiguration serverConfiguration;
  
  @Override
  public ServerConfiguration getConfiguration() {
    return serverConfiguration;
  }
  
}
