package cloud.cave.doubles;

import org.json.simple.JSONObject;

import cloud.cave.broker.*;
import cloud.cave.server.common.ServerConfiguration;

/**
 * A Saboteur (Meszaros, 2007) test double, i.e. a test double that tries to
 * sabotage an operation - here simulating network connection exceptions.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class SaboteurCRHDecorator implements ClientRequestHandler {

  private ClientRequestHandler decoratee;
  private String exceptionMsg;
  
  public SaboteurCRHDecorator(ClientRequestHandler decoratee) {
    this.decoratee = decoratee;
    exceptionMsg = null;
  }
  
  public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson)
      throws CaveIPCException {
    if ( exceptionMsg != null ) { throw new CaveIPCException(exceptionMsg, null); }
    return decoratee.sendRequestAndBlockUntilReply(requestJson);
  }

  public void throwNextTime(String exceptionMessage) {
    exceptionMsg = exceptionMessage;
  }

  @Override
  public void initialize(ServerConfiguration config) {
    // Not relevant, as this request handler is only used in
    // testing and under programmatic control
  }

}
