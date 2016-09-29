package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import cloud.cave.common.Inspector;
import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.doubles.*;
import cloud.cave.server.common.FakeTimeStrategy;
import cloud.cave.server.common.TimeStrategy;
import cloud.cave.service.RealWeatherServiceWithCircuitBreaker;
import cloud.cave.service.RealWeatherServiceWithTimeout;
import org.json.simple.JSONObject;
import org.junit.*;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.*;
import cloud.cave.service.WeatherService;

/**
 * TDD Implementation of the weather stuff - initial steps.
 *
 * @author Henrik Baerbak Christensen, Aarhus University
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
        assertThat(json.get("authenticated"), is(false));
        assertThat(json.get("errorMessage").toString(), is("GroupName grp02 or playerID user-003 is not authenticated"));

        // Try it using the full api
        Login loginResult = cave.login("mathilde_aarskort", "321");
        player = loginResult.getPlayer();
        assertNotNull("The player should have been logged in", player);

        String weather = player.getWeather();
        assertThat(weather, containsString("The weather service failed with message:\nGroupName grp02 or playerID user-003 is not authenticated"));
    }

    @Test
    public void shouldThrowConnectionException() {
        TestStubWeatherService testStubWeatherService = new TestStubWeatherService();
        testStubWeatherService.setState(TestStubWeatherServiceState.EXCEPTION);
        CaveServerFactory factory = new AllTestDoubleFactory() {
            @Override
            public WeatherService createWeatherServiceConnector(ObjectManager objMgr) {
                WeatherService service = new RealWeatherServiceWithTimeout(testStubWeatherService);
                service.initialize(null, null); // no config object required
                return service;
            }
        };
        ObjectManager objMgr = new StandardObjectManager(factory);
        cave = new CaveServant(objMgr);
        loginName = "mikkel_aarskort";
        Login loginResult = cave.login(loginName, "123");
        player = loginResult.getPlayer();
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. Connection timeout. Try again later. ***"));
    }

    @Test
    public void shouldTripCircuitBreaker() {
        TestStubWeatherService testStubWeatherService = new TestStubWeatherService();
        testStubWeatherService.setState(TestStubWeatherServiceState.EXCEPTION);
        CaveServerFactory factory = new AllTestDoubleFactory() {
            @Override
            public WeatherService createWeatherServiceConnector(ObjectManager objMgr) {
                WeatherService service = new RealWeatherServiceWithCircuitBreaker(testStubWeatherService);
                return service;
            }
        };
        ObjectManager objMgr = new StandardObjectManager(factory);
        objMgr.getWeatherService().initialize(objMgr,null);
        cave = new CaveServant(objMgr);
        loginName = "mikkel_aarskort";
        Login loginResult = cave.login(loginName, "123");
        player = loginResult.getPlayer();
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Closed Circuit) ***"));
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Closed Circuit) ***"));
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Closed Circuit) ***"));
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Open Circuit) ***"));
    }

    @Test
    public void shouldTripCircuitBreakerAndCloseAndOpen() {
        TestStubWeatherService testStubWeatherService = new TestStubWeatherService();
        testStubWeatherService.setState(TestStubWeatherServiceState.EXCEPTION);
        FakeTimeStrategy fakeTimeStrategy = new FakeTimeStrategy();
        CaveServerFactory factory = new AllTestDoubleFactory() {
            @Override
            public WeatherService createWeatherServiceConnector(ObjectManager objMgr) {
                WeatherService service = new RealWeatherServiceWithCircuitBreaker(testStubWeatherService, fakeTimeStrategy, 0.5);
                return service;
            }
        };
        ObjectManager objMgr = new StandardObjectManager(factory);
        objMgr.getWeatherService().initialize(objMgr,null);
        cave = new CaveServant(objMgr);
        loginName = "mikkel_aarskort";
        Login loginResult = cave.login(loginName, "123");
        player = loginResult.getPlayer();
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Closed Circuit) ***"));
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Closed Circuit) ***"));
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Closed Circuit) ***"));
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Open Circuit) ***"));
        fakeTimeStrategy.incrementTime();
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Closed Circuit) ***"));
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Open Circuit) ***"));
    }

    @Test
    public void shouldWorkOnCorrectInputButTripCircuitBreakerOnExceptions() {
        TestStubWeatherService testStubWeatherService = new TestStubWeatherService();
        FakeTimeStrategy fakeTimeStrategy = new FakeTimeStrategy();
        CaveServerFactory factory = new AllTestDoubleFactory() {
            @Override
            public WeatherService createWeatherServiceConnector(ObjectManager objMgr) {
                WeatherService service = new RealWeatherServiceWithCircuitBreaker(testStubWeatherService, fakeTimeStrategy, 0.5);
                return service;
            }
        };
        ObjectManager objMgr = new StandardObjectManager(factory);
        objMgr.getWeatherService().initialize(objMgr,null);
        cave = new CaveServant(objMgr);
        loginName = "mikkel_aarskort";
        Login loginResult = cave.login(loginName, "123");
        player = loginResult.getPlayer();
        assertThat(player.getWeather(), containsString("The weather in AARHUS is Clear, temperature 27.4C (feelslike -2.7C). Wind: 1.2 m/s, direction West."));
        assertThat(player.getWeather(), containsString("This report is dated: Thu, 05 Mar 2015 09:38:37 +0100"));
        testStubWeatherService.setState(TestStubWeatherServiceState.EXCEPTION);
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Closed Circuit) ***"));
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Closed Circuit) ***"));
        testStubWeatherService.setState(TestStubWeatherServiceState.JSON);
        assertThat(player.getWeather(), containsString("The weather in AARHUS is Clear, temperature 27.4C (feelslike -2.7C). Wind: 1.2 m/s, direction West."));
        assertThat(player.getWeather(), containsString("This report is dated: Thu, 05 Mar 2015 09:38:37 +0100"));
        testStubWeatherService.setState(TestStubWeatherServiceState.EXCEPTION);
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Closed Circuit) ***"));
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Closed Circuit) ***"));
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Closed Circuit) ***"));
        assertThat(player.getWeather(), containsString("*** Weather service not available, sorry. (Open Circuit) ***"));
        fakeTimeStrategy.incrementTime();
        testStubWeatherService.setState(TestStubWeatherServiceState.JSON);
        assertThat(player.getWeather(), containsString("The weather in AARHUS is Clear, temperature 27.4C (feelslike -2.7C). Wind: 1.2 m/s, direction West."));
        assertThat(player.getWeather(), containsString("This report is dated: Thu, 05 Mar 2015 09:38:37 +0100"));
    }
}
