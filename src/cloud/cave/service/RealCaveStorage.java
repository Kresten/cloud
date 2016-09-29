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

    private MongoCollection<Document> rooms;
    private MongoCollection<Document> wallMessages;
    private MongoCollection<Document> players;
    private final String POSITION_KEY = "position";
    private final String DESCRIPTION_KEY = "description";
    private final String MESSAGE_KEY = "message";
    private final String PLAYER_KEY = "player";

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase("CaveStorage");
        rooms = db.getCollection("rooms");
        wallMessages = db.getCollection("wallMessages");
        players = db.getCollection("players");
    }

    @Override
    public RoomRecord getRoom(String positionString) {
        Document roomDoc = new Document(POSITION_KEY, positionString);
        List<Document> docList = new ArrayList();
        rooms.find(roomDoc).into(docList);
        if (!docList.isEmpty()){
            Document doc = docList.get(0);
            return new RoomRecord((String) doc.get("description"));
        }
        else {
            return null;
        }
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        RoomRecord roomRecord = getRoom(positionString);
        if (roomRecord!=null){
            return false;
        }
        else {
            Document roomDoc = new Document();
            roomDoc.put(POSITION_KEY, positionString);
            roomDoc.put(DESCRIPTION_KEY, description);
            rooms.insertOne(roomDoc);
            return true;
        }
    }

    @Override
    public void addMessage(String positionString, String message) {
        Document addMessage = new Document();
        addMessage.put(POSITION_KEY, positionString);
        addMessage.put(MESSAGE_KEY, message);
        wallMessages.insertOne(addMessage);
    }

    @Override
    public List<String> getMessageList(String positionString, int from, int amount) {
        Document messageDoc = new Document(POSITION_KEY, positionString);
        List<Document> docList = new ArrayList();
        rooms.find(messageDoc).into(docList);
        List<String> messageList = new ArrayList();
        for (Document doc : docList){
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
            if (!docList.isEmpty()){
                listOfExits.add(d);
            }
        }
        return listOfExits;
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        Document playerDoc = new Document(PLAYER_KEY, playerID);
        List<Document> docList = new ArrayList();
        players.find(playerDoc).into(docList);
        PlayerRecord playerRecord = null;
        if (!docList.isEmpty()){
            Document player = docList.get(0);
            playerRecord = new PlayerRecord(new SubscriptionRecord(
                    (String) player.get("playerId"),
                    (String) player.get("playerName"),
                    (String) player.get("groupName"),
                    Region.valueOf((String) player.get("region"))),
                    (String) player.get("positionString"),
                    (String) player.get("sessionId"));
        }
        return playerRecord;
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        //        playerId2PlayerSpecs.put(record.getPlayerID(), record);
        Document addMessage = new Document();
        addMessage.put(PLAYER_KEY, record.getPlayerID());
        wallMessages.insertOne(addMessage);
    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
        return null;
    }

    @Override
    public int computeCountOfActivePlayers() {
        return 0;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public ServerConfiguration getConfiguration() {
        return null;
    }
}
