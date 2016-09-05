package cloud.cave.doubles;

import cloud.cave.broker.*;
import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.ServerConfiguration;

/** NullObject implementation.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class NullServerRequestHandler implements ServerRequestHandler {

  @Override
  public void run() {
  }

  @Override
  public void initialize(ObjectManager objectManager, ServerConfiguration config) {
  }

}
