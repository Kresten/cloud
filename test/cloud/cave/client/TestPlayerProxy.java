package cloud.cave.client;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.junit.*;

import cloud.cave.broker.*;
import cloud.cave.common.*;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import cloud.cave.doubles.*;

/**
 * Test that the PlayerClientProxy allows all behavior defined by the Player
 * interface to be successfully communicated with the server tier.
 * <p>
 * Note: Most of these tests are naturally identical for the tests of the server
 * player implementation.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class TestPlayerProxy {

  private PlayerProxy playerProxy;
  private ClientRequestHandler crh;
  private Cave caveProxy;
  
  @Before
  public void setUp() throws Exception {
    // Create the server tier
    ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();
    
    // create the client request handler as a test double that
    // simply uses method calls to call the 'server side'
    crh = new LocalMethodCallClientRequestHandler(objMgr.getInvoker());
    
    // Create the cave proxy, and login mikkel
    caveProxy = new CaveProxy(crh);
    Login loginResult = caveProxy.login( "mikkel_aarskort", "123");
    
    playerProxy = (PlayerProxy) loginResult.getPlayer();
  }

  @Test
  public void shouldAccessSimpleAttributes() {
    CommonPlayerTests.shouldAccessSimpleAttributes(playerProxy);
  }

  @Test
  public void shouldAllowMovingEast() {
    boolean isLegal = playerProxy.move(Direction.EAST);
    JSONObject req = playerProxy.lastSentRequestObject();
    assertEquals( 36, req.get(MarshalingKeys.PLAYER_SESSION_ID_KEY).toString().length());
    assertEquals( MarshalingKeys.MOVE_METHOD_KEY, req.get(MarshalingKeys.METHOD_KEY));
    assertEquals( "EAST", req.get(MarshalingKeys.PARAMETER_HEAD_KEY));
    
    assertTrue( "Moving east should be legal", isLegal);
  }
  
  @Test
  public void shouldGetShortRoomDescription() {
    String description = playerProxy.getShortRoomDescription();
    assertTrue("Initial location missing proper description",
        description.contains("You are standing at the end of a road before a small brick building."));
  }

  @Test
  public void shouldGetPosition() {
    CommonPlayerTests.shouldTestCoordinateTranslations(playerProxy);
  }

  @Test
  public void shouldHandleRemoteMoveAndDescription() {
    // move east 
    shouldAllowMovingEast();
    String description = playerProxy.getShortRoomDescription();
    // System.out.println(description);
    assertTrue("The (remote) move has not moved the player",
        description.contains("You are inside a building, a well house for a large spring."));
  }

  @Test
  public void shouldProvideLongDescription() {
    String longDescription = playerProxy.getLongRoomDescription();

    assertTrue(longDescription.contains("There are exits in"));
    assertTrue(longDescription.contains("NORTH"));
    assertTrue(longDescription.contains("WEST"));
    assertTrue(longDescription.contains("EAST"));
    assertTrue(!longDescription.contains("SOUTH"));

    assertThat(longDescription, containsString("You see other players:"));
    assertThat(longDescription, containsString("[0] Mikkel"));
  }

  @Test
  public void shouldAllowPlayerToDigNewRooms() {
    CommonPlayerTests.shouldAllowPlayerToDigNewRooms(playerProxy);
  }
  
  @Test
  public void shouldNotAllowDigAtEast() {
    CommonPlayerTests.shouldNotAllowDigAtEast(playerProxy);
  }
  
  @Test
  public void shouldShowExitsForPlayersPosition() {
    CommonPlayerTests.shouldShowExitsForPlayersPosition(playerProxy);
  }

  @Test
  public void shouldSeePlayersInRoom() {
    CommonPlayerTests.shouldSeeMathildeComingInAndOutOfRoomDuringSession(caveProxy, playerProxy);
  }
  
  @Test
  public void shouldShowValidExitsFromEntryRoom() {
    CommonPlayerTests.shouldGetProperExitSet(playerProxy);
  }

  /** Try to make an illformed request (not using the proxy)
   * and ensure that the server invoker makes an
   * appropriate reply.
   * @throws IOException 
   */
  @Test
  public void shouldReplyWithErrorInCaseRequestIsMalformed() throws CaveIPCException {
    JSONObject invalidRequest = 
        Marshaling.createRequestObject(playerProxy.getID(), playerProxy.getSessionID(), "invalid-method-key", "my best parameter");
    JSONObject reply = null;
    
    reply = crh.sendRequestAndBlockUntilReply(invalidRequest);
    
    String errorCode = reply.get(MarshalingKeys.ERROR_CODE_KEY).toString();
    
    assertThat(errorCode, is(StatusCode.SERVER_UNKNOWN_METHOD_FAILURE));
   
    assertThat( //"The invalid request should tell where the error is",
        reply.get(MarshalingKeys.ERROR_MSG_KEY).toString(),
        containsString("StandardInvoker.handleRequest: Unhandled request as the method key invalid-method-key is unknown."));
  }
  
  @Test
  public void shouldBeAtPositionOfLastLogout() {
    // Log mathilde into the cave, initial position is 0,0,0
    // as the database is reset
    Login loginResult = caveProxy.login( "mathilde_aarskort", "321");
    Player p1 = loginResult.getPlayer();

    CommonPlayerTests.shouldBeAtPositionOfLastLogout(caveProxy, p1);
  }
  
  @Test
  public void shouldValidateToString() {
    assertThat( playerProxy.toString(), is("(PlayerClientProxy: user-001/Mikkel)"));
  }
  
  @Test
  public void shouldHaveSessionIdAssigned() {
    // The session id is a UUID which is 36 characters long.
    assertThat(playerProxy.getSessionID().length(), is(36));
  }
  
  @Test
  public void shouldAllowToBacktrackMoves() {
    CommonPlayerTests.shouldAllowToBacktrackMoves(playerProxy);
  }
  
  @Test
  public void shouldNotAllowToBacktrackMovesAcrossSessions() {
    CommonPlayerTests.shouldNotAllowToBacktrackMovesAcrossSessions(playerProxy, caveProxy);
  }

  Player p1, p2;
  
  private void enterBothPlayers() {
    Login loginResult = caveProxy.login( "magnus_aarskort", "312");
    p1 = loginResult.getPlayer();
    
    loginResult = caveProxy.login( "mathilde_aarskort", "321");
    p2 = loginResult.getPlayer();
  }

  // Test that if a second client connects using the
  // same credentials as a first client is already
  // connected with, then the first client is
  // prevented from any actions ("disconnected" in
  // a sense). This is similar to the behavior of
  // Blizzard games (which is probably the standard).
  
  @Test
  public void shouldPreventCallsFromDualLogins() {
    enterBothPlayers();
    p2.move(Direction.EAST);

    // log in Mathilde a second time
    Login loginResult = caveProxy.login( "mathilde_aarskort", "321");
    assertThat( loginResult.getResultCode(), is(LoginResult.LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN));
    
    Player p2second = loginResult.getPlayer();
    
    // just precautions - we have two objects representing same player, right?
    assertNotEquals( p2, p2second);
    
    // Verify that the second client logged in is in the same
    // room as the the first client moved to
    assertThat( p2second.getPosition(), is("(1,0,0)"));
    
    // Verify that the first client CANNOT move mathilde west even
    // though the topology of the cave would allow it, instead
    // throws an Exception
    try {
      p2.move(Direction.WEST);
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThat( e.getMessage(), containsString("The session for player user-003 is no longer valid"));
    }
    
    // assert that the second session IS allowed to do it
    assertThat( p2second.move(Direction.WEST), is(true));
    
    // Verify the other methods that do not cache things locally - sigh - cannot avoid a lot of duplicated code...
    try {
      p2.getPosition();
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThat( e.getMessage(), containsString("The session for player user-003 is no longer valid"));
    }

    try {
      p2.getShortRoomDescription();
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThat( e.getMessage(), containsString("The session for player user-003 is no longer valid"));
    }

    try {
      p2.getLongRoomDescription();
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThat( e.getMessage(), containsString("The session for player user-003 is no longer valid"));
    }

    try {
      p2.getExitSet();
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThat( e.getMessage(), containsString("The session for player user-003 is no longer valid"));
    }

    try {
      p2.getPlayersHere();
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThat( e.getMessage(), containsString("The session for player user-003 is no longer valid"));
    }

    try {
      p2.getRegion();
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThat( e.getMessage(), containsString("The session for player user-003 is no longer valid"));
    }

    try {
      p2.digRoom(Direction.DOWN, "You are in the wonder room");
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThat( e.getMessage(), containsString("The session for player user-003 is no longer valid"));
    }
    try {
      p2.execute("HomeCommand", "");
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThat( e.getMessage(), containsString("The session for player user-003 is no longer valid"));
    }
  }
}
