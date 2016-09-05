package cloud.cave.config;

import cloud.cave.broker.ClientRequestHandler;
import cloud.cave.server.common.ServerConfiguration;

/**
 * Concrete ClientFactory that uses a property reader to create
 * delegates for the client side. After creation, each service delegate is
 * configured through their 'initialize' method with their service end point
 * configuration, again based upon reading their respective properties.
 * 
 * @see Config
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class StandardClientFactory implements CaveClientFactory {

  private PropertyReaderStrategy propertyReader;

  public StandardClientFactory(PropertyReaderStrategy envReader) {
    propertyReader = envReader;
  }

  @Override
  public ClientRequestHandler createClientRequestHandler() {
    ClientRequestHandler crh = null; 
    crh = Config.loadAndInstantiate(propertyReader, 
        Config.SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION, crh);

    // Read in the server configuration
    ServerConfiguration config = 
        new ServerConfiguration(propertyReader, Config.SKYCAVE_APPSERVER);
    crh.initialize(config);

    return crh;
  }
}
