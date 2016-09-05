package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.*;

import cloud.cave.common.*;
import cloud.cave.domain.*;

/** Test server side implementation of the Player abstraction.
 * 
 * On the server side, a player object directly communicate
 * with the storage layer in order to modify its state.
 * 
 * Most of these tests are the results of TDD. 
 * 
 * Many of the 'later' tests are abstracted into
 * static methods in CommonPlayerTests to allow
 * the same tests to be run using the client side
 * proxies as well.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestServerPlayer {

  private Cave cave;
  private Player player;
  
  private String description;
  
  @Before
  public void setup() {
    cave = CommonCaveTests.createTestDoubledConfiguredCave().getCave();
    
    Login loginResult = cave.login( "mikkel_aarskort", "123");
    player = loginResult.getPlayer();
  }
 
  // TDD of simple player attributes
  @Test
  public void shouldAccessSimpleAttributes() {
    CommonPlayerTests.shouldAccessSimpleAttributes(player);
  }

  // TDD room description
  @Test
  public void shouldHaveInitialLocation() {
    description = player.getShortRoomDescription();
    assertTrue("Initial location missing proper description",
        description.contains("You are standing at the end of a road before a small brick building."));
  }

  // TDD the movement of a player
  @Test
  public void shouldAllowNorthOneMove() {
    player.move(Direction.NORTH);
    description = player.getShortRoomDescription();
    assertTrue("OneMoveNorth missing proper description",
        description.contains("You are in open forest, with a deep valley to one side."));
    // Move back again
    player.move(Direction.SOUTH);
    description = player.getShortRoomDescription();
    assertTrue("Initial location missing proper description",
        description.contains("You are standing at the end of a road before a small brick"));
  }
  
  // TDD movement of player
  @Test
  public void shouldAllowEastWestMoves() {
    player.move(Direction.EAST);
    description = player.getShortRoomDescription();
    assertTrue("EastWestMove 1 missing proper description",
        description.contains("You are inside a building, a well house for a large spring."));
    
    player.move(Direction.WEST);
    description = player.getShortRoomDescription();
    assertTrue("EastWestMove 2 missing proper description",
        description.contains("You are standing at the end of a road before a small brick"));

    player.move(Direction.WEST);
    description = player.getShortRoomDescription();
    assertTrue("EastWestMove 3 missing proper description",
        description.contains("You have walked up a hill, still in the forest."));

    player.move(Direction.EAST);
    description = player.getShortRoomDescription();
    assertTrue("EastWestMove 4 missing proper description",
        description.contains("You are standing at the end of a road before a small brick"));
  }

  // Handle illegal moves, trying to move to non-existing room
  @Test
  public void shouldNotAllowMovingSouth() {
    boolean canMove = player.move(Direction.SOUTH);
    assertFalse("It should not be possible to move south, no node there", canMove);
    
    description = player.getShortRoomDescription();
    assertTrue("Initial location missing proper description",
        description.contains("You are standing at the end of a road before a small brick"));
  }
  
  // TDD the behavior for changing the (x,y,z) coordinates
  // during movement
  @Test
  public void shouldTestCoordinateTranslations() {
    assertEquals( "(0,0,0)", player.getPosition());

    player.move(Direction.NORTH);
    assertEquals( "(0,1,0)", player.getPosition());    
    player.move(Direction.SOUTH);
    assertEquals( "(0,0,0)", player.getPosition());    

    player.move(Direction.UP);
    assertEquals( "(0,0,1)", player.getPosition());    
    player.move(Direction.DOWN);
    assertEquals( "(0,0,0)", player.getPosition());    

    player.move(Direction.WEST);
    assertEquals( "(-1,0,0)", player.getPosition());    

    player.move(Direction.EAST);
    assertEquals( "(0,0,0)", player.getPosition());    
  }
  
  // TDD digging new rooms for a player
  @Test
  public void shouldAllowPlayerToDigNewRooms() {
    CommonPlayerTests.shouldAllowPlayerToDigNewRooms(player);
  }

  // Cannot dig a node in a direction where a node already exists
  @Test
  public void shouldNotAllowDigAtEast() {
    CommonPlayerTests.shouldNotAllowDigAtEast(player);
  }
  
  // TDD of get exits, a validate that the
  // the long description is correct
  @Test
  public void shouldShowExitsForPlayersPosition() {
    CommonPlayerTests.shouldShowExitsForPlayersPosition(player);
  }

  // TDD of get exits
  @Test
  public void shouldShowValidExitsFromEntryRoom() {
    CommonPlayerTests.shouldGetProperExitSet(player);
  }
  
  // Positions of players are stored across logins
  @Test
  public void shouldBeAtPositionOfLastLogout() {
    // Log mathilde into the cave, initial position is 0,0,0
    // as the database is reset
    Login loginResult = cave.login( "mathilde_aarskort", "321");
    Player p1 = loginResult.getPlayer();

    CommonPlayerTests.shouldBeAtPositionOfLastLogout(cave, p1);
  }

  // TDD of session id
  @Test
  public void shouldUniqueAssignSessionIdForEveryLogin() {
    // The session id should be a new ID for every session 
    // (a session lasts from when a player logs in until he/she
    // logs out).
    String idoriginal = player.getSessionID();
    String playerId = player.getID();
    assertNotNull(idoriginal);
    
    // Do a second login and ensure that it gets a new session id
    Login loginResult = cave.login( "mikkel_aarskort", "123");
    Player p1 = loginResult.getPlayer();
    
    // It should be the same player, now double "logged in"
    // note, cannot call player.getID() as this will throw an
    // access control exception
    assertThat(playerId, is(p1.getID()));
    
    // But the session id is different
    assertThat(p1.getSessionID(), is(not(idoriginal)));
  }
  
  // TDD of the 'back-track' feature, that is, undo the
  // last move indefinitely within this logged in session.
  // That is, you can undo moves until the last move you
  // made since you last logged in. Undoing does not cross
  // log in / log out.
  @Test
  public void shouldAllowToBacktrackMoves() {
    CommonPlayerTests.shouldAllowToBacktrackMoves(player);
  }
  
  @Test
  public void shouldNotAllowToBacktrackMovesAcrossSessions() {
    CommonPlayerTests.shouldNotAllowToBacktrackMovesAcrossSessions(player, cave);
  }
  
  // Regression test of bug in servant player, namely that moves to
  // non existing rooms where none the less stacked, leading to
  // backtracks into the very room the player was in
  @Test
  public void shouldNotStackMovesToNonExistingRooms() {
    // move north and then 'hammer your head into the wall'
    // three times. The unsuccessful moves shall not
    // be stacked
    assertThat(player.move(Direction.NORTH), is(true));
    assertThat(player.move(Direction.NORTH), is(false));
    assertThat(player.move(Direction.NORTH), is(false));
    assertThat(player.move(Direction.NORTH), is(false));

    // verify that backtrack moves to entry room and no
    // more backtracks are available
    assertThat(player.backtrack(), is(true));
    assertEquals( "(0,0,0)", player.getPosition());  

    assertThat(player.backtrack(), is(false));
    assertThat(player.backtrack(), is(false));
    assertThat(player.backtrack(), is(false));
  }

  
  
  // Test the toString of login record; not really an essential
  // test, but increases coverage :)
  @Test
  public void shouldValidateLoginResultToString() {
    Login loginResult = cave.login( "mathilde_aarskort", "321");
    assertThat( loginResult.toString(), containsString("(LoginResult: Mathilde/LOGIN_SUCCESS"));
  }
  
  // Test just to increase coverage :)
  @Test
  public void shouldReturnReasonableToString() {
    assertThat(player.toString(), containsString("PlayerServant [storage=FakeCaveStorage"));
    assertThat(player.toString(), containsString("name=Mikkel, ID=user-001, region=AARHUS"));
  }  
}
