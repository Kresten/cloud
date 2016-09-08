package cloud.cave.server.common;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a record type (struct / PODO (Plain Old Data Object)) representing
 * the core data of a room. At present there is not much more to a room than a
 * textual description.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class RoomRecord {

  public String description;
  private List<String> messageList = new ArrayList<>();
  
  public RoomRecord(String description) {
    this.description = description;
  }

  public List<String> getMessageList() {
    return messageList;
  }
}
