package cloud.cave.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import cloud.cave.config.*;
import cloud.cave.domain.*;
import cloud.cave.doubles.AllTestDoubleFactory;

public class CommonCaveTests {

  public static ObjectManager createTestDoubledConfiguredCave() {
    CaveServerFactory factory = new AllTestDoubleFactory();
    ObjectManager objMgr = new StandardObjectManager(factory);
    return objMgr;
  }

  public static void shouldAllowAddingPlayers(Cave cave) {
    // One player
    Login loginResult = cave.login( "magnus_aarskort", "312");
    Player p1 = loginResult.getPlayer();
    assertNotNull(p1);
    assertEquals( "user-002", p1.getID());
    assertThat( p1.getName(), is("Magnus"));
    assertThat( p1.getRegion(), is(Region.COPENHAGEN));

    // Enter Mathilde
    loginResult = cave.login( "mathilde_aarskort", "321");
    Player p2 = loginResult.getPlayer();
    assertNotNull(p2);
    assertEquals( "user-003", p2.getID());
    assertThat( p2.getName(), is("Mathilde"));
  }

  public static void shouldAllowLoggingOutMagnus(Cave cave, Player p1) {
    // log out p1
    LogoutResult result = cave.logout(p1.getID());
    assertNotNull("The result of the logout is null", result);
    assertEquals(LogoutResult.SUCCESS, result);
  }

  public static void shouldNotAllowLoggingOutMathildeTwice(Cave cave, Player p2) {
    // log out Mathilde
    LogoutResult result = cave.logout(p2.getID());
    assertEquals(LogoutResult.SUCCESS, result);
    
    result = cave.logout(p2.getID());
    assertEquals(LogoutResult.PLAYER_NOT_IN_CAVE, result);
  }

  public static void shouldWarnIfMathildeLogsInASecondTime(Cave cave) {
    // Try to login mathilde a second time
    Login loginResult = cave.login( "mathilde_aarskort", "321");
    // The login should be successfull but a warning should be issued of potentially
    // more than one client operating the player
    assertEquals(LoginResult.LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN, loginResult.getResultCode() );
    Player p = loginResult.getPlayer();
    assertTrue("A valid player object should be returned even if a second login is made", 
        p != null);
    assertThat( p.getID(), is("user-003"));
  }

  public static void shouldRejectUnknownSubscriptions(Cave cave) {
    Login loginResult = cave.login( "bandit@cs.au.dk", "wrongkey");
    assertEquals( LoginResult.LOGIN_FAILED_UNKNOWN_SUBSCRIPTION, loginResult.getResultCode() );

    loginResult = cave.login( "magnus_aarskort", "wrongkey");
    assertNotNull("A login result must always be returned.", loginResult);
    assertEquals( LoginResult.LOGIN_FAILED_UNKNOWN_SUBSCRIPTION, loginResult.getResultCode() );
  }

}
