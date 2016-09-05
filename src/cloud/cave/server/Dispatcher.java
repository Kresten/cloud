package cloud.cave.server;

import org.json.simple.*;

/**
 * The dispatcher is the handler that translates from the partially decoded abstract
 * form of a method call to the actual method call on a particular class of objects.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */

public interface Dispatcher {

  /**
   * Perform the method dispatching - call the proper method in the proper
   * object for the parameters extracted from the request object; and marshal
   * the result of the method call into a properly formatted reply object.
   * 
   * @param methodKey
   *          the identity of the method to call, as outlined by the constants @see
   *          MarshalingKeys
   * @param playerID
   *          id of the player, or null if not a player method 
   * @param sessionID
   *          session ID for the player (if it was a player method); otherwise
   *          this parameter may have other interpretations
   * @param parameter1
   *          first parameter provided for the method call
   * @param parameterList
   *          (JSON) array of subsequent parameters for the method call
   * @return the result of the method call, with return values encoded as a JSON
   *         object
   */

  JSONObject dispatch(String methodKey, String playerID, String sessionID, String parameter1,
      JSONArray parameterList);

}
