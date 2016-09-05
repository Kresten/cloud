package cloud.cave.client;

import org.json.simple.JSONObject;
import org.slf4j.*;

import cloud.cave.broker.*;

/** Collection of functions used in both client side proxies.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */

public class ClientCommon {

  private static Logger logger = LoggerFactory.getLogger(ClientCommon.class);
  
  /**
   * Given the client request handler and a request encoded in JSON, send the
   * request, block until a reply is received, and return it to the caller. In
   * case the reply represents a failed request, log it.
   * 
   * @param crh
   *          the client request handler responsible for forwarding the request
   *          'over the wire using IPC' to the server.
   * @param requestJson
   *          the JSON encoded request
   * @return the JSON encoded reply
   * @throws CaveIPCException for any IPC error in contacting the server
   */
  public static JSONObject requestAndAwaitReply(ClientRequestHandler crh, 
      JSONObject requestJson) throws CaveIPCException {
    JSONObject replyJson = null;

    replyJson = crh.sendRequestAndBlockUntilReply(requestJson);

    // The reply may indicate an error occurring in the server, log it
    String statusCode = replyJson.get(MarshalingKeys.ERROR_CODE_KEY).toString();
    if (! statusCode.equals(StatusCode.OK)) {
      String errMsg = replyJson.get(MarshalingKeys.ERROR_MSG_KEY).toString();
      logger.warn("requestAndAwaitReply: Server returned an error '"+ errMsg+"'.");
    }
    return replyJson; 
  }

}
