package cloud.cave.service;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cloud.cave.domain.*;
import cloud.cave.doubles.*;
import cloud.cave.server.common.*;
import static org.hamcrest.CoreMatchers.*;

/** TDD of the storage handling interfaces.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestStorage {

  private CaveStorage storage;
  
  // Two subscription records, tied to id1 and id2
  private SubscriptionRecord sub1, sub2;
  
  private String id1 = "id02";
  private String id2 = "id-203212";
  
  private Point3 p000 = new Point3(0, 0, 0);

  private Point3 p876 = new Point3(8,7,6);
  private Point3 p273 = new Point3(2,7,3);

  @Before
  public void setUp() throws Exception {
    storage = new FakeCaveStorage();
    storage.initialize(null, null);
    
    sub1 = new SubscriptionRecord(id1,"Tutmosis", "grp01", Region.ODENSE);
    sub2 = new SubscriptionRecord(id2, "MrLongName", "grp02", Region.COPENHAGEN);
  }
  
  @After
  public void tearDown() {
    storage.disconnect();
  }

  @Test
  public void shouldUpdateRoomTables() {
    RoomRecord room = storage.getRoom(p000.getPositionString());
    assertThat( room.description, is("You are standing at the end of a road before a small brick building."));
    
    p000 = new Point3(-1,0,0);
    room = storage.getRoom(p000.getPositionString());
    assertThat( room.description, containsString("You have walked up a hill, still"));
    
    // validate that rooms can be made
    boolean canAdd = storage.addRoom(p273.getPositionString(), new RoomRecord("You are in a dark lecturing hall."));
    assertThat(canAdd, is(true));
    
    room = storage.getRoom(p273.getPositionString());
    assertThat( room.description, is("You are in a dark lecturing hall."));

    // validate that existing rooms cannot be overridden
    canAdd = storage.addRoom(p273.getPositionString(), new RoomRecord("This room must never be made"));
    
    assertThat(canAdd, is(false));
  }
  
  @Test
  public void shouldGetExitSet() {
    List<Direction> exits = storage.getSetOfExitsFromRoom(p000.getPositionString());
    assertThat( exits.size(), is(4));
    
    assertThat( exits.contains(Direction.EAST), is(true) );
    assertThat( exits.contains(Direction.WEST), is(true) );
    assertThat( exits.contains(Direction.NORTH), is(true) );
    assertThat( exits.contains(Direction.SOUTH), is(false) );
    assertThat( exits.contains(Direction.DOWN), is(false) );
    assertThat( exits.contains(Direction.UP), is(true) );
  }
  
  @Test
  public void shouldUpdatePlayerAndPositionTables() {
    // Add player
    SubscriptionRecord sub01 = sub1; String sessionid = "session1";
    addPlayerRecordToStorageForSubscription(sub01, sessionid);

    // and move him to position 2,7,3
    updatePlayerPosition(id1, p273.getPositionString());
    
    // assert that we have recorded the player
    int count = storage.computeCountOfActivePlayers();
    assertThat(count, is(1));
    
    // Tutmosis is in the cave
    assertThat(storage.getPlayerByID(id1).getPlayerName(), is("Tutmosis"));
    assertThat(storage.getPlayerByID(id1).isInCave(), is(true));
    
    // get all players at 2,7,3
    List<PlayerRecord>  ll = storage.computeListOfPlayersAt(p273.getPositionString());
    
    assertThat( ll.size(), is(1));
    assertThat( ll.get(0).getPlayerID(), is(id1));
    
    // and verify none are at 8,7,6
    ll = storage.computeListOfPlayersAt(p876.getPositionString());
    assertThat( ll.size(), is(0));
    
    // Intro another player
    addPlayerRecordToStorageForSubscription(sub2, "session2");

    
    assertThat(storage.computeCountOfActivePlayers(), is(2));
    
    // move player 2 to same 8,7,6
    updatePlayerPosition(id2, p876.getPositionString());

    ll = storage.computeListOfPlayersAt(p876.getPositionString());
    assertThat( ll.size(), is(1));
    assertThat( ll.get(0).getPlayerID(), is(id2));
    
    // move player 1 there also
    updatePlayerPosition(id1, p876.getPositionString());
    
    // and verify that computation is correct
    ll = storage.computeListOfPlayersAt(p876.getPositionString());
    assertThat( ll.size(), is(2));
    assertThat( ll.get(0).getPlayerID(), either(is(id1)).
        or(is(id2)));
    assertThat( ll.get(1).getPlayerID(), either(is(id1)).
        or(is(id2)));
  }

  private void updatePlayerPosition(String id12, String positionString) {
    PlayerRecord pRecord = storage.getPlayerByID(id12);
    pRecord.setPositionAsString(positionString);
    storage.updatePlayerRecord(pRecord);
  }

  private void addPlayerRecordToStorageForSubscription(SubscriptionRecord sub01, 
      String sessionid) {
    PlayerRecord rec1 = new PlayerRecord(sub01, "(0,0,0)", sessionid);
    storage.updatePlayerRecord(rec1);
  }
  
  @Test
  public void shouldUpdatePlayerTables() {
    addPlayerRecordToStorageForSubscription(sub1, "session1");
    addPlayerRecordToStorageForSubscription(sub2, "session2");

    // two players in cave
    assertThat( storage.computeCountOfActivePlayers(), is(2));
    
    // end session for player one
    PlayerRecord rec1 = storage.getPlayerByID(id1);
    rec1.setSessionId(null);
    storage.updatePlayerRecord(rec1);
    
    // now only one left
    assertThat( storage.computeCountOfActivePlayers(), is(1));
    
    // and the right one is left
    PlayerRecord p;
    p = storage.getPlayerByID(id1);
    assertThat( p.isInCave(), is(false));
    
    p = storage.getPlayerByID(id2);
    assertThat( p.isInCave(), is(true));
  }
  
  @Test
  public void shouldGetPlayerByID() {
    addPlayerRecordToStorageForSubscription(sub1, "session1");
    addPlayerRecordToStorageForSubscription(sub2, "session2");
    
    PlayerRecord p;
    p = storage.getPlayerByID(id1);
    assertThat( p.getPlayerName(), is("Tutmosis"));
    p = storage.getPlayerByID(id2);
    assertThat( p.getPlayerName(), is("MrLongName"));
  }
  
  @Test
  public void shouldInchUpCoverageOfPoint() {
    // Not really a storage test, but increases
    // test coverage just a tiny bit, and Point3
    // is closely associated with storage
    assertThat(p876.toString(), is("(8,7,6)"));
  }
}
