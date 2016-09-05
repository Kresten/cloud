package cloud.cave.server.common;

import org.json.simple.JSONObject;

import cloud.cave.config.ObjectManager;

/**
 * The Command role in the Command pattern (Flexible Reliable Software, p 308.)
 * <p>
 * An instance of Player may take an instance of a command and execute it.
 * <p>
 * Any instance of this interface will be executed as a 'template method' call
 * where the methods 'setPlayerID' and 'setStorageService' has already been
 * called before the 'execute' method is invoked.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public interface Command {

  /**
   * This method is invoked just before the 'execute' method and defines the ID
   * of the player executing the command
   * 
   * @param playerID
   *          id of the player
   */
  void setPlayerID(String playerID);

  /**
   * This method is invoked just before the 'execute' method and defines the
   * object manager used by the SkyCave.
   * 
   * @param objMgr
   *          the object manager
   */
  void setObjectManager(ObjectManager objMgr);

  /**
   * Perform the execution of the command instance.
   * 
   * @param parameters
   *          a variable length list of string parameters to be interpreted by
   *          the actual command
   * @return a JSON object that is the return value of executing the command,
   *         the format is ideally that defined by the marshaling functions in
   *         the IPC package.
   */
  JSONObject execute(String... parameters);

}
