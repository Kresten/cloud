package cloud.cave.server.common;

/**
 * Record defining the host name and port number of a single node/server.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public interface ServerData {

  String getHostName();

  int getPortNumber();

}
