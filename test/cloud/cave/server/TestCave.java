package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.*;

import cloud.cave.common.*;
import cloud.cave.domain.*;

/** Test cases for the server side implementation of the
 * Cave. Heavy use of test doubles to avoid all dependencies
 * to external services.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestCave {
  
  private Cave cave;
  
  private Player p1, p2;

  @Before
  public void setup() {
    cave = CommonCaveTests.createTestDoubledConfiguredCave().getCave();
  }

  @Test
  public void shouldAllowAddingPlayers() {
    CommonCaveTests.shouldAllowAddingPlayers(cave);
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
    assertThat(configString, containsString("CaveStorage: cloud.cave.doubles.FakeCaveStorage"));
    assertThat(configString, containsString("SubscriptionService: cloud.cave.doubles.TestStubSubscriptionService"));
    assertThat(configString, containsString("WeatherService: cloud.cave.doubles.TestStubWeatherService"));
  }
}
