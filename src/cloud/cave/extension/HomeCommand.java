package cloud.cave.extension;

import org.json.simple.JSONObject;

import cloud.cave.broker.Marshaling;
import cloud.cave.server.PlayerSessionCache;
import cloud.cave.server.common.*;
import cloud.cave.service.CaveStorage;

/**
 * An implementation of a command that 'flies the player home' to the entry room
 * (0,0,0).
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class HomeCommand extends AbstractCommand implements Command {

  @Override
  public JSONObject execute(String... parameters) {
    Point3 home = new Point3(0, 0, 0);
    CaveStorage storage = objectManager.getCaveStorage();
    PlayerRecord pRecord = storage.getPlayerByID(playerID);

    // Update the session cache to allow proper 'back' tracking
    PlayerSessionCache cache = objectManager.getPlayerSessionCache();
    cache.pushPosition(playerID, Point3.parseString(pRecord.getPositionAsString()));
    
    // Update position in the storage
    pRecord.setPositionAsString(home.getPositionString());
    storage.updatePlayerRecord(pRecord);

    JSONObject reply;
    reply =
        Marshaling.createValidReplyWithReturnValue("You went home to position "+home.getPositionString());
    return reply;
  }


}
