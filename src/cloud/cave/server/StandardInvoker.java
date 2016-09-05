package cloud.cave.server;

import java.util.*;

import org.json.simple.*;
import org.slf4j.*;

import cloud.cave.broker.*;
import cloud.cave.common.Inspector;
import cloud.cave.config.*;

/**
 * Standard implementation of the Invoker role of the Broker pattern.
 * 
 * @author Henrik Baerbak Christensen, University of Aarhus
 *
 */
public class StandardInvoker implements Invoker {
  private Logger logger;
  private Dispatcher dispatcher;
  
  private Map<String,Dispatcher> mapKey2Dispatch;
  private ObjectManager objManager;
  private Inspector inspector;

  /**
   * Create an invoker that dispatches requests using default dispatching, that
   * is the set of methods known in the initial release of SkyCave.
   * 
   * @param objectManager
   *          object manager that holds delegates to use
   */
  public StandardInvoker(ObjectManager objectManager) {
    // Create the map that maps from class prefix to
    // dispatcher for that particular class type,
    // see Reactor pattern (POSA p 259) and 'identifyDispather' method.
    mapKey2Dispatch = new HashMap<String, Dispatcher>();    
    mapKey2Dispatch.put(MarshalingKeys.CAVE_TYPE_PREFIX, new CaveDispatcher(objectManager));
    mapKey2Dispatch.put(MarshalingKeys.PLAYER_TYPE_PREFIX, new PlayerDispatcher(objectManager));
    initialize(objectManager, mapKey2Dispatch);
  }
  
  /**
   * Create an invoker that dispatches requests to the cave using a given
   * dispatcher.
   * 
   * @param objectManager
   *          object manager that holds delegates to use
   * @param mapTypePrefixToDispatchers
   *          a Map that maps strings to dispatchers; the key string must be one
   *          of the type prefixes, like "player-", defined in the
   *          MarshalingKeys.
   */
  public StandardInvoker(ObjectManager objectManager, Map<String, Dispatcher> mapTypePrefixToDispatchers) {
    initialize(objectManager, mapTypePrefixToDispatchers);
  }

  /** initialize the invoker
   * @param objectManager 
   * 
   * @param mapTypePrefixToDispatchers dispatcher map to use
   */
  private void initialize(ObjectManager objectManager, Map<String, Dispatcher> mapTypePrefixToDispatchers) {
    this.objManager = objectManager;
    // Cache the inspector
    this.inspector = objManager.getInspector();
    mapKey2Dispatch = mapTypePrefixToDispatchers;
    logger = LoggerFactory.getLogger(StandardInvoker.class);
  }
  
  
  @Override
  public JSONObject handleRequest(JSONObject request) {
    JSONObject reply = null;  
    
    // Write to the IPC inspector log
    inspector.write(Inspector.IPC_TOPIC, 
        "[REQUEST] "+request.toString());
 
    // Extract the common parameters from the request object and assign
    // them names that reflect their meaning
    String playerID = request.get(MarshalingKeys.PLAYER_ID_KEY).toString();
    String sessionID = request.get(MarshalingKeys.PLAYER_SESSION_ID_KEY).toString(); 
    String methodKey = request.get(MarshalingKeys.METHOD_KEY).toString();
    String parameter1 = "";
    Object parameter1AsObj = request.get(MarshalingKeys.PARAMETER_HEAD_KEY);
    if ( parameter1AsObj != null ) {
      parameter1 = parameter1AsObj.toString();
    }
    JSONArray parameterList = 
        (JSONArray) request.get(MarshalingKeys.PARAMETER_TAIL_KEY);
 
    // Dispatch the event (POSA vol 4 Reactor code)
    dispatcher = identifyDispatcher(methodKey);
    
    // We may get a null object back if the method key is ill formed
    // thus guard the dispatch call
    if (dispatcher != null) {
      // Next, do the dispatching - based upon the parameters, call
      // the proper method on the proper object
      reply = dispatcher.dispatch(methodKey, playerID, sessionID, parameter1,
          parameterList);
    }    
    // UNHANDLED METHOD 
    if (reply == null) {
      reply = Marshaling.createInvalidReplyWithExplanation(StatusCode.SERVER_UNKNOWN_METHOD_FAILURE,
          "StandardInvoker.handleRequest: Unhandled request as the method key "+methodKey +
          " is unknown. Full request="+request.toString());
      logger.warn("handleRequest: Unhandled request as the method key "+methodKey +
          " is unknown. Full request="+request.toString());
    }
    inspector.write(Inspector.IPC_TOPIC, "[REPLY] "+reply);

    return reply;
  }

  /**
   * Identify the dispatcher appropriate for the given method. Corresponds to
   * the identify_handler(event) in Reactor pattern.
   * <p>
   * Presently relies on mangled method names, i.e. that a method on the player
   * starts with 'play' and a method on cave starts with 'cave'.
   * 
   * @param methodKey
   *          key of the method, see MarshalingKeys
   * @return the appropriate dispatcher for the class containing that particular
   *         method or null if the method key is ill-formed
   */
  private Dispatcher identifyDispatcher(String methodKey) {
    Dispatcher dsp = dispatcher;
    int firstDash = methodKey.indexOf("-");
    String key = methodKey.substring(0, firstDash+1);
    dsp = mapKey2Dispatch.get(key);
    return dsp;
  }
}
