package cloud.cave.service;

import cloud.cave.broker.CaveStorageException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Direction;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.RoomRecord;
import cloud.cave.server.common.ServerConfiguration;
import com.mongodb.MongoException;

import java.util.List;

/**
 * Created by krest on 11-10-2016.
 */
public class RealCaveStorageFailoverDecorator implements CaveStorage {

    private CaveStorage decoratee;

    public RealCaveStorageFailoverDecorator(CaveStorage decoratee) {
        this.decoratee = decoratee;
    }

    @Override
    public RoomRecord getRoom(String positionString) {
        try {
            return decoratee.getRoom(positionString);
        } catch (MongoException e) {
            throw new CaveStorageException("*** Cannot get room. The database is currently unavailable ***", e);
        }
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        try {
            return decoratee.addRoom(positionString, description);
        } catch (MongoException e) {
            throw new CaveStorageException("*** Cannot dig room. The database is currently unavailable ***", e);
        }

    }

    @Override
    public void addMessage(String positionString, String message) {
        try {
            decoratee.addMessage(positionString, message);
        } catch (MongoException e) {
            throw new CaveStorageException("*** Cannot add message to wall. The database is currently unavailable ***", e);
        }
    }

    @Override
    public List<String> getMessageList(String positionString, int from, int amount) {
        try {
            return decoratee.getMessageList(positionString, from, amount);
        } catch (MongoException e) {
            throw new CaveStorageException("*** Cannot get wall messages. The database is currently unavailable ***", e);
        }
    }

    @Override
    public List<Direction> getSetOfExitsFromRoom(String positionString) {
        try {
            return decoratee.getSetOfExitsFromRoom(positionString);
        } catch (MongoException e) {
            throw new CaveStorageException("*** Cannot get exits. The database is currently unavailable ***", e);
        }
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        try {
            return decoratee.getPlayerByID(playerID);
        } catch (MongoException e) {
            throw new CaveStorageException("*** Cannot find player. The database is currently unavailable ***", e);
        }
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        try {
            decoratee.updatePlayerRecord(record);
        } catch (MongoException e) {
            throw new CaveStorageException("*** Cannot update player. The database is currently unavailable ***", e);
        }
    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
        try {
            return decoratee.computeListOfPlayersAt(positionString);
        } catch (MongoException e) {
            throw new CaveStorageException("*** Cannot compute list of players. The database is currently unavailable ***", e);
        }
    }

    @Override
    public int computeCountOfActivePlayers() {
        try {
            return decoratee.computeCountOfActivePlayers();
        } catch (MongoException e) {
            throw new CaveStorageException("*** Cannot compute number of active players. The database is currently unavailable ***", e);
        }
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        decoratee.initialize(objectManager, config);
    }

    @Override
    public void disconnect() {
        decoratee.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return decoratee.getConfiguration();
    }
}
