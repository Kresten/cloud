package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.*;

import org.json.simple.*;
import org.junit.Test;

import cloud.cave.broker.*;
import cloud.cave.common.CommonCaveTests;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;

/**
 * Testing of the StandardInvoker. Most of its behavior is tested thoroughly by
 * the proxies for cave and player, but here we test the ability to install a
 * new (or actually bit changed) dispatcher.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestInvoker {

  @Test
  public void shouldDecorateCaveDispatching() {
    ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();
    
    // Create the map of dispatchers that link types to dispatchers
    Map<String,Dispatcher> myOwnDispatchers = new HashMap<String,Dispatcher>();

    // Reuse the standard dispatcher for player methods
    myOwnDispatchers.put(MarshalingKeys.PLAYER_TYPE_PREFIX, new PlayerDispatcher(objMgr));

    // But install a special one for cave methods which counts the number
    // of method up-calls to a cave instance
    MyCaveDispatcher myCaveDispatcher = new MyCaveDispatcher(objMgr);
    
    myOwnDispatchers.put(MarshalingKeys.CAVE_TYPE_PREFIX, myCaveDispatcher);

    StandardInvoker invoker = new StandardInvoker(objMgr, myOwnDispatchers);
    
    // All set - lets us make a request of an invalid login; 
    // the parameters are taken from CaveProxy's login method
    JSONObject requestJson = Marshaling.createRequestObject(
        "ignore-player-id", // No player id
        "ignore-session-id", MarshalingKeys.LOGIN_METHOD_KEY, "mathilde_aarskort",
        "invalidPwd");
    
    JSONObject reply = invoker.handleRequest(requestJson);
    // Litmus test - the invoker calculated the proper response
    assertThat(reply.get(MarshalingKeys.RETURNVALUE_HEAD_KEY).toString(), 
        is(LoginResult.LOGIN_FAILED_UNKNOWN_SUBSCRIPTION.toString()));
    
    // and our little 'spy' counted the upcall
    assertThat(myCaveDispatcher.getCount(), is(1));
  }
  
  
}

class MyCaveDispatcher extends CaveDispatcher {
  int count;
  public MyCaveDispatcher(ObjectManager objMgr) {
    super(objMgr);
    count = 0;
  }
  public int getCount() {
    return count;
  }

  @Override
  public JSONObject dispatch(String methodKey, String playerID,
      String sessionID, String parameter1, JSONArray parameterList) {
    count++;
    return super.dispatch(methodKey, playerID, sessionID, parameter1, parameterList);
  }
  
}

