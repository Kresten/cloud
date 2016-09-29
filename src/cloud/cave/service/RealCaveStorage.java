package cloud.cave.service;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Direction;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.RoomRecord;
import cloud.cave.server.common.ServerConfiguration;
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
    private String POSITION_KEY = "position";

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase("CaveStorage");
        rooms = db.getCollection("rooms");
        wallMessages = db.getCollection("wallMessages");
    }

    @Override
    public RoomRecord getRoom(String positionString) {
        Document newRoom = new Document(POSITION_KEY, positionString);
        List<Document> docList = new ArrayList();
        rooms.find(newRoom).into(docList);
        Document doc = docList.get(0);
        return new RoomRecord((String) doc.get("description"));
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        return false;
    }

    @Override
    public void addMessage(String positionString, String message) {

    }

    @Override
    public List<String> getMessageList(String positionString, int from, int amount) {
        return null;
    }

    @Override
    public List<Direction> getSetOfExitsFromRoom(String positionString) {
        return null;
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        return null;
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {

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
