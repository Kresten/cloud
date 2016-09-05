package cloud.cave.broker;

import org.json.simple.*;

/**
 * A very simple marshaling technique: a small collection of static functions
 * provides the ability to create JSON request- and reply objects.
 * <p>
 * The format allows unlimited parameters of any type as well as unlimited
 * return values of any type.
 * <p>
 * As the by far mostly used type of method call to be marshaled has three
 * parameters (playerId, session id, and a parameter) and has a single valued
 * return type (a return value) convenience methods handle these cases.
 * <p>
 * For multi parameter method calls and multi valued returns, arrays are used.
 * <p>
 * The convention is used that the 1 parameter/return value is denoted HEAD and
 * the array of the rest denoted TAIL (i.e. the LISP convention, if anyone are
 * old enough to remember that.)
 * <p>
 * Also the convention is used that everything is STRINGS! Thus when
 * interpreting any value, take care to make the proper conversion.
 * <p>
 * And beware of the ordering of arguments - as everything are strings
 * accidentally reversing the playerID and sessionID in the call
 * leads to nasty defects. 
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class Marshaling {
  
  /** Version of the current marshaling */
  public static final String MARSHALING_VERSION = "2";
  
  @SuppressWarnings("unchecked") 
  /** Create a simple request object having one parameter
   * for a given player and given method
   * @param playerID id of the player
   * @param sessionID id of the session the player is logged in
   * @param commandKey key of method to call 
   * @param parameter the single parameter provided
   * @return JSON object that contains the marshaled
   * object representing this method call.
   */
  public static JSONObject createRequestObject(String playerID,
      String sessionID, String commandKey, String parameter1) {
    JSONObject requestJson = new JSONObject();

    requestJson.put(MarshalingKeys.PLAYER_ID_KEY, playerID);
    requestJson.put(MarshalingKeys.PLAYER_SESSION_ID_KEY, sessionID);
    requestJson.put(MarshalingKeys.VERSION_NO_KEY, MARSHALING_VERSION);
    requestJson.put(MarshalingKeys.METHOD_KEY, commandKey);
    requestJson.put(MarshalingKeys.PARAMETER_HEAD_KEY, parameter1);

    return requestJson;
  }
  
  @SuppressWarnings("unchecked")
  /**
   * Create a complex request object having several parameters for a given
   * player and given method
   * 
   * @param playerID
   *          id of the player
   * @param commandKey
   *          key of method to call
   * @param parameter
   *          the first parameter provided
   * @param parametArray
   *          the rest of the parameters
   * @return JSON object that contains the marshaled object representing this
   *         method call.
   */
  public static JSONObject createRequestObject(String playerID,
      String sessionID,
      String methodKey, String parameter1, String... parameterArray) {
    JSONObject request = createRequestObject(playerID, sessionID, methodKey, parameter1);
    // and add the rest of the parameters
    JSONArray array = new JSONArray();
    for (String parameter : parameterArray) {
      array.add(parameter.toString());
    }
    request.put(MarshalingKeys.PARAMETER_TAIL_KEY, array);
    return request;
  } 

  
  @SuppressWarnings("unchecked") 
  /** Create a simple reply JSON object that represents a
   * successful operation with a single return value. Typically
   * it represents the return value of a method invocation.
   * Use an empty string for a void method.
   * 
   * @param returnValue the value to return
   * @return a valid reply JSON object following the marshaling
   * conventions.
   */
  public static JSONObject createValidReplyWithReturnValue(String returnValue) { 
    JSONObject reply = new JSONObject(); 
    reply.put(MarshalingKeys.ERROR_CODE_KEY, StatusCode.OK); 
    reply.put(MarshalingKeys.ERROR_MSG_KEY, "OK"); 
    reply.put(MarshalingKeys.RETURNVALUE_HEAD_KEY, returnValue); 
    reply.put(MarshalingKeys.VERSION_NO_KEY, MARSHALING_VERSION); 
    return reply; 
  } 

  @SuppressWarnings("unchecked")
  /** Create a complex reply JSON object that represents a
   * successful operation with multiple return values. 
   * 
   * @param returnValue the value to return
   * @return a valid reply JSON object following the marshaling
   * conventions.
   */
  public static JSONObject createValidReplyWithReturnValue(String returnValue,
      String... returnValueArray) {
    JSONObject reply = createValidReplyWithReturnValue(returnValue);
    // and add the rest of the return values
    JSONArray array = new JSONArray();
    for (String returnvalue : returnValueArray) {
      array.add(returnvalue);
    }
    reply.put(MarshalingKeys.RETURNVALUE_TAIL_KEY, array);
    return reply;
  }
 
  @SuppressWarnings("unchecked")
  /** Create a reply JSON object that represents the result of
   * an operation that failed for some reason, for instance
   * a method that threw an exception, a time out, a
   * denial of service due to overload, or similar.
   * 
   * @param statusCode the code for the status, see the
   * valid ones in StatusCode
   * @param errorMsg a further description of the error as
   * a human readable and sufficiently self contained message
   * to aid debugging 
   * @return the reply object that represents the failed operation
   */
  public static JSONObject createInvalidReplyWithExplanation(String statusCode,
      String errorMsg) {
    JSONObject reply = new JSONObject();
    reply.put(MarshalingKeys.ERROR_CODE_KEY, statusCode);
    reply.put(MarshalingKeys.ERROR_MSG_KEY, errorMsg);
    reply.put(MarshalingKeys.VERSION_NO_KEY, MARSHALING_VERSION); 
    return reply;
  }
}
