package cloud.cave.server;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Player;
import cloud.cave.server.common.Point3;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Kresten on 13-10-2016.
 */
public class MemcachedSessionCache implements PlayerSessionCache {

    private ObjectManager objectManager;
    private ServerConfiguration serverConfiguration;
    private MemcachedClient memcacheClient;

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.objectManager = objectManager;
        serverConfiguration = config;
        ServerData serverData = serverConfiguration.get(0);
        try {
            memcacheClient = new MemcachedClient(new InetSocketAddress(serverData.getHostName(), serverData.getPortNumber()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Player get(String playerID) {
        Player player = (Player) memcacheClient.get(playerID);
        if (player == null) {
            player = new PlayerServant(playerID, objectManager);
            add(playerID, player);
        }
        return player;
    }

    @Override
    public void add(String playerID, Player player) {
        memcacheClient.set(playerID, 3600, player);
    }

    @Override
    public void remove(String playerID) {

    }

    @Override
    public void pushPosition(String playerID, Point3 position) {

    }

    @Override
    public Point3 popPosition(String playerID) {
        return null;
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return null;
    }
}
