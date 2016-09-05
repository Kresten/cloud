package cloud.cave.service;

import cloud.cave.server.common.SubscriptionRecord;

/**
 * Interface for the service that handles authentication of
 * players wanting to enter the cave.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public interface SubscriptionService extends ExternalService {

  /**
   * Look up the subscription for the given loginName and with the provided
   * password.
   * <p>
   * Note: A subscription is ALWAYS returned, it is the error code of the
   * subscription object that defines any illegal lookups, like no such
   * loginName, wrong password, etc.
   * 
   * @param loginName
   *          login name of the player to lookup
   * @param password
   *          the password of the player
   * @return a record type with the properties of the subscription, may be an
   *         illegal record.
   */
  SubscriptionRecord lookup(String loginName, String password);
}
