package cloud.cave.manual;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;

/**
 * A Null Object Subscription service; all logins are granted.
 * <p>
 * Of course, this is NOT the implementation to use for production; but it is
 * ideal for load testing where a lot of users needs to be logged into the
 * server with as little fuss as possible.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class NullSubscriptionService implements SubscriptionService {
  
  public NullSubscriptionService() {
  }
  
  @Override
  public SubscriptionRecord lookup(String loginName, String password) {
    String playerID = "id-"+loginName;
    SubscriptionRecord record = new SubscriptionRecord(playerID, loginName, "ALL", Region.AALBORG);
    return record;
  }
  
  public String toString() {
    return "NullSubscriptionRecord";
  }

  @Override
  public ServerConfiguration getConfiguration() {
    return null;
  }

  @Override
  public void initialize(ObjectManager objMgr, ServerConfiguration config) {
  }

  @Override
  public void disconnect() {
    // No op
  }
}