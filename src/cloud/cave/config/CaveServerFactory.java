package cloud.cave.config;

import cloud.cave.broker.*;
import cloud.cave.common.Inspector;
import cloud.cave.server.PlayerSessionCache;
import cloud.cave.service.*;

/**
 * Abstract factory (FRS, page 217) interface for creating delegates for the
 * server side cave. For production, use the implementation based
 * upon reading Chained Property Files.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public interface CaveServerFactory {

  /**
   * Create and return an initialized connector to the cave storage, the binding
   * to the database system that holds all data related to the cave: players,
   * rooms, etc.
   * <p>
   * In case of an external storage system (a database connection) the factory
   * will return a fully initialized and open connection.
   * 
   * @param objectManager the system wide manager of delegates
   * 
   * @return a binding to the storage system
   */
  CaveStorage createCaveStorageConnector(ObjectManager objectManager);

  /**
   * Create and return an initialized connector to the subscription service.
   * 
   * @param objectManager the system wide manager of delegates
   * 
   * @return a connector to the subscription service
   */
  SubscriptionService createSubscriptionServiceConnector(ObjectManager objectManager);

  /**
   * Create and return an initialized connector to the weather service.
   * 
   * @param objectManager the system wide manager of delegates
   *
   * @return a connector to the weather service
   */
  WeatherService createWeatherServiceConnector(ObjectManager objectManager);

  /**
   * Create and return the server request handler object that binds the server invoker
   * to the particular OS and the IPC system chosen.
   * 
   * @param objectManager
   *          the objectManager that holds all delegates
   * 
   * @return the server request handler
   */
  ServerRequestHandler createServerRequestHandler(ObjectManager objectManager);

  /**
   * Create and return an initialized player session cache.
   * 
   * @param objectManager
   *          the objectManager that holds all delegates
   *          
   * @return the session cache for the player
   */
  PlayerSessionCache createPlayerSessionCache(ObjectManager objectManager);

  /**
   * Create and return an initialized inspector instance.
   * 
   * @param objectManager
   *          the objectManager that holds all delegates
   *
   * @return the inspector
   */
  Inspector createInspector(ObjectManager objectManager);

}
