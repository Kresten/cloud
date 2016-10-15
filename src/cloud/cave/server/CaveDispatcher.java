package cloud.cave.server;

import cloud.cave.broker.CaveStorageException;
import cloud.cave.broker.Marshaling;
import cloud.cave.broker.MarshalingKeys;
import cloud.cave.broker.StatusCode;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Dispatcher implementation covering the methods provided by Cave.
 *
 * @author Henrik Baerbak Christensen, Aarhus University.
 */
public class CaveDispatcher implements Dispatcher {

    private ObjectManager objectManager;

    /**
     * Construct the dispatcher on all Cave method invocations.
     *
     * @param objectManager the object manager
     */
    public CaveDispatcher(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    @Override
    public JSONObject dispatch(String methodKey, String playerID,
                               String sessionID, String parameter1, JSONArray parameterList) {
        Cave cave = objectManager.getCave();
        JSONObject reply = null;
        try {
            // === LOGIN
            if (methodKey.equals(MarshalingKeys.LOGIN_METHOD_KEY)) {
                String loginName = parameter1;
                String password = parameterList.get(0).toString();
                Login result = cave.login(loginName, password);
                if (LoginResult.isValidLogin(result.getResultCode())) {
                    String playerId = result.getPlayer().getID();
                    String playerName = result.getPlayer().getName();
                    String sessionId = result.getPlayer().getSessionID();
                    Region region = result.getPlayer().getRegion();
                    String position = result.getPlayer().getPosition();
                    String roomDesc = result.getPlayer().getShortRoomDescription();
                    // add the player to the cache
                    // cache.add(sessionId, result.getPlayer());
                    reply = Marshaling.createValidReplyWithReturnValue(result
                            .getResultCode().toString(), playerId, playerName, sessionId, region.name(), position, roomDesc);
                } else {
                    // The player id is set to "" as we do not know
                    reply = Marshaling.createValidReplyWithReturnValue(result
                            .getResultCode().toString());
                }
            }
            // === LOGOUT
            else if (methodKey.equals(MarshalingKeys.LOGOUT_METHOD_KEY)) {
                LogoutResult result = cave.logout(playerID);

                // remove the player's session from the cache
                // cache.remove(sessionID);

                reply = Marshaling.createValidReplyWithReturnValue(result.toString());
            }
            // === DESCRIBE CONFIGURATION
            else if (methodKey.equals(MarshalingKeys.DESCRIBE_CONFIGURATION_METHOD_KEY)) {

                reply = Marshaling.createValidReplyWithReturnValue(cave.describeConfiguration());
            }

        } catch (CaveStorageException e) {
            reply = Marshaling.createInvalidReplyWithExplanation(StatusCode.SERVER_FAILED_TO_LOAD_COMMAND, e.getMessage());
        }
        // No need for a 'default case' as the returned null value will
        // be caught in the caller.
        return reply;
    }

}
