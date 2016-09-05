package cloud.cave.doubles;

import cloud.cave.broker.Invoker;
import cloud.cave.common.Inspector;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.server.PlayerSessionCache;
import cloud.cave.service.*;

/** Null Object implementation, used in a few test cases.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class NullObjectManager implements ObjectManager {

  @Override
  public Cave getCave() {
    return null;
  }

  @Override
  public Runnable getServerRequestHandler() {
    return null;
  }

  @Override
  public CaveStorage getCaveStorage() {
    return null;
  }

  @Override
  public SubscriptionService getSubscriptionService() {
    return null;
  }

  @Override
  public WeatherService getWeatherService() {
    return null;
  }

  @Override
  public PlayerSessionCache getPlayerSessionCache() {
    return null;
  }

  @Override
  public Inspector getInspector() {
    return null;
  }

  @Override
  public Invoker getInvoker() {
    return null;
  }

}
