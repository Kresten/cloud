package cloud.cave.config;

import cloud.cave.broker.Invoker;
import cloud.cave.common.Inspector;
import cloud.cave.domain.Cave;
import cloud.cave.server.PlayerSessionCache;
import cloud.cave.service.*;

/**
 * The Object Manager is responsible for providing a system wide lookup service
 * for central roles in the system, a 'yellow-pages' or 'dns'. This way only an
 * instance of object manager is needed to be passed along between roles, as all
 * relevant delegates can be accessed by requesting them.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public interface ObjectManager {

  /** Return the cave instance.
   * 
   * @return the cave
   */
  Cave getCave();

  /** Return the server request handler
   * 
   * @return server request handler
   */
  Runnable getServerRequestHandler();

  /** Return the cave storage / the database connector
   * 
   * @return cave storage
   */
  CaveStorage getCaveStorage();

  /** Return the subscription service connector
   * 
   * @return subscription service
   */
  SubscriptionService getSubscriptionService();

  /** Return the weather service connector
   * 
   * @return weather service
   */
  WeatherService getWeatherService();

  /** Return the session cache for players
   * 
   * @return player session cache
   */
  PlayerSessionCache getPlayerSessionCache();

  /** Return the inspector
   * 
   * @return inspector
   */
  Inspector getInspector();

  /** Return the invoker on the server side
   * 
   * @return invoker
   */
  Invoker getInvoker();

}
