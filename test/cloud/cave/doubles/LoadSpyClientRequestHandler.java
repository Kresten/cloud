package cloud.cave.doubles;

import org.json.simple.JSONObject;

import cloud.cave.broker.*;
import cloud.cave.server.common.ServerConfiguration;

/** A test spy (Meszaros) that counts number of requests made
 * as well as how many bytes of traffic are sent and received over
 * a test double IPC connection.
 * 
 * @author Henrik Baerbak Christensen, Computer Science, Aarhus University
 *
 */
public class LoadSpyClientRequestHandler implements ClientRequestHandler {
  private ClientRequestHandler decoratee;
  private int bytesSent;
  private int bytesReceived;
  private int requestsSent;

  public LoadSpyClientRequestHandler(ClientRequestHandler crh) {
    decoratee = crh;
    reset();
  }

  public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson)
      throws CaveIPCException {
    requestsSent++;
    bytesSent = requestJson.toJSONString().length();
    // System.out.println("SPY: --> # "+bytesSent);
    JSONObject reply = decoratee.sendRequestAndBlockUntilReply(requestJson);
    bytesReceived = reply.toJSONString().length();
    // System.out.println("SPY: --> # "+bytesReceived);
    return reply;
  }

  public void initialize(ServerConfiguration config) {
    decoratee.initialize(config);
  }

  public void reset() {
    bytesSent = bytesReceived = requestsSent = 0;
  }

  public int getSent() {
    return bytesSent;
  }

  public int getReived() {
    return bytesReceived;
  }

  public int getRequestsSent() {
    return requestsSent;
  }
}
