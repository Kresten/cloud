package cloud.cave.service;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Direction;
import cloud.cave.domain.Region;
import cloud.cave.server.common.*;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kresten on 29-09-2016.
 */
public class RealCaveStorage implements CaveStorage {

    //Room
    private MongoCollection<Document> rooms;
    private final String POSITION_KEY = "position";
    private final String DESCRIPTION_KEY = "description";
    //Wall
    private MongoCollection<Document> wallMessages;
    private final String MESSAGE_KEY = "message";
    //Player
    private MongoCollection<Document> players;
    private final String PLAYERID_KEY = "playerId";
    private final String PLAYERNAME_KEY = "playerName";
    private final String GROUPNAME_KEY = "groupName";
    private final String REGION_KEY = "region";
    private final String POSITIONSTRING_KEY = "positionString";
    private final String SESSIONID_KEY = "sessionId";
    private ServerConfiguration serverConfiguration;
    private MongoClient mongoClient;


    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.serverConfiguration = config;
        mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase("CaveStorage");
        rooms = db.getCollection("rooms");
        wallMessages = db.getCollection("wallMessages");
        players = db.getCollection("players");

        if (getRoom("0, 0, 0") == null) {
            addRoom("0, 0, 0", new RoomRecord(
                    "You are standing at the end of a road before a small brick building."));
        } if (getRoom("0, 1, 0") == null) {
            addRoom("0, 1, 0", new RoomRecord(
                    "You are in open forest, with a deep valley to one side."));
        } if (getRoom("1, 0, 0") == null) {
            addRoom("1, 0, 0", new RoomRecord(
                    "You are inside a building, a well house for a large spring."));
        } if (getRoom("-1, 0, 0") == null) {
            addRoom("-1, 0, 0", new RoomRecord(
                    "You have walked up a hill, still in the forest."));
        } if (getRoom("0, 0, 1") == null) {
            addRoom("0, 0, 1", new RoomRecord(
                    "You are in the top of a tall tree, at the end of a road."));
        }
    }

    @Override
    public RoomRecord getRoom(String positionString) {
        Document roomDoc = new Document(POSITION_KEY, positionString);
        List<Document> docList = new ArrayList();
        rooms.find(roomDoc).into(docList);
        if (!docList.isEmpty()) {
            Document doc = docList.get(0);
            return new RoomRecord((String) doc.get("description"));
        } else {
            return null;
        }
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        RoomRecord roomRecord = getRoom(positionString);
        if (roomRecord != null) {
            return false;
        } else {
            Document roomDoc = new Document();
            roomDoc.put(POSITION_KEY, positionString);
            roomDoc.put(DESCRIPTION_KEY, description.description);
            rooms.insertOne(roomDoc);
            return true;
        }
    }

    @Override
    public void addMessage(String positionString, String message) {
        Document addMessageDoc = new Document();
        addMessageDoc.put(POSITION_KEY, positionString);
        addMessageDoc.put(MESSAGE_KEY, message);
        wallMessages.insertOne(addMessageDoc);
    }

    @Override
    public List<String> getMessageList(String positionString, int from, int amount) {
        Document messageDoc = new Document(POSITION_KEY, positionString);
        List<Document> docList = new ArrayList();
        rooms.find(messageDoc).into(docList);
        List<String> messageList = new ArrayList();
        for (Document doc : docList) {
            messageList.add((String) doc.get(MESSAGE_KEY));
        }
        int messageListSize = messageList.size();
        if (messageListSize < from + amount) {
            amount = messageListSize - from;
        }
        List<String> shortMessageList = new ArrayList<>();
        for (int i = from; i < from + amount; i++) {
            shortMessageList.add(messageList.get(i));
        }
        return shortMessageList;
    }

    @Override
    public List<Direction> getSetOfExitsFromRoom(String positionString) {
        //refactor this to only use one query ($OR maybe)
        List<Direction> listOfExits = new ArrayList<Direction>();
        Point3 pZero = Point3.parseString(positionString);
        Point3 p;
        for (Direction d : Direction.values()) {
            p = new Point3(pZero.x(), pZero.y(), pZero.z());
            p.translate(d);
            String position = p.getPositionString();
            Document roomDoc = new Document(POSITION_KEY, position);
            List<Document> docList = new ArrayList();
            rooms.find(roomDoc).into(docList);
            if (!docList.isEmpty()) {
                listOfExits.add(d);
            }
        }
        return listOfExits;
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        Document playerDoc = new Document(PLAYERID_KEY, playerID);
        List<Document> docList = new ArrayList();
        players.find(playerDoc).into(docList);
        PlayerRecord playerRecord = null;
        if (!docList.isEmpty()) {
            Document player = docList.get(0);
            playerRecord = new PlayerRecord(new SubscriptionRecord(
                    (String) player.get(PLAYERID_KEY),
                    (String) player.get(PLAYERNAME_KEY),
                    (String) player.get(GROUPNAME_KEY),
                    Region.valueOf((String) player.get(REGION_KEY))),
                    (String) player.get(POSITIONSTRING_KEY),
                    (String) player.get(SESSIONID_KEY));
        }
        return playerRecord;
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        Document updatePlayerDoc = new Document();
        updatePlayerDoc.put(PLAYERID_KEY, record.getPlayerID());
        updatePlayerDoc.put(PLAYERNAME_KEY, record.getPlayerName());
        updatePlayerDoc.put(GROUPNAME_KEY, record.getGroupName());
        updatePlayerDoc.put(REGION_KEY, record.getRegion());
        updatePlayerDoc.put(POSITIONSTRING_KEY, record.getPositionAsString());
        updatePlayerDoc.put(SESSIONID_KEY, record.getSessionId());
        players.insertOne(updatePlayerDoc);
    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
        List<PlayerRecord> theList = new ArrayList<PlayerRecord>();
        Document playerDoc = new Document(POSITIONSTRING_KEY, positionString);
        List<Document> docList = new ArrayList();
        players.find(playerDoc).into(docList);
        for (Document doc : docList) {
            PlayerRecord playerRecord = new PlayerRecord(new SubscriptionRecord(
                    (String) doc.get(PLAYERID_KEY),
                    (String) doc.get(PLAYERNAME_KEY),
                    (String) doc.get(GROUPNAME_KEY),
                    Region.valueOf((String) doc.get(REGION_KEY))),
                    (String) doc.get(POSITIONSTRING_KEY),
                    (String) doc.get(SESSIONID_KEY));
            if (playerRecord.isInCave() && playerRecord.getPositionAsString().equals(positionString)) {
                theList.add(playerRecord);
            }
        }
        return theList;
    }

    @Override
    public int computeCountOfActivePlayers() {
        return getPlayerList().size();
    }

    /**
     * Compute the list of players in the cave.
     *
     * @return list of all players in the cave.
     */
    private List<PlayerRecord> getPlayerList() {
        Document notEqualsNull = new Document("$ne", null);
        Document sessionDoc = new Document(SESSIONID_KEY, notEqualsNull);
        List<Document> docList = new ArrayList();
        players.find(sessionDoc).into(docList);
        List<PlayerRecord> theList = new ArrayList<PlayerRecord>();
        for (Document doc : docList) {
            PlayerRecord playerRecord = new PlayerRecord(new SubscriptionRecord(
                    (String) doc.get(PLAYERID_KEY),
                    (String) doc.get(PLAYERNAME_KEY),
                    (String) doc.get(GROUPNAME_KEY),
                    Region.valueOf((String) doc.get(REGION_KEY))),
                    (String) doc.get(POSITIONSTRING_KEY),
                    (String) doc.get(SESSIONID_KEY));
            theList.add(playerRecord);
        }
        return theList;
    }

    @Override
    public void disconnect() {
        mongoClient.close();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return serverConfiguration;
    }
}
