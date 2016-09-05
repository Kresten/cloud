package cloud.cave.server;

import java.util.*;

import cloud.cave.common.Inspector;
import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.ServerConfiguration;

/**
 * Inspector implementation using in-memory storage, suitable for a single
 * server, single thread only. No synchronization of read and write operations.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class SimpleInspector implements Inspector {

  /** The database of all log entries on all topics. */
  private Map<String, List<String>> topic2Log;
  private ServerConfiguration config;

  public SimpleInspector() {
    super();
    topic2Log = new HashMap<>();
  }

  @Override
  public void write(String topic, String logEntry) {
    List<String> topicLog = topic2Log.get(topic);
    // Create if non-existent
    if (topicLog == null) {
      topicLog = new ArrayList<>();
      topic2Log.put(topic, topicLog);
    }
    topicLog.add(logEntry);  
  }

  @Override
  public List<String> read(String topic) {
    List<String> contents = topic2Log.get(topic);
    if (contents == null) { contents = new ArrayList<>(); }
    return contents;
  }

  @Override
  public void reset(String topic) {
    ArrayList<String> topicLog = new ArrayList<>();
    topic2Log.put(topic, topicLog);
  }

  @Override
  public void initialize(ObjectManager objMgr, ServerConfiguration config) {
    this.config = config;
  }

  @Override
  public void disconnect() {
  }

  @Override
  public ServerConfiguration getConfiguration() {
    return config;
  }
}
