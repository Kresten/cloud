package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.List;

import org.junit.*;

import cloud.cave.config.*;
import cloud.cave.domain.*;
import cloud.cave.doubles.*;
import cloud.cave.server.common.*;
import cloud.cave.service.CaveStorage;

/**
 * Testing the access pattern of the DB regarding updates and simple queries of
 * the player record.
 * <p>
 * Demonstrates the use of a spy to inspect behavior of the the cave and player
 * implementations' use of the underlying database.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestStorageAccess {
  private Cave cave;
  private SpyCaveStorage spy;
  
  @Before
  public void setup() {
    CaveServerFactory factory = new AllTestDoubleFactory() {
      @Override
      public CaveStorage createCaveStorageConnector(ObjectManager objMgr) {
        CaveStorage storage = new FakeCaveStorage();
        storage.initialize(null, null);
        // Decorate the storage with a spy that
        // monitors DB access patterns
        spy = new SpyCaveStorage(storage);
        return spy;
      }

    };
    ObjectManager objMgr = new StandardObjectManager(factory);
    cave = new CaveServant(objMgr);
  }

  @Test
  public void shouldSpyAccessPatternInDB() {
    Login loginResult = cave.login( "magnus_aarskort", "312");
    Player p1 = loginResult.getPlayer();
    assertNotNull(p1);

    // assert the number of database updates and queries
    assertThat(spy.getPlayerUpdateCount(), is(1));
    // Three 'gets' in the db, could actually be optimized a bit
    assertThat(spy.getPlayerGetCount(), is(3));

    // assert the number of updates and queries
    
    // Uncomment the statement below to get full stack traces of
    // where the storage queries are made in the player impl.
    // spy.setTracingTo(true);
    
    p1.getPlayersHere();
    spy.setTracingTo(false);
    
    assertThat(spy.getPlayerUpdateCount(), is(1)); // no updates
    assertThat(spy.getPlayerGetCount(), is(4)); // and a single query extra
    
    LogoutResult result = cave.logout(p1.getID());
    assertNotNull("The result of the logout is null", result);
    assertEquals(LogoutResult.SUCCESS, result);
  }

}

/** Rudimentary implementation! */
class SpyCaveStorage implements CaveStorage {

  private CaveStorage decoratee;
  private boolean traceOn;
  
  public SpyCaveStorage(CaveStorage decoratee) {
    super();
    this.decoratee = decoratee;
    traceOn = false;
  }

  public void setTracingTo(boolean b) {
    traceOn = b; 
  }

  public RoomRecord getRoom(String positionString) {
    incrementGetCount();
    return decoratee.getRoom(positionString);
  }


  public boolean addRoom(String positionString, RoomRecord description) {
    updateCount++;
    return decoratee.addRoom(positionString, description);
  }


  public void initialize(ObjectManager objMgr, ServerConfiguration config) {
    decoratee.initialize(objMgr, config);
  }


  public List<Direction> getSetOfExitsFromRoom(String positionString) {
    incrementGetCount();
    return decoratee.getSetOfExitsFromRoom(positionString);
  }


  private int getCount = 0;
  public PlayerRecord getPlayerByID(String playerID) {
    incrementGetCount();
    return decoratee.getPlayerByID(playerID);
  }

  @SuppressWarnings("static-access")
  private void incrementGetCount() {
    getCount++;
    if (traceOn) Thread.currentThread().dumpStack();
  }
  
  public int getPlayerGetCount() {
    return getCount;
  }

  public void disconnect() {
    decoratee.disconnect();
  }


  private int updateCount = 0;
  public void updatePlayerRecord(PlayerRecord record) {
    updateCount++;
    decoratee.updatePlayerRecord(record);
  }

  public int getPlayerUpdateCount() {
    return updateCount;
  }

  public ServerConfiguration getConfiguration() {
    // Not a DB access, only an object call
    return decoratee.getConfiguration();
  }

  public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
    incrementGetCount();
    return decoratee.computeListOfPlayersAt(positionString);
  }


  public int computeCountOfActivePlayers() {
    incrementGetCount();
    return decoratee.computeCountOfActivePlayers();
  }

}
