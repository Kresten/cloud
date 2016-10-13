package cloud.cave.server;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Player;
import cloud.cave.server.common.*;
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
    private ObjectManager objectManager;


    @Override
    public void initialize(ObjectManager objMgr, ServerConfiguration config) {
        objectManager = objMgr;
        storage = objMgr.getCaveStorage();
        serverConfiguration = config;
    }

    @Override
    public Player get(String playerID) {
        //Using the approach Henrik talks about in TestLoadBalancing
        //Just creating a new player object, although he says "Never call this constructor directly"
        //When the constructor is called, it automatically refreshes from storage
        return new PlayerServant(playerID, objectManager);
    }

    @Override
    public void add(String playerID, Player player) {
        //no need
    }

    @Override
    public void remove(String playerID) {
        //no need
    }

    @Override
    public void pushPosition(String playerID, Point3 position) {
        //backtrack is disabled
    }

    @Override
    public Point3 popPosition(String playerID) {
        //backtrack is disabled
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
