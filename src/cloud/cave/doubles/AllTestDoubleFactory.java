package cloud.cave.doubles;

import cloud.cave.broker.*;
import cloud.cave.common.*;
import cloud.cave.config.*;
import cloud.cave.server.*;
import cloud.cave.service.*;

/**
 * Concrete factory for making making delegates that are all test doubles or a
 * simple implementations (the inspector and player session cache are actually
 * functionally correct implementations.)
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class AllTestDoubleFactory implements CaveServerFactory {

  @Override
  public CaveStorage createCaveStorageConnector(ObjectManager objMgr) {
    CaveStorage storage = new FakeCaveStorage();
    storage.initialize(null, null); // the fake storage needs no external delegates
    return storage;
  }

  @Override
  public SubscriptionService createSubscriptionServiceConnector(ObjectManager objMgr) {
    SubscriptionService service = new TestStubSubscriptionService();
    service.initialize(null, null); // no config object required for the stub
    return service;
  }

  @Override
  public WeatherService createWeatherServiceConnector(ObjectManager objectManager) {
    WeatherService service = new TestStubWeatherService();
    service.initialize(null, null); // no config object required
    return service;
  }

  @Override
  public ServerRequestHandler createServerRequestHandler(ObjectManager objMgr) {
    // The SRH is not presently used in the test cases...
    return new NullServerRequestHandler();
  }

  @Override
  public PlayerSessionCache createPlayerSessionCache(ObjectManager objMgr) {
    PlayerSessionCache cache = new SimpleInMemoryCache();
    cache.initialize(null, null); // no config object required
    return cache;
  }

  @Override
  public Inspector createInspector(ObjectManager objMgr) {
    return new NullInspector();
  }
}
