package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.json.simple.JSONObject;
import org.junit.*;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.*;
import cloud.cave.doubles.TestStubWeatherService;
import cloud.cave.service.WeatherService;

/**
 * TDD Implementation of the weather stuff - initial steps.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class TestWeather {

  private Cave cave;
  private String loginName;
  private Player player;

  @Before
  public void setUp() throws Exception {
    cave = CommonCaveTests.createTestDoubledConfiguredCave().getCave();
    loginName = "mikkel_aarskort";
    Login loginResult = cave.login(loginName, "123");
    player = loginResult.getPlayer();
  }

  @Test
  public void shouldGetWeatherServerSide() {
    String weather = player.getWeather();
    assertThat(weather, containsString("The weather in AARHUS is Clear, temperature 27.4C (feelslike -2.7C). Wind: 1.2 m/s, direction West."));
    assertThat(weather, containsString("This report is dated: Thu, 05 Mar 2015 09:38:37 +0100"));
  }
  
  @Test
  public void shouldRejectUnknownPlayer() {
    // Test the raw weather service api for unknown players
    WeatherService ws = new TestStubWeatherService();
    JSONObject json = ws.requestWeather("grp02", "user-003", Region.COPENHAGEN);
    assertThat(json.get("authenticated").toString(), is("false"));
    assertThat(json.get("errorMessage").toString(), is("GroupName grp02 or playerID user-003 is not authenticated"));
    
    // Try it using the full api
    Login loginResult = cave.login( "mathilde_aarskort", "321");
    player = loginResult.getPlayer();
    assertNotNull("The player should have been logged in", player);
    
    String weather = player.getWeather();
    assertThat(weather, containsString("The weather service failed with message:\nGroupName grp02 or playerID user-003 is not authenticated"));
  }
}
