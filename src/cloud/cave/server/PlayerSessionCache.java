package cloud.cave.server;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Player;
import cloud.cave.server.common.*;

/**
 * The role of a cache for player related objects, notably
 * the player object itself (so less need to fetch it from
 * the database), and a stack of positions (to allow
 * backtracking using the 'back' command in the cmd).
 * <p>
 * It is an ExternalService in preparation for implementing
 * the cache using real distributed caching, alas, the
 * cache resides on dedicated caching nodes.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public interface PlayerSessionCache {

  /**
   * Get the player object corresponding to the given player id.
   * 
   * @param playerID
   *          the id of the plaeyr
   * @return null if no player id is stored in the cache, otherwise the player
   *         object
   */
  Player get(String playerID);

  /**
   * Add a player instance under the given player id
   * 
   * @param playerID
   *          id to store player instance under
   * @param player
   *          the player instance to cache
   */
  void add(String playerID, Player player);

  /**
   * Remove the player instance for the given player id from the cache
   * 
   * @param playerID
   *          player id of player instance to remove
   */
  void remove(String playerID);

  /**
   * Push a position onto the stack of moves made by a given player, allowing
   * backtracking through visited rooms using the same route.
   * 
   * @param playerID
   *          id of the player to push position for
   * @param position
   *          the room position to push
   */
  void pushPosition(String playerID, Point3 position);

  /**
   * Pop last position pushed for the given player ID.
   * 
   * @param playerID
   *          id of the player to pop position for
   * @return position the room position on top of stack
   */
  Point3 popPosition(String playerID);
  
  /**
   * Initialize the cache. Must be run before any other method.
   * 
   * @param objectManager
   *          the object manager of SkyCave
   * 
   * @param config
   *          the configuration of the cache if using external services
   * 
   */
  void initialize(ObjectManager objectManager, ServerConfiguration config);

  /** Get the configuration of this cache.
   * 
   * @return the configuration
   */
  public ServerConfiguration getConfiguration();

}
