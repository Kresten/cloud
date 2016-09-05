package cloud.cave.broker;

import org.json.simple.JSONObject;

import cloud.cave.server.common.ServerConfiguration;

/**
 * A Client Request Handler (Pattern-oriented Software Architecture, vol 4, p
 * 246) that encapsulate and performs IPC (inter process communication) on
 * behalf of client objects that sends request to, and received replies from,
 * remote objects.
 * <p>
 * 
 * Proxies marshal method calls into JSON request objects and use an instance
 * of this role to send it to the server.
 * <p>
 * 
 * As a request handler instance is created dynamically by the ClientFactory
 * there cannot be any other constructors than the default one. Be SURE to
 * invoke the 'initialize' method BEFORE the send...() method is invoked.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public interface ClientRequestHandler {

  /**
   * Send a request object over some inter process communication protocol, and
   * block until a reply object is returned.
   * 
   * @param requestJson
   *          the request encoded in JSON
   * @return the reply object from the server
   * @throws CaveIPCException
   *           thrown in case some unexpected network or server failure occured
   */
  JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson)
      throws CaveIPCException;

  /**
   * Initialize the client request handler with the server configuration. This
   * HAS to be executed BEFORE any sendRequestAndBlockUntilReply() method is
   * invoked.
   * 
   * @param config
   *          the configuration of IP and ports of the server(s) to communicate
   *          with
   */

  void initialize(ServerConfiguration config);
}
