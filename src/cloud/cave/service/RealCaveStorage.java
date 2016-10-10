package cloud.cave.service;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Direction;
import cloud.cave.domain.Region;
import cloud.cave.server.common.*;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

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
        List<ServerAddress> serverAddresses = new ArrayList<>();
        for (int i = 0; i < serverConfiguration.size(); i++) {
            ServerData serverData = serverConfiguration.get(i);
            serverAddresses.add(new ServerAddress(serverData.getHostName(), serverData.getPortNumber()));
        }
        mongoClient = new MongoClient(serverAddresses);
        MongoDatabase db = mongoClient.getDatabase("CaveStorage");
        rooms = db.getCollection("rooms");
        wallMessages = db.getCollection("wallMessages");
        players = db.getCollection("players");
        this.addRoom(new Point3(0, 0, 0).getPositionString(), new RoomRecord(
                "You are standing at the end of a road before a small brick building."));
        this.addRoom(new Point3(0, 1, 0).getPositionString(), new RoomRecord(
                "You are in open forest, with a deep valley to one side."));
        this.addRoom(new Point3(1, 0, 0).getPositionString(), new RoomRecord(
                "You are inside a building, a well house for a large spring."));
        this.addRoom(new Point3(-1, 0, 0).getPositionString(), new RoomRecord(
                "You have walked up a hill, still in the forest."));
        this.addRoom(new Point3(0, 0, 1).getPositionString(), new RoomRecord(
                "You are in the top of a tall tree, at the end of a road."));
    }

    @Override
    public RoomRecord getRoom(String positionString) {
        Document roomDoc = rooms.find(eq(POSITION_KEY, positionString)).first();
        if (roomDoc != null) {
            return new RoomRecord((String) roomDoc.get("description"));
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
        wallMessages.find(messageDoc).into(docList);
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
            Document roomDoc = rooms.find(eq(POSITION_KEY, position)).first();
            if (roomDoc != null) {
                listOfExits.add(d);
            }
        }
        return listOfExits;
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        Document playerDoc = players.find(eq(PLAYERID_KEY, playerID)).first();
        if (playerDoc != null) {
            PlayerRecord playerRecord = new PlayerRecord(new SubscriptionRecord(
                    playerID,
                    playerDoc.get(PLAYERNAME_KEY).toString(),
                    playerDoc.get(GROUPNAME_KEY).toString(),
                    Region.valueOf(playerDoc.get(REGION_KEY).toString())),
                    playerDoc.get(POSITIONSTRING_KEY).toString(),
                    playerDoc.get(SESSIONID_KEY).toString());
            return playerRecord;
        }
        return null;
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        Bson filter = eq(PLAYERID_KEY, record.getPlayerID());
        Document updatePlayerDoc = new Document();
        updatePlayerDoc.put(PLAYERID_KEY, record.getPlayerID());
        updatePlayerDoc.put(PLAYERNAME_KEY, record.getPlayerName());
        updatePlayerDoc.put(GROUPNAME_KEY, record.getGroupName());
        updatePlayerDoc.put(REGION_KEY, record.getRegion().toString());
        updatePlayerDoc.put(POSITIONSTRING_KEY, record.getPositionAsString());
        updatePlayerDoc.put(SESSIONID_KEY, record.getSessionId());
        Document update = new Document("$set", updatePlayerDoc);
        UpdateOptions updateOptions = new UpdateOptions().upsert(true);
        players.updateOne(filter, update, updateOptions);
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
