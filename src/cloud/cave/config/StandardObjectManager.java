package cloud.cave.config;

import cloud.cave.broker.*;
import cloud.cave.common.Inspector;
import cloud.cave.domain.Cave;
import cloud.cave.server.*;
import cloud.cave.service.*;

public class StandardObjectManager implements ObjectManager {

  private CaveServant caveServant;
  private StandardInvoker serverInvoker;
  private ServerRequestHandler serverRequestHandler;

  private CaveStorage storage;
  private SubscriptionService subscriptionService;
  private WeatherService weatherService;
  private PlayerSessionCache sessionCache;
  private Inspector inspector;

  public StandardObjectManager(CaveServerFactory factory) {
    // Create the inspector that records internal daemon state
    inspector = factory.createInspector(this);

    // Create database connection
    storage = factory.createCaveStorageConnector(this);
    
    // Create connector to subscription service
    subscriptionService = factory
        .createSubscriptionServiceConnector(this);

    // Create connector to weather service
    weatherService = factory.createWeatherServiceConnector(this);
    
    // Create connector to session cache for player
    sessionCache = factory.createPlayerSessionCache(this);
    
    // Create the server side cave instance
    caveServant = new CaveServant(this);

    // Create the invoker on the server side, and bind it to the cave
    serverInvoker = new StandardInvoker(this);
    
    // Create the server side SRH... 
    serverRequestHandler = factory.createServerRequestHandler(this); 

    logAllCfgInInspector();
  }

  /**
   * Write a log in the inspector under topic CFG_TOPIC on the configuration of
   * all delegates / injected dependencies.
   */
  private void logAllCfgInInspector() {
    inspector.write(Inspector.CFG_TOPIC, "  CaveStorage: "+ storage.getClass().getName());
    inspector.write(Inspector.CFG_TOPIC, "   - cfg: "+storage.getConfiguration());
    
    inspector.write(Inspector.CFG_TOPIC, "  SubscriptionService: "+ subscriptionService.getClass().getName());
    inspector.write(Inspector.CFG_TOPIC, "   - cfg: "+subscriptionService.getConfiguration());

    inspector.write(Inspector.CFG_TOPIC, "  WeatherService: "+ weatherService.getClass().getName());
    inspector.write(Inspector.CFG_TOPIC, "   - cfg: "+weatherService.getConfiguration());

    inspector.write(Inspector.CFG_TOPIC, "  PlayerSessionCache: "+ sessionCache.getClass().getName());
    inspector.write(Inspector.CFG_TOPIC, "   - cfg: "+sessionCache.getConfiguration());

    inspector.write(Inspector.CFG_TOPIC, "  Inspector: "+ inspector.getClass().getName());
    inspector.write(Inspector.CFG_TOPIC, "   - cfg: "+inspector.getConfiguration());

    inspector.write(Inspector.CFG_TOPIC, "  ServerRequestHandler: "+ serverRequestHandler.getClass().getName());
  }

  @Override
  public Cave getCave() {
    return caveServant;
  }

  @Override
  public Runnable getServerRequestHandler() {
    return serverRequestHandler;
  }

  @Override
  public CaveStorage getCaveStorage() {
    return storage;
  }

  @Override
  public SubscriptionService getSubscriptionService() {
    return subscriptionService;
  }

  @Override
  public WeatherService getWeatherService() {
    return weatherService;
  }

  @Override
  public PlayerSessionCache getPlayerSessionCache() {
    return sessionCache;
  }

  @Override
  public Inspector getInspector() {
    return inspector;
  }

  @Override
  public Invoker getInvoker() {
    return serverInvoker;
  }

}
