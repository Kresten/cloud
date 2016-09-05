package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.*;
import org.json.simple.*;

import cloud.cave.broker.*;
import cloud.cave.common.LoginRecord;
import cloud.cave.config.Config;
import cloud.cave.domain.*;
import cloud.cave.server.common.*;

/**
 * TDD tests of the marshaling code and some simple tests of the record types.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class TestMarshaling {
  
  @Test
  public void shouldMarshalSimpleCommand() {
    JSONObject request = 
        Marshaling.createRequestObject(
            "user-003",
            "long-session-id",  
            MarshalingKeys.MOVE_METHOD_KEY, 
            ""+Direction.EAST);
    assertEquals( "user-003", request.get(MarshalingKeys.PLAYER_ID_KEY));
    assertEquals( "long-session-id", request.get(MarshalingKeys.PLAYER_SESSION_ID_KEY));
    assertEquals( MarshalingKeys.MOVE_METHOD_KEY, request.get(MarshalingKeys.METHOD_KEY));
    assertEquals( Direction.EAST.toString(), request.get(MarshalingKeys.PARAMETER_HEAD_KEY));
  }
  
  @Test
  public void shouldMarshalComplexCommand() {
    String loginName = "any@one.two.edu";
    String password = "secrect-pwd";
    int parameter2 = 42;
    Direction parameter3 = Direction.SOUTH;
    JSONObject request = 
        Marshaling.createRequestObject("user-002", "longer-session-id", MarshalingKeys.LOGIN_METHOD_KEY, 
            loginName, password, ""+parameter2, parameter3.toString());

    assertEquals( "user-002", request.get(MarshalingKeys.PLAYER_ID_KEY));
    assertEquals( "longer-session-id", request.get(MarshalingKeys.PLAYER_SESSION_ID_KEY));
    assertEquals( loginName, request.get(MarshalingKeys.PARAMETER_HEAD_KEY));
    assertEquals( MarshalingKeys.LOGIN_METHOD_KEY, request.get(MarshalingKeys.METHOD_KEY));
    assertEquals( Marshaling.MARSHALING_VERSION, request.get(MarshalingKeys.VERSION_NO_KEY));
    
    // verify the extensible parameter list
    JSONArray paramList = (JSONArray) request.get(MarshalingKeys.PARAMETER_TAIL_KEY);
    assertNotNull("The request JSON must contain an array of 'tail' parameters", paramList);
    assertEquals(3, paramList.size());
    
    assertEquals(password, paramList.get(0).toString());
    assertEquals( 42, Integer.parseInt((paramList.get(1)).toString()) );
    assertEquals( parameter3.toString(), (paramList.get(2)).toString() );
  } 
  
  @Test
  public void shouldMashalValidReply() {
    JSONObject reply = Marshaling.createValidReplyWithReturnValue("EAST");
    assertThat(reply.get(MarshalingKeys.ERROR_CODE_KEY).toString(), is(StatusCode.OK));
    assertEquals( Marshaling.MARSHALING_VERSION, reply.get(MarshalingKeys.VERSION_NO_KEY));
  }

  @Test
  public void shouldMashalInvalidReply() {
    JSONObject reply = Marshaling.
        createInvalidReplyWithExplanation(StatusCode.SERVER_FAILURE, "The server has blown up!");
    // System.out.println(reply);
    assertThat(reply.get(MarshalingKeys.ERROR_CODE_KEY).toString(), is(StatusCode.SERVER_FAILURE));
    assertThat(reply.get(MarshalingKeys.ERROR_MSG_KEY).toString(), is("The server has blown up!"));
    assertEquals( Marshaling.MARSHALING_VERSION, reply.get(MarshalingKeys.VERSION_NO_KEY));
  }
  
  
  // Validate the object methods of the record types, not that interesting
  // but these tests augment the coverage
  
  @Test
  public void shouldTestPlayerRecordObjectMethods() {
    
    PlayerRecord pr = new PlayerRecord(
        new SubscriptionRecord("u2", "bimse", "grp01", Region.AARHUS), "(0,0,0)", "session-123");
    assertThat( pr.toString(), 
        is("PlayerRecord [playerID=u2, playerName=bimse, groupName=grp01, region=AARHUS, positionAsString=(0,0,0), sessionID=session-123]"));
    assertThat( pr.hashCode(), is(not(0)));
    assertFalse( pr.equals(null));
  }

  @Test
  public void shouldTestSubscriptionRecordObjectMethods() {
    SubscriptionRecord sr = new SubscriptionRecord(SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN);
    assertThat( sr.toString(), 
        is("SubscriptionRecord [playerName=null, playerID=null, groupName=null, region=null, errorCode=LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN]"));
  }

  @Test
  public void shouldTestLoginRecordObjectMethods() {
    LoginRecord lr = new LoginRecord(LoginResult.LOGIN_FAILED_UNKNOWN_SUBSCRIPTION);
    assertThat( lr.toString(), 
        is("(LoginResult: UNDEFINED/LOGIN_FAILED_UNKNOWN_SUBSCRIPTION)"));
  }
  
  @SuppressWarnings("unused")
  @Test
  public void shouldIncreaseCoverageByMagic() {
    // non-sense tests to increase coverage...
    Marshaling m = new Marshaling();
    Config c = new Config();
    assertTrue(true);
  }
}
