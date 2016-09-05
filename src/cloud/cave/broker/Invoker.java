package cloud.cave.broker;

import org.json.simple.JSONObject;

/**
 * The Invoker role on the server side, responsible for demarshalling the request
 * from the client, invoke the proper method on the proper Servant(s), and return a
 * reply object that encapsulate the result of the method call.
 * 
 * <p>
 * The server request handler will call the invoker's handleRequest method after
 * having received a request on the network.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public interface Invoker {

  /**
   * This method is called by the server request handler on the server side that
   * receives raw messages from clients, converts them into JSON and then
   * lets an instance of Invoker interpret and makes the proper invocation of a
   * method on the proper server side object. 
   * 
   * @param requestJson
   *          the request object from the client
   * @return the returned answer from the proper server-side object
   */
  JSONObject handleRequest(JSONObject requestJson);

}
