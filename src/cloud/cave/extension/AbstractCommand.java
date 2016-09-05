package cloud.cave.extension;

import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.Command;

/**
 * Abstract base class for command instances. Simply stores the playerID and
 * storage service reference.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public abstract class AbstractCommand implements Command {

  protected ObjectManager objectManager;
  protected String playerID;

  public AbstractCommand() {
    super();
  }

  @Override
  public void setObjectManager(ObjectManager storage) {
    this.objectManager = storage;
  }

  @Override
  public void setPlayerID(String playerID) {
    this.playerID = playerID;
  }

}