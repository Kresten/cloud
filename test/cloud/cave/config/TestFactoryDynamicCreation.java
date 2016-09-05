package cloud.cave.config;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.*;

import cloud.cave.broker.*;
import cloud.cave.common.*;
import cloud.cave.doubles.*;
import cloud.cave.server.PlayerSessionCache;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.*;

/**
 * Validate the ServerFactory's ability to read in the properties and create
 * correctly configured delegates based upon their values.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestFactoryDynamicCreation {
  
  CaveServerFactory factory; 
  StubPropertyReaderStrategy envReader;

  @Before
  public void setup() {
    envReader = new StubPropertyReaderStrategy();
    factory = new StandardServerFactory(envReader);
  }

  @Test
  public void shouldCreateProperCaveInstances() {
    envReader.setNextExpectation(Config.SKYCAVE_CAVESTORAGE_IMPLEMENTATION, 
        "cloud.cave.doubles.FakeCaveStorage");
    envReader.setNextExpectation(Config.SKYCAVE_DBSERVER, 
        "192.168.237.130:27017");
    CaveStorage storage = factory.createCaveStorageConnector(null);
    assertThat(storage.toString(), containsString("FakeCaveStorage"));
    
    ServerConfiguration config = storage.getConfiguration();
    assertNotNull("The initialization must assign a cave storage configuration.", config);
    assertThat(config.get(0).getHostName(), is("192.168.237.130"));
    assertThat(config.get(0).getPortNumber(), is(27017));
  }

  @Test
  public void shouldCreateProperSubscriptionInstances() {
    envReader.setNextExpectation(Config.SKYCAVE_SUBSCRIPTION_IMPLEMENTATION, 
        "cloud.cave.doubles.TestStubSubscriptionService");
    envReader.setNextExpectation(Config.SKYCAVE_SUBSCRIPTIONSERVER, 
        "subscription.baerbak.com:42042");
    SubscriptionService service = factory.createSubscriptionServiceConnector(null);
    assertThat(service.toString(), containsString("TestStubSubscriptionService"));
    ServerConfiguration config = service.getConfiguration();
    assertNotNull("The initialization must assign a subscription service configuration.", config);
    assertThat(config.get(0).getHostName(), is("subscription.baerbak.com"));
    assertThat(config.get(0).getPortNumber(), is(42042));
  }

  @Test
  public void shouldCreateProperServerRequestHandlerInstances() {
    envReader.setNextExpectation(Config.SKYCAVE_SERVERREQUESTHANDLER_IMPLEMENTATION,
        "cloud.cave.config.socket.SocketServerRequestHandler");
    envReader.setNextExpectation(Config.SKYCAVE_APPSERVER,
        "localhost:37123");
    ObjectManager objMgr = new NullObjectManager();
    ServerRequestHandler srh = factory.createServerRequestHandler(objMgr);
    assertThat(srh.toString(), containsString("SocketServerRequestHandler. Assigned to port: 37123"));
  }

  @Test
  public void shouldCreateProperClientRequestHandler() {
    envReader.setNextExpectation(Config.SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION,
        "cloud.cave.config.socket.SocketClientRequestHandler");
    envReader.setNextExpectation(Config.SKYCAVE_APPSERVER,
        "skycave.mycompany.com:37123");
    
    CaveClientFactory factory = new StandardClientFactory(envReader);
    ClientRequestHandler crh = factory.createClientRequestHandler();
    assertThat(crh.toString(), containsString("AppServer Cfg: skycave.mycompany.com:37123."));
  }

  @Test
  public void shouldCreateProperCaveReplicaSet() {
    envReader.setNextExpectation(Config.SKYCAVE_CAVESTORAGE_IMPLEMENTATION, 
        "cloud.cave.doubles.FakeCaveStorage");
    envReader.setNextExpectation(Config.SKYCAVE_DBSERVER, 
        "192.168.237.130:27017,192.168.237.131:27018,192.168.237.132:27019");
    CaveStorage storage = factory.createCaveStorageConnector(null);
    assertThat(storage.toString(), containsString("FakeCaveStorage"));
    
    ServerConfiguration config = storage.getConfiguration();
    assertNotNull("The initialization must assign a cave storage configuration.", config);
    assertThat(config.get(0).getHostName(), is("192.168.237.130"));
    assertThat(config.get(0).getPortNumber(), is(27017));
    
    assertThat(config.get(1).getHostName(), is("192.168.237.131"));
    assertThat(config.get(1).getPortNumber(), is(27018));
    
    assertThat(config.get(2).getHostName(), is("192.168.237.132"));
    assertThat(config.get(2).getPortNumber(), is(27019));
    
    assertThat(config.size(), is(3));
  }

  @Test
  public void shouldCreateProperPlayerSessionCache() {
    envReader.setNextExpectation(Config.SKYCAVE_PLAYERSESSIONCACHE_IMPLEMENTATION,
        "cloud.cave.server.SimpleInMemoryCache");
    envReader.setNextExpectation(Config.SKYCAVE_PLAYERSESSIONCACHESERVER, 
        "10.11.82.10:11211,10.11.82.12:11211");
    
    PlayerSessionCache cache = factory.createPlayerSessionCache(null);
    assertThat(cache.toString(), containsString("SimpleInMemoryCache"));
    
    ServerConfiguration config = cache.getConfiguration();
    assertNotNull("The initialization must assign a player cache configuration.", config);
    assertThat(config.get(0).getHostName(), is("10.11.82.10"));
    assertThat(config.get(0).getPortNumber(), is(11211));
    
    assertThat(config.get(1).getHostName(), is("10.11.82.12"));
    assertThat(config.get(1).getPortNumber(), is(11211));
  }
  
  @Test
  public void shouldCreateProperInspector() {
    envReader.setNextExpectation(Config.SKYCAVE_INSPECTOR_IMPLEMENTATION,
        "cloud.cave.server.SimpleInspector");
    envReader.setNextExpectation(Config.SKYCAVE_INSPECTORSERVER, 
        "localhost:27711");
    
    Inspector cache = factory.createInspector(null);
    assertThat(cache.toString(), containsString("SimpleInspector"));
    
    ServerConfiguration config = cache.getConfiguration();
    assertNotNull("The initialization must assign an Inspector configuration.", config);
    assertThat(config.get(0).getHostName(), is("localhost"));
    assertThat(config.get(0).getPortNumber(), is(27711));
    
  }

  @Test(expected=CaveClassNotFoundException.class)
  public void shouldThrowExceptionForNonExistingCaveClass() {
    envReader = new StubPropertyReaderStrategy();
    envReader.setNextExpectation(Config.SKYCAVE_CAVESTORAGE_IMPLEMENTATION,
        "cloud.cave.doubles.SuperDuperNonExistingClass");
    factory = new StandardServerFactory(envReader);
    @SuppressWarnings("unused")
    ExternalService storage = factory.createCaveStorageConnector(null);
  }

  @Test(expected=CaveConfigurationNotSetException.class)
  public void shouldThrowExceptionIfEnvVarNotSet() {
    envReader = new StubPropertyReaderStrategy();
    envReader.setNextExpectation(Config.SKYCAVE_CAVESTORAGE_IMPLEMENTATION,
        null);
    factory = new StandardServerFactory(envReader);
    @SuppressWarnings("unused")
    ExternalService storage = factory.createCaveStorageConnector(null);
  }

  @Test(expected=CaveConfigurationNotSetException.class)
  public void shouldThrowExceptionIfIndexError() {
    envReader.setNextExpectation(Config.SKYCAVE_CAVESTORAGE_IMPLEMENTATION, 
        "cloud.cave.doubles.FakeCaveStorage");
    envReader.setNextExpectation(Config.SKYCAVE_DBSERVER, 
        "192.168.237.130:27017,192.168.237.131:27018,192.168.237.132:27019");
    CaveStorage storage = factory.createCaveStorageConnector(null);
    
    ServerConfiguration config = storage.getConfiguration();
    config.get(4);
  }
  
  @Test
  public void shouldCreateProperWeatherInstances() {
    envReader.setNextExpectation(Config.SKYCAVE_WEATHER_IMPLEMENATION, 
        "cloud.cave.doubles.TestStubWeatherService");
    envReader.setNextExpectation(Config.SKYCAVE_WEATHERSERVER, 
        "weather.baerbak.com:8182");
    WeatherService service = factory.createWeatherServiceConnector(null);
    assertThat(service.toString(), containsString("TestStubWeatherService"));
    ServerConfiguration config = service.getConfiguration();
    assertNotNull("The initialization must assign a weather service configuration.", config);
    assertThat(config.get(0).getHostName(), is("weather.baerbak.com"));
    assertThat(config.get(0).getPortNumber(), is(8182));
  }
  
  @Test
  public void shouldIncreaseCoverage() {
    // not really fun except for increasing the amount of green
    // paint in jacoco
    ServerConfiguration cfg = new ServerConfiguration("www.baerbak.com", 12345);
    assertThat(cfg.get(0).getHostName(), is("www.baerbak.com"));
    assertThat(cfg.get(0).getPortNumber(), is(12345));
    
    assertThat(cfg.toString(), containsString("www.baerbak.com:12345"));
  }
}
