package cloud.cave.server.common;

/**
 * The result of matching a (loginName,password) with the
 * credentials stored in the subscription service will be
 * one of the following values.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public enum SubscriptionResult {
  LOGIN_NAME_HAS_VALID_SUBSCRIPTION, // Both login name and password match the subscription
  LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN, // The login name is not found in subscription service
}
