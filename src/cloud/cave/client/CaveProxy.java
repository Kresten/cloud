package cloud.cave.client;

import cloud.cave.broker.*;
import cloud.cave.common.LoginRecord;
import cloud.cave.domain.*;

import org.json.simple.*;
import org.slf4j.*;

/** The Cave implementation on the client as a remote proxy.
 * <p>
 * It is a Proxy pattern (Flexible, Reliable Software, p. 317), 
 * more specifically a ClientProxy (FRS, 2nd edition).
 * <p> 
 * In the present implementation, it also
 * implements the 'Requestor' role of the Broker pattern,
 * which is responsible for marshalling and location (FRS, 2nd edition).
 * <p>
 * All methods follow the same remote proxy template
 * <ol>
 * <li> Marshal this and parameters into a request object in JSON
 * <li> Send it to server and await reply
 * <li> Convert reply object back into return values
 * </ol>
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class CaveProxy implements Cave, ClientProxy {

  private ClientRequestHandler crh;
  private JSONObject requestJson;

  private Logger logger; 

  /** Create the cave proxy with the given request handler.
   * 
   * @param crh the request handler for IPC
   */
  public CaveProxy(ClientRequestHandler crh) {
    this.crh = crh;
    logger = LoggerFactory.getLogger(CaveProxy.class);
  }

  @Override
  public Login login(String loginName, String password) {
    // Build the json request object 
    requestJson = 
        Marshaling.createRequestObject("ignore-player-id", // No player id
            "ignore-session-id",
            MarshalingKeys.LOGIN_METHOD_KEY, 
            loginName, password);

    Login result = null;

    // send the request over the connector and retrieve the reply object 
    JSONObject replyJson = requestAndAwaitReply(requestJson);

    String statusCode = replyJson.get(MarshalingKeys.ERROR_CODE_KEY).toString();

    if (! statusCode.equals(StatusCode.OK)) {
      result = new LoginRecord(LoginResult.LOGIN_FAILED_SERVER_ERROR);
      // log the incident
      String errorMsg = replyJson.get(MarshalingKeys.ERROR_MSG_KEY).toString();
      logger.error("Login of "+loginName+" failed due to "+LoginResult.LOGIN_FAILED_SERVER_ERROR+". "
          + "Error msg from server: '"+errorMsg+"'.");
    } else {
      // extract the reply and convert into client side objects; note
      // that in case of unsuccessful login the return JSON has no tail part!
      String loginResultAsString = replyJson.get(
          MarshalingKeys.RETURNVALUE_HEAD_KEY).toString();
      LoginResult loginResult = LoginResult.valueOf(loginResultAsString);
      boolean validLogin = LoginResult.isValidLogin(loginResult);

      if (validLogin) {
        JSONArray returnValueArray = (JSONArray) replyJson
            .get(MarshalingKeys.RETURNVALUE_TAIL_KEY);

        String playerID = returnValueArray.get(0).toString();
        String playerName = returnValueArray.get(1).toString();
        String sessionId = returnValueArray.get(2).toString();

        Player player = new PlayerProxy(crh, playerID, playerName, sessionId);
        result = new LoginRecord(player, loginResult);
      } else {
        result = new LoginRecord(loginResult);
      }
    }
    return result;
  }

  @Override
  public LogoutResult logout(String playerID) {
    // Build the json request object 
    requestJson = Marshaling.createRequestObject(playerID,
        "ignore-session-id", // No session id
        MarshalingKeys.LOGOUT_METHOD_KEY, "");
    LogoutResult result = LogoutResult.PLAYER_NOT_IN_CAVE;

    // send the request over the connector and retrieve the reply object
    JSONObject replyJson;
    replyJson = requestAndAwaitReply(requestJson);

    String statusCode = replyJson.get(MarshalingKeys.ERROR_CODE_KEY).toString();

    if (statusCode.equals(StatusCode.OK)) {
      String enumAsString = replyJson.get(MarshalingKeys.RETURNVALUE_HEAD_KEY)
          .toString();
      result = LogoutResult.valueOf(enumAsString);
    } else {
      // log the incident
      String errorMsg = replyJson.get(MarshalingKeys.ERROR_MSG_KEY).toString();
      logger.error("Logout of "+playerID+" failed. "
          + "Error msg from server: '"+errorMsg+"'.");
      result = LogoutResult.SERVER_FAILURE;
    }
    return result;
  }

  private JSONObject requestAndAwaitReply(JSONObject requestJson) {
    return ClientCommon.requestAndAwaitReply(crh, requestJson);
  }
  
  public String toString() {
    return describeConfiguration();
  }

  @Override
  public String describeConfiguration() {
    String cfg = "CaveProxy configuration:\n";
    cfg += "  ClientRequestHandler: "+ crh.getClass().getName()+"\n"
        +  "   - cfg: "+crh.toString() + "\n";

    // Build the json request object 
    requestJson = 
        Marshaling.createRequestObject("", // no player id 
            "none", // no session id
            MarshalingKeys.DESCRIBE_CONFIGURATION_METHOD_KEY,"");

    // send the request over the connector and retrieve the reply object 
    JSONObject replyJson = requestAndAwaitReply(requestJson);

    String statusCode = replyJson.get(MarshalingKeys.ERROR_CODE_KEY).toString();

    if (! statusCode.equals(StatusCode.OK)) {
      cfg += " Server side configuration not available.";
    } else {
      cfg += replyJson.get(MarshalingKeys.RETURNVALUE_HEAD_KEY).toString();
    }
    
    return cfg;
  }

}
