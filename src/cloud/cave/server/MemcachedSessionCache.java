package cloud.cave.server;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Player;
import cloud.cave.server.common.Point3;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Stack;

/**
 * Created by Kresten on 13-10-2016.
 */
public class MemcachedSessionCache implements PlayerSessionCache {

    private ObjectManager objectManager;
    private ServerConfiguration serverConfiguration;
    private MemcachedClient memcacheClient;
    private final int EXPIRE_TIME = 0;

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
        return new PlayerServant(playerID, objectManager);
    }

    @Override
    public void add(String playerID, Player player) {
        //used to create stack for player
        Stack<Point3> herStack = new Stack<>();
        memcacheClient.add(playerID, EXPIRE_TIME, herStack);
    }

    @Override
    public void remove(String playerID) {
        //used to delete stack for player
        memcacheClient.delete(playerID);
    }

    @Override
    public void pushPosition(String playerID, Point3 position) {
        Stack<Point3> stack = (Stack<Point3>) memcacheClient.get(playerID);
        stack.push(position);
        memcacheClient.set(playerID, EXPIRE_TIME, stack);
    }

    @Override
    public Point3 popPosition(String playerID) {
        Stack<Point3> stack = (Stack<Point3>) memcacheClient.get(playerID);

        // Pop top element, handle empty stack gracefully
        Point3 p = null;
        if (!stack.empty()) {
            p = stack.pop();
            memcacheClient.set(playerID, EXPIRE_TIME, stack);
        }
        return p;
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return serverConfiguration;
    }
}
