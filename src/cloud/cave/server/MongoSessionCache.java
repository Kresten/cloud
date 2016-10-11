package cloud.cave.server;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Player;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.Point3;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import cloud.cave.service.CaveStorage;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Kresten on 11-10-2016.
 */
public class MongoSessionCache implements PlayerSessionCache {

    private CaveStorage storage;
    private ServerConfiguration serverConfiguration;


    @Override
    public void initialize(ObjectManager objMgr, ServerConfiguration config) {
        storage = objMgr.getCaveStorage();
        serverConfiguration = config;
    }

    @Override
    public Player get(String playerID) {
        //maybe make method that gets player instead of playerRecord
        PlayerRecord playerByID = storage.getPlayerByID(playerID);
        playerByID.getSessionId();
        return null;
    }

    @Override
    public void add(String playerID, Player player) {
        //add the player to storage somehow
    }

    @Override
    public void remove(String playerID) {
        //remove from storage by playerID
    }

    @Override
    public void pushPosition(String playerID, Point3 position) {
        //no need I'd say
    }

    @Override
    public Point3 popPosition(String playerID) {
        //undo is disabled
        return null;
    }

    @Override
    public String toString() {
        return "MongoSessionCache";
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return serverConfiguration;
    }
}
