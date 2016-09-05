package cloud.cave.broker;

import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.ServerConfiguration;

/**
 * The server request handler role in the Broker pattern. This is solely a
 * marker interface as it may be implemented in numerous ways depending upon
 * choice of library/framework.
 * <p>
 * Responsibility: To define a specific IPC protocol and listen to any incoming
 * network messages, and forward them to an associated Invoker instance, and
 * return any ReplyObjects from the Invoker to reply messages on the network.
 * <p>
 * It is associated with a ClientRequestHandler on the client side of the
 * network which (of course) understands a mutually agreed protocol.
 * <p>
 * However implemented, it should always spawn thread(s) to handle incoming
 * network requests.
 * <p>
 * As an instance is created dynamically by the ServerFactory there cannot be
 * any other constructors than the default one. Be SURE to invoke the
 * 'initialize' method BEFORE the run() method is invoked in your concrete
 * implementation of this role.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public interface ServerRequestHandler extends Runnable {

  /**
   * Initialize the server request handler with the server configuration. This
   * HAS to be executed BEFORE the run() method is invoked.
   * 
   * @param objectManager
   *          the object manager
   * @param config
   *          the configuration of IP and ports.
   */
  void initialize(ObjectManager objectManager, ServerConfiguration config);
}
