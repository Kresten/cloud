package cloud.cave.server;

import java.util.*;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Player;
import cloud.cave.server.common.*;

/**
 * Implementation of the player session cache using in-memory Map data
 * structures.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class SimpleInMemoryCache implements PlayerSessionCache {

  private Map<String,Player> cacheOfOnlinePlayer;
  private Map<String,Stack<Point3>> cacheOfPlayerTraces;
 
  public SimpleInMemoryCache() {
    cacheOfOnlinePlayer = new HashMap<>();
    cacheOfPlayerTraces = new HashMap<>();
  }

  @Override
  public Player get(String playerID) {
    Player p = cacheOfOnlinePlayer.get(playerID);
    return p;
  }

  @Override
  public void add(String playerID, Player player) {
    cacheOfOnlinePlayer.put(playerID, player);
    Stack<Point3> herStack = new Stack<>();
    cacheOfPlayerTraces.put(playerID, herStack); 
  }

  @Override
  public void remove(String playerID) {
    cacheOfOnlinePlayer.remove(playerID);
    cacheOfPlayerTraces.remove(playerID); 
  }

  @Override
  public void pushPosition(String playerID, Point3 position) {
    cacheOfPlayerTraces.get(playerID).push(position);
  }

  @Override
  public Point3 popPosition(String playerID) {
    Stack<Point3> stack = cacheOfPlayerTraces.get(playerID);
  
    // Pop top element, handle empty stack gracefully
    Point3 p = null;
    if (!stack.empty()) { p = stack.pop();  }
    return p;
  }

  @Override
  public String toString() {
    return "SimpleInMemoryCache";
  }

  // === ExternalService handling
  private ServerConfiguration serverConfiguration;

  @Override
  public void initialize(ObjectManager objMgr, ServerConfiguration config) {
    this.serverConfiguration = config;
  }

  @Override
  public ServerConfiguration getConfiguration() {
    return serverConfiguration;
  }
  
  
}
