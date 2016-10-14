package cloud.cave.server;

import cloud.cave.common.Inspector;
import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kresten on 14-10-2016.
 */
public class MemcachedInspector implements Inspector {

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
    public void write(String topic, String logEntry) {
        List<String> topicLog = (List<String>) memcacheClient.get(topic);
        // Create if non-existent
        if (topicLog == null) {
            topicLog = new ArrayList<>();
        }
        topicLog.add(logEntry);
        memcacheClient.set(topic, EXPIRE_TIME, topicLog);
    }

    @Override
    public List<String> read(String topic) {
        List<String> contents = (List<String>) memcacheClient.get(topic);
        if (contents == null) { contents = new ArrayList<>(); }
        return contents;
    }

    @Override
    public void reset(String topic) {
        ArrayList<String> topicLog = new ArrayList<>();
        memcacheClient.set(topic, EXPIRE_TIME, topicLog);
    }

    @Override
    public void disconnect() {

    }

    @Override
    public ServerConfiguration getConfiguration() {
        return serverConfiguration;
    }
}
