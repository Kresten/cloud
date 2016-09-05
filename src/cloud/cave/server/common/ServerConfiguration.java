package cloud.cave.server.common;

import java.util.Arrays;

import cloud.cave.common.CaveConfigurationNotSetException;
import cloud.cave.config.*;

/**
 * Record that defines the server information for a cluster of nodes.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class ServerConfiguration {

  private ServerData[] serverList;
  
  /**
   * Create a server configuration by reading a property. The strategy used to
   * do the actual read is defined by the reader strategy. The property value
   * must be in the format "(ip0):(port0),(ip1):(port1)" i.e. comma separated
   * lists of IP addresses and ports.
   * 
   * @param propertyReader
   *          the strategy used to read properties
   * @param propertyKey
   *          the property to read as a server configuration, like e.g.
   *          SKYCAVE_APPSERVER.
   */
  public ServerConfiguration(PropertyReaderStrategy propertyReader, 
      String propertyKey) {
    String asString = Config.failFastRead(propertyReader, propertyKey);
    
    String[] parts = asString.split(",");
    serverList = new ServerData[parts.length];
    
    for (int i=0; i < parts.length; i++) {
      String[] tokens = parts[i].split(":");
      serverList[i] = new StandardServerData(tokens[0], Integer.parseInt(tokens[1]));
    }
  }
  
  /**
   * Create a server configuration directly. Should not be used except for the
   * test code.
   * 
   * @param ip
   *          the ip or hostname of the server/service
   * @param port
   *          the port number of the server/service
   */
  public ServerConfiguration(String ip, int port) {
    serverList = new ServerData[1];
    serverList[0] = new StandardServerData(ip, port);
  }

  public ServerData get(int index) {
    if ( index > serverList.length ) { throw new CaveConfigurationNotSetException("ServerConfiguration: Index error, only "+serverList.length+
        " server addresses in configuration, you asked for index "+index); }
    return serverList[index];
  }
  
  public int size() {
    return serverList.length;
  }

  @Override
  public String toString() {
    return "ServerConfiguration [serverList=" + Arrays.toString(serverList) + "]";
  }

}

class StandardServerData implements ServerData {

  private String hostName;
  private int portNumber;

  public StandardServerData(String hostName, int portNumber) {
    this.hostName = hostName;
    this.portNumber = portNumber;
  }

  @Override
  public String getHostName() {
    return hostName;
  }

  @Override
  public int getPortNumber() {
    return portNumber;
  }

  @Override
  public String toString() {
    return hostName + ":" + portNumber;
  }
}