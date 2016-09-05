package cloud.cave.client;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Login;
import cloud.cave.domain.Player;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;

/**
 * Testing of the wall behavior on the client side.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */

public class TestWallClient {

  private Player player;
  private LocalMethodCallClientRequestHandler crh;
  private CaveProxy caveProxy;

  @Before
  public void setup() {
    // Create the server tier
    ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();
    
    // create the client request handler as a test double that
    // simply uses method calls to call the 'server side'
    crh = new LocalMethodCallClientRequestHandler(objMgr.getInvoker());
    
    // Create the cave proxy, and login mikkel
    caveProxy = new CaveProxy(crh);
    Login loginResult = caveProxy.login( "mikkel_aarskort", "123");
    
    player = (PlayerProxy) loginResult.getPlayer();

  }

  // TODO: Exercise - starting point for the 'wall' exercise
  @Test
  public void shouldWriteToAndReadWall() {
    player.addMessage("This is message no. 1");
    List<String> wallContents = player.getMessageList();
    assertThat( wallContents.size(), is(1));
    assertThat( wallContents.get(0), containsString("NOT IMPLEMENTED YET"));
  }

}
