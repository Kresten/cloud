package cloud.cave.client;

import java.util.*;

import org.json.simple.*;

import cloud.cave.broker.*;
import cloud.cave.common.PlayerSessionExpiredException;
import cloud.cave.domain.*;

/**
 * A Proxy (Flexible, Reliable Software, page 317) or more specifically a
 * combination of ClientProxy and Requestor (Broker pattern / Flexible Reliable
 * Software 2nd edition) for the Player role.
 * <p>
 * Some Player methods are implemented locally.
 * <p>
 * Never instantiate a player proxy instance directly, instead you must create a
 * cave proxy, and use its login method to retrieve the player proxy.
 * <p>
 * All methods follow the same remote proxy + requestor template
 * <ol>
 * <li>Marshal this and parameters into a request object in JSON
 * <li>Send it to server and await reply
 * <li>Convert reply object back into return values
 * </ol>
 * <p>
 *
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class PlayerProxy implements Player, ClientProxy, Requestor {
    private ClientRequestHandler crh;
    private String playerID;

    private String playerName;
    private String sessionID;
    private Region region;
    private String position;
    private String roomDesc;

    private JSONObject requestJson;

    /**
     * DO NOT USE THIS CONSTRUCTOR DIRECTLY (except in unit tests perhaps). Create
     * the player proxy configured with the relevant request handler and
     * properties.
     * @param crh        the client request handler to communicate with the server based
     *                   player instance
     * @param playerID   id of the player
     * @param playerName name of the player
     * @param sessionID  id of the session initiated by this player's login
     * @param region     region of the player
     * @param position   the first position of the player
     * @param roomDesc   the description of the first room
     */
    PlayerProxy(ClientRequestHandler crh,
                String playerID,
                String playerName,
                String sessionID,
                Region region,
                String position,
                String roomDesc) {
        this.crh = crh;
        this.playerID = playerID;
        this.playerName = playerName;
        this.sessionID = sessionID;
        this.region = region;
        this.position = position;
        this.roomDesc = roomDesc;
    }

    @Override
    public String getID() {
        return playerID;
    }

    @Override
    public String getShortRoomDescription() {
        return roomDesc;
    }

    @Override
    public String getLongRoomDescription() {
        // Local implementation of the long room description.
        // TODO: Exercise - what quality attribute is negatively affected by the way
        // this method is implemented?
        String allOfIt = getShortRoomDescription() + "\nThere are exits in directions:\n";
        for (Direction dir : getExitSet()) {
            allOfIt += "  " + dir + " ";
        }
        allOfIt += "\nYou see other players:\n";
        List<String> playerNameList = getPlayersHere();
        int count = 0;
        for (String p : playerNameList) {
            allOfIt += "  [" + count + "] " + p;
            count++;
        }
        return allOfIt;
    }

    @Override
    public String getName() {
        return playerName;
    }

    @Override
    public String getSessionID() {
        return sessionID;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public boolean move(Direction direction) {
        requestJson = createRequestObject(MarshalingKeys.MOVE_METHOD_KEY,
                direction.toString());
        JSONObject replyJson = requestAndAwaitReply(requestJson);
        if (replyJson.get(MarshalingKeys.ERROR_CODE_KEY).equals(StatusCode.SERVER_FAILED_TO_LOAD_COMMAND)) {
            return false;
        }
        String asString = replyJson.get(MarshalingKeys.RETURNVALUE_HEAD_KEY).toString();
        boolean unboxed = Boolean.parseBoolean(asString);

        JSONArray array = (JSONArray) replyJson.get(MarshalingKeys.RETURNVALUE_TAIL_KEY);
        position = (String) array.get(0);
        roomDesc = (String) array.get(1);

        return unboxed;
    }

    @Override
    public boolean backtrack() {
        requestJson = createRequestObject(MarshalingKeys.BACKTRACK_METHOD_KEY, "");
        JSONObject replyJson = requestAndAwaitReply(requestJson);
        String asString = replyJson.get(MarshalingKeys.RETURNVALUE_HEAD_KEY).toString();
        boolean unboxed = Boolean.parseBoolean(asString);

        JSONArray array = (JSONArray) replyJson.get(MarshalingKeys.RETURNVALUE_TAIL_KEY);
        position = (String) array.get(0);
        roomDesc = (String) array.get(1);

        return unboxed;
    }

    @Override
    public boolean digRoom(Direction direction, String description) {
        JSONObject requestJson =
                Marshaling.createRequestObject(playerID,
                        sessionID, MarshalingKeys.DIG_ROOM_METHOD_KEY,
                        "" + direction.toString(), description);
        JSONObject replyJson = requestAndAwaitReply(requestJson);
        if (replyJson.get(MarshalingKeys.ERROR_CODE_KEY).equals(StatusCode.SERVER_FAILED_TO_LOAD_COMMAND)) {
            return false;
        }
        String asString = replyJson.get(MarshalingKeys.RETURNVALUE_HEAD_KEY).toString();
        boolean unboxed = Boolean.parseBoolean(asString);
        return unboxed;
    }

    @Override
    public String getPosition() {
        return position;
    }

    @Override
    public List<Direction> getExitSet() {
        requestJson = createRequestObject(MarshalingKeys.GET_EXITSET_METHOD_KEY, null);
        JSONObject replyJson = requestAndAwaitReply(requestJson);
        List<Direction> exitsHere = new ArrayList<Direction>();
        if (replyJson.get(MarshalingKeys.ERROR_CODE_KEY).equals(StatusCode.SERVER_FAILED_TO_LOAD_COMMAND)) {
            exitsHere.add((Direction) replyJson.get(MarshalingKeys.ERROR_MSG_KEY));
        } else {
            // The HEAD is not used, the list of player names are stored in the TAIL
            JSONArray array = (JSONArray) replyJson.get(MarshalingKeys.RETURNVALUE_TAIL_KEY);
            // Convert the string values back into enums
            for (Object item : array) {
                exitsHere.add(Direction.valueOf(item.toString()));
            }
        }
        return exitsHere;
    }

    @Override
    public List<String> getPlayersHere() {
        JSONObject requestJson = createRequestObject(MarshalingKeys.GET_PLAYERS_HERE_METHOD_KEY, "");
        JSONObject replyJson = requestAndAwaitReply(requestJson);
        // The HEAD is not used, the list of player names are stored in the TAIL
        List<String> playersHere = new ArrayList<String>();
        processJsonList(replyJson, playersHere);
        return playersHere;
    }

    private void processJsonList(JSONObject replyJson, List<String> playersHere) {
        if (replyJson.get(MarshalingKeys.ERROR_CODE_KEY).equals(StatusCode.SERVER_FAILED_TO_LOAD_COMMAND)) {
            playersHere.add((String) replyJson.get(MarshalingKeys.ERROR_MSG_KEY));
        } else {
            JSONArray array = (JSONArray) replyJson.get(MarshalingKeys.RETURNVALUE_TAIL_KEY);
            // Convert it into a string list
            for (Object item : array) {
                playersHere.add(item.toString());
            }
        }
    }

    @Override
    public void addMessage(String message) {
        JSONObject requestJson = createRequestObject(MarshalingKeys.ADD_MESSAGE_METHOD_KEY, message);
        requestAndAwaitReply(requestJson);
    }

    @Override
    public List<String> getMessageList(int from) {
        JSONObject requestJson = createRequestObject(MarshalingKeys.GET_MESSAGE_LIST_METHOD_KEY, Integer.toString(from));
        JSONObject replyJson = requestAndAwaitReply(requestJson);
        List<String> messageList = new ArrayList<String>();
        processJsonList(replyJson, messageList);
        return messageList;
    }

    @Override
    public String getWeather() {
        JSONObject requestJson = createRequestObject(MarshalingKeys.GET_WEATHER_METHOD_KEY, null);
        JSONObject replyJson = requestAndAwaitReply(requestJson);
        String weather = replyJson.get(MarshalingKeys.RETURNVALUE_HEAD_KEY).toString();
        return weather;
    }

    @Override
    public JSONObject execute(String commandName, String... parameters) {
        JSONObject requestJson =
                Marshaling.createRequestObject(playerID, sessionID, MarshalingKeys.EXECUTE_METHOD_KEY, commandName, parameters);
        JSONObject replyJson = requestAndAwaitReply(requestJson);
        position = (String) replyJson.get("position");
        roomDesc = (String) replyJson.get("roomDesc");
        return replyJson;
    }

    @Override
    public String toString() {
        return "(PlayerClientProxy: " + getID() + "/" + getName() + ")";
    }

    public JSONObject lastSentRequestObject() {
        return requestJson;
    }

    private JSONObject createRequestObject(String methodKey,
                                           String parameter) {
        JSONObject requestJson =
                Marshaling.createRequestObject(playerID, sessionID, methodKey, parameter);
        return requestJson;
    }

    private JSONObject requestAndAwaitReply(JSONObject requestJson) {
        JSONObject replyJson = ClientCommon.requestAndAwaitReply(crh, requestJson);
        String statusCode = replyJson.get(MarshalingKeys.ERROR_CODE_KEY).toString();
        if (statusCode.equals(StatusCode.SERVER_PLAYER_SESSION_EXPIRED_FAILURE)) {
            String errMsg = replyJson.get(MarshalingKeys.ERROR_MSG_KEY).toString();
            throw new PlayerSessionExpiredException(errMsg);
        }

        return replyJson;
    }

}
