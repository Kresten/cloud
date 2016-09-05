package cloud.cave.doubles;

import org.json.simple.JSONObject;

import cloud.cave.broker.*;
import cloud.cave.server.common.ServerConfiguration;

/**
 * A test double for the client request handler that forwards all client
 * requests directly to an associated server invoker. Must
 * be configured for the actual server invoker to use.
 * <p>
 * This way all network related issues are abstracted away for ease of testing
 * the implementation of client proxies and server invoker command
 * switching logic.
 * <p>
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class LocalMethodCallClientRequestHandler implements
    ClientRequestHandler {

  private Invoker invoker;

  public LocalMethodCallClientRequestHandler(Invoker srh) {
    invoker = srh;
  }

  @Override
  public void initialize(ServerConfiguration config) {
    // Not relevant, as this request handler is only used in
    // testing and under programmatic control
  }
  
  @Override
  public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson) {
    // System.out.println("--> CRH: "+ requestJson);
    JSONObject reply = invoker.handleRequest(requestJson);
    // System.out.println("<-- CRH: "+ reply);
    return reply;
  }

  @Override
  public String toString() {
    return "LocalMethodCallClientRequestHandler";
  }
}
