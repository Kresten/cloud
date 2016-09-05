package cloud.cave.doubles;

import java.util.*;

import cloud.cave.common.Inspector;
import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.ServerConfiguration;

/** NullObject implementation. Still must
 * return non-null objects.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class NullInspector implements Inspector {

  @Override
  public void write(String topic, String logEntry) {
  }

  @Override
  public List<String> read(String topic) {
    List<String> nothing = new ArrayList<>();
    nothing.add("(Empty: NullInspector records nothing.)");
    return nothing;
  }

  @Override
  public void reset(String topic) {
  }

  @Override
  public void initialize(ObjectManager objMgr, ServerConfiguration config) {
  }

  @Override
  public void disconnect() {
  }

  @Override
  public ServerConfiguration getConfiguration() {
    return new ServerConfiguration("null-configuration",0);
  }

}
