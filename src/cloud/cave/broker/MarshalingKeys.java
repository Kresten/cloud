package cloud.cave.broker;

/**
 * Requests and replies are marshaled into JSON which is basically an notation
 * for lists of (key,value) pairs.
 * <p>
 * The constants below define the keys for the individual attributes.
 * <p>
 * Note: a convention is made for method name keys, namely that all method names
 * are prefixed with a string indicating the type/class of the servant object
 * ('player-' = player; 'cave-' = cave), to allow easy lookup of the proper
 * event handler (called Dispatcher here) by the Invoker. This is essentially an
 * example of 'name mangling'.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class MarshalingKeys {

  // Key of the version number of the marshalling
  public static final Object VERSION_NO_KEY = "version";

  // List of keys used in the JSON request/reply objects 
  public static final String METHOD_KEY = "method"; 
  public static final String PLAYER_ID_KEY = "player-id"; 
  public static final String PLAYER_SESSION_ID_KEY = "player-session-id"; 
  public static final String PARAMETER_HEAD_KEY = "parameter";
  public static final String PARAMETER_TAIL_KEY = "parameter-tail";
  
  // the key for return values
  public static final String RETURNVALUE_HEAD_KEY = "reply";
  public static final String RETURNVALUE_TAIL_KEY = "reply-tail";
  public static final String ERROR_CODE_KEY = "error-code"; 
  public static final String ERROR_MSG_KEY = "error-message";

  // Prefixes of the types/classes that have methods associated
  // Note that the Invoker depends upon these ending in a dash
  // so do not change that.
  public static final String CAVE_TYPE_PREFIX = "cave-";
  public static final String PLAYER_TYPE_PREFIX = "player-";
  
  // List of player method keys
  public static final String MOVE_METHOD_KEY = PLAYER_TYPE_PREFIX+"move";
  public static final String BACKTRACK_METHOD_KEY = PLAYER_TYPE_PREFIX+"backtrack";;
  public static final String GET_SHORT_ROOM_DESCRIPTION_METHOD_KEY = PLAYER_TYPE_PREFIX+"get-short-room-description";
  public static final String GET_POSITION_METHOD_KEY = PLAYER_TYPE_PREFIX+"get-position";
  public static final String GET_REGION_METHOD_KEY = PLAYER_TYPE_PREFIX+"get-region";
  public static final String GET_PLAYERS_HERE_METHOD_KEY = PLAYER_TYPE_PREFIX+"get-players-here";
  public static final String GET_EXITSET_METHOD_KEY = PLAYER_TYPE_PREFIX+"get-exit-set";
  public static final String DIG_ROOM_METHOD_KEY = PLAYER_TYPE_PREFIX+"dig-room";
  public static final String EXECUTE_METHOD_KEY = PLAYER_TYPE_PREFIX+"execute";
  
  public static final String GET_WEATHER_METHOD_KEY = PLAYER_TYPE_PREFIX+"get-weather";
  public static final String ADD_MESSAGE_METHOD_KEY = PLAYER_TYPE_PREFIX+"add-message";
  public static final String GET_MESSAGE_LIST_METHOD_KEY = PLAYER_TYPE_PREFIX+"get-message-list";

  // List of cave method keys
  public static final String LOGIN_METHOD_KEY = CAVE_TYPE_PREFIX+"login";
  public static final String LOGOUT_METHOD_KEY = CAVE_TYPE_PREFIX+"logout";
  public static final String DESCRIBE_CONFIGURATION_METHOD_KEY = CAVE_TYPE_PREFIX+"describe-configuration";


}
