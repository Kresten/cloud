package cloud.cave.broker;

/**
 * List of status codes in the MarshalingKeys.ERROR_CODE_KEY value returned in
 * JSON replies from the application server to client side proxies.
 * 
 * @see Marshaling
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class StatusCode {

  public static final String OK = "OK";
  public static final String SERVER_FAILURE = "GENERAL_SERVER_FAILURE";
  public static final String SERVER_FAILED_TO_LOAD_COMMAND = "SERVER_COMMAND_LOAD_FAILURE";
  public static final String SERVER_FAILED_TO_INSTANTIATE_COMMAND = "SERVER_COMMAND_INSTANTIATE_FAILURE";
  public static final String SERVER_UNKNOWN_METHOD_FAILURE = "SERVER_UNKNOWN_METHOD_FAILURE";
  public static final String SERVER_PLAYER_SESSION_EXPIRED_FAILURE = "SERVER_PLAYER_SESSION_EXPIRED_FAILURE";
}
