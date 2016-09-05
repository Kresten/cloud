package cloud.cave.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.cave.broker.*;
import cloud.cave.common.*;
import cloud.cave.server.PlayerSessionCache;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.*;

/**
 * Concrete ServerFactory that creates server side delegates based upon dynamic
 * class loading of classes whose qualified names are defined by a set of
 * properties. After creation, each service delegate is configured through their
 * 'initialize' method with their service end point configuration, again based
 * upon reading their respective properties.
 * 
 * @see Config
 * 
 * @author Henrik Baerbak Christensen, University of Aarhus
 * 
 */
public class StandardServerFactory implements CaveServerFactory {

  private Logger logger;
  private PropertyReaderStrategy propertyReader;

  /**
   * Construct a new server factory, which creates delegates
   * by reading properties from the given reader strategy.
   * 
   * @param envReader
   *          the reader strategy for setting properties
   */
  public StandardServerFactory(PropertyReaderStrategy envReader) {
    logger = LoggerFactory.getLogger(StandardServerFactory.class);
    this.propertyReader = envReader;
  }

  @Override
  public CaveStorage createCaveStorageConnector(ObjectManager objMgr) {
    CaveStorage caveStorage = null; 
    caveStorage = Config.loadAndInstantiate(propertyReader, 
        Config.SKYCAVE_CAVESTORAGE_IMPLEMENTATION, caveStorage);

    // Read in the server configuration
    ServerConfiguration config = 
        new ServerConfiguration(propertyReader, Config.SKYCAVE_DBSERVER);
    caveStorage.initialize(objMgr, config);
    
    logger.info("Creating cave storage with cfg: "+ config);
    
    return caveStorage;
  }

  @Override
  public SubscriptionService createSubscriptionServiceConnector(ObjectManager objMgr) {
    SubscriptionService subscriptionService = null; 
    subscriptionService = Config.loadAndInstantiate(propertyReader, 
        Config.SKYCAVE_SUBSCRIPTION_IMPLEMENTATION, subscriptionService);
    
    // Read in the server configuration
    ServerConfiguration config = 
        new ServerConfiguration(propertyReader, Config.SKYCAVE_SUBSCRIPTIONSERVER);
    subscriptionService.initialize(objMgr, config);

    logger.info("Creating subscription service with cfg: "+ config);

    return subscriptionService;
  }

  @Override
  public WeatherService createWeatherServiceConnector(ObjectManager objMgr) {
    WeatherService weatherService = null; 
    weatherService = Config.loadAndInstantiate(propertyReader, 
        Config.SKYCAVE_WEATHER_IMPLEMENATION, weatherService);
    
    // Read in the server configuration
    ServerConfiguration config = 
        new ServerConfiguration(propertyReader, Config.SKYCAVE_WEATHERSERVER);
    weatherService.initialize(objMgr, config);

    logger.info("Creating weather service with cfg: "+ config);

    return weatherService;
  }

  @Override
  public ServerRequestHandler createServerRequestHandler(ObjectManager objMgr) {
    ServerRequestHandler srh = null; 
    srh = Config.loadAndInstantiate(propertyReader, 
        Config.SKYCAVE_SERVERREQUESTHANDLER_IMPLEMENTATION, srh);

    // Read in the server configuration
    ServerConfiguration config = 
        new ServerConfiguration(propertyReader, Config.SKYCAVE_APPSERVER);
    srh.initialize(objMgr, config);

    logger.info("Creating server request handler with cfg: "+ config);

    return srh;
  }

  @Override
  public PlayerSessionCache createPlayerSessionCache(ObjectManager objMgr) {
    PlayerSessionCache sessionCache = null;
    sessionCache = Config.loadAndInstantiate(propertyReader, 
        Config.SKYCAVE_PLAYERSESSIONCACHE_IMPLEMENTATION, sessionCache);

    // Read in the cache configuration
    ServerConfiguration config = 
        new ServerConfiguration(propertyReader, Config.SKYCAVE_PLAYERSESSIONCACHESERVER);
    sessionCache.initialize(objMgr, config);

    logger.info("Creating player session cache with cfg: "+ config);
    
    return sessionCache;
  }

  @Override
  public Inspector createInspector(ObjectManager objMgr) {
    Inspector inspector = null;
    inspector = Config.loadAndInstantiate(propertyReader, 
        Config.SKYCAVE_INSPECTOR_IMPLEMENTATION, inspector);
    
    // Read in the cache configuration
    ServerConfiguration config = 
        new ServerConfiguration(propertyReader, Config.SKYCAVE_INSPECTORSERVER);
    inspector.initialize(objMgr, config);

    logger.info("Creating inspector with cfg: "+ config);

    return inspector;
  }

}
