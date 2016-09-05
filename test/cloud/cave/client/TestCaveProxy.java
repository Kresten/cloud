package cloud.cave.client;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.*;

import cloud.cave.broker.*;
import cloud.cave.client.CaveProxy;
import cloud.cave.common.CommonCaveTests;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import cloud.cave.doubles.*;

/**
 * Test the Cave proxy which has the ability to log a player in and out of the
 * cave.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class TestCaveProxy {

  private Cave cave;

  private Player p1, p2;

  private ClientRequestHandler crh;

  @Before
  public void setup() {
    // Create the server tier
    ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();

    // ... and the client side
    crh = new LocalMethodCallClientRequestHandler(objMgr.getInvoker());

    // finally the cave proxies using the client request handler...
    cave = new CaveProxy(crh);
  }

  @Test
  public void shouldAllowLoginOfMikkel() {
    // One player
    Login loginResult = cave.login( "mikkel_aarskort", "123");
    // System.out.println(loginResult);
    assertThat( loginResult.getResultCode(), is(LoginResult.LOGIN_SUCCESS));
    Player p1 = loginResult.getPlayer();
    assertNotNull("The returned player object is valid", p1);

    assertEquals( "user-001", p1.getID());
    assertEquals( "Mikkel", p1.getName());
  }
  
  @Test
  public void shouldRejectUnknownSubscriptions() {
    CommonCaveTests.shouldRejectUnknownSubscriptions(cave);
  }
  
  @Test
  public void shouldAllowLoggingOutMagnus() {
    enterBothPlayers();
    CommonCaveTests.shouldAllowLoggingOutMagnus(cave, p1);
  }

  @Test
  public void shouldNotAllowLoggingOutMathildeTwice() {
    enterBothPlayers();
    CommonCaveTests.shouldNotAllowLoggingOutMathildeTwice(cave,p2);
  }

  @Test
  public void shouldWarnIfMathildeLogsInASecondTime() {
    enterBothPlayers();
    CommonCaveTests.shouldWarnIfMathildeLogsInASecondTime(cave);
  }

  private void enterBothPlayers() {
    Login loginResult = cave.login( "magnus_aarskort", "312");
    p1 = loginResult.getPlayer();
    loginResult = cave.login( "mathilde_aarskort", "321");
    p2 = loginResult.getPlayer();
  }
  
  @Test
  public void shouldDescribeConfiguration() {
    String configString = cave.describeConfiguration();
    assertNotNull(configString);
    
    // System.out.println(configString);
    assertThat(configString, containsString("ClientRequestHandler: cloud.cave.doubles.LocalMethodCallClientRequestHandler"));
    assertThat(configString, containsString("CaveStorage: cloud.cave.doubles.FakeCaveStorage"));
    assertThat(configString, containsString("SubscriptionService: cloud.cave.doubles.TestStubSubscriptionService"));
    assertThat(configString, containsString("WeatherService: cloud.cave.doubles.TestStubWeatherService"));
  }
}
