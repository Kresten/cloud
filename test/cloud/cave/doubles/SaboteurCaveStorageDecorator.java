package cloud.cave.doubles;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Direction;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.RoomRecord;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.CaveStorage;
import com.mongodb.MongoSocketReadException;

import java.util.List;

/**
 * Created by krest on 11-10-2016.
 */
public class SaboteurCaveStorageDecorator implements CaveStorage {

    private final CaveStorage decoratee;
    private int count;

    public SaboteurCaveStorageDecorator(CaveStorage decoratee) {
        this.decoratee = decoratee;
        count = 0;
    }

    @Override
    public RoomRecord getRoom(String positionString) {
        if (count < 5) {
            count++;
            return decoratee.getRoom(positionString);
        } else {
            throw new MongoSocketReadException("This is a faked exception", null);
        }
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        if (count < 5) {
            count++;
            return decoratee.addRoom(positionString, description);
        } else {
            throw new MongoSocketReadException("This is a faked exception", null);
        }
    }

    @Override
    public void addMessage(String positionString, String message) {
        if (count < 5) {
            count++;
            decoratee.addMessage(positionString, message);
        } else {
            throw new MongoSocketReadException("This is a faked exception", null);
        }
    }

    @Override
    public List<String> getMessageList(String positionString, int from, int amount) {
        if (count < 5) {
            count++;
            return decoratee.getMessageList(positionString, from, amount);
        } else {
            throw new MongoSocketReadException("This is a faked exception", null);
        }
    }

    @Override
    public List<Direction> getSetOfExitsFromRoom(String positionString) {
        if (count < 5) {
            count++;
            return decoratee.getSetOfExitsFromRoom(positionString);
        } else {
            throw new MongoSocketReadException("This is a faked exception", null);
        }
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        if (count < 5) {
            count++;
            return decoratee.getPlayerByID(playerID);
        } else {
            throw new MongoSocketReadException("This is a faked exception", null);
        }
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        if (count < 5) {
            count++;
            decoratee.updatePlayerRecord(record);
        } else {
            throw new MongoSocketReadException("This is a faked exception", null);
        }
    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
        if (count < 5) {
            count++;
            return decoratee.computeListOfPlayersAt(positionString);
        } else {
            throw new MongoSocketReadException("This is a faked exception", null);
        }
    }

    @Override
    public int computeCountOfActivePlayers() {
        if (count < 5) {
            count++;
            return decoratee.computeCountOfActivePlayers();
        } else {
            throw new MongoSocketReadException("This is a faked exception", null);
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
