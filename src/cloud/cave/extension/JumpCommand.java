package cloud.cave.extension;

import org.json.simple.JSONObject;

import cloud.cave.broker.Marshaling;
import cloud.cave.server.PlayerSessionCache;
import cloud.cave.server.common.*;
import cloud.cave.service.CaveStorage;

/**
 * A command pattern based implementation of a jump command, that allows a
 * player to instantly move to a specific room.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class JumpCommand extends AbstractCommand implements Command {

  @Override
  public JSONObject execute(String... parameters) {
    String positionString = parameters[0];
    
    CaveStorage storage = objectManager.getCaveStorage();
    
    // Validate that the position is known in the cave
    RoomRecord room = storage.getRoom(positionString);
    if ( room == null ) {
      return Marshaling.createValidReplyWithReturnValue("false", "JumpCommand failed, room " 
          + positionString + " does not exist in the cave.");
    }
    
    PlayerRecord pRecord = storage.getPlayerByID(playerID);

    // Update the session cache to allow proper 'back' tracking
    PlayerSessionCache cache = objectManager.getPlayerSessionCache();
    cache.pushPosition(playerID, Point3.parseString(pRecord.getPositionAsString()));

    // Update the position in storage
    pRecord.setPositionAsString(positionString);
    storage.updatePlayerRecord(pRecord);

    JSONObject reply =
        Marshaling.createValidReplyWithReturnValue("true", 
            "You jumped to position: "+positionString);

    return reply;
  }

}
