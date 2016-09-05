package cloud.cave.config;

import cloud.cave.broker.ClientRequestHandler;

/** Abstract factory for the client of SkyCave, creating
 * the delegates that are configurable.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public interface CaveClientFactory {

  /** Create the request handler for the client side.
   * 
   * @return a new client request handler
   */
  ClientRequestHandler createClientRequestHandler();

}
