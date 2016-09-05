package cloud.cave.main;

import java.io.File;

import org.slf4j.*;

import cloud.cave.config.*;
import cloud.cave.domain.Cave;

/**
 * The 'main' daemon to run on the server side. It uses a ServerFactory that
 * reads all relevant parameters to define the server side delegates
 * (subscription service, database connector, server request handler implementation, IPs and
 * ports of connections...).
 * 
 * @see Config
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class CaveDaemon {
  
  private static Thread daemon; 

  public static void main(String[] args) throws InterruptedException {
    
    // Create the logging
    Logger logger = LoggerFactory.getLogger(CaveDaemon.class);
    
    String cpfFilename = args[0];
 
    // Create the abstract factory to create delegates using dependency injection.
    // The daemon always uses CPF files for defining delegates.
    CaveServerFactory factory; 
    PropertyReaderStrategy propertyReader;
    propertyReader = new ChainedPropertyFileReaderStrategy(new File(cpfFilename));
    factory = new StandardServerFactory(propertyReader);
    
    // Create the object manager that creates and holds all delegate references
    // for global access - a sort of lookup service/DNS/yellow pages for delegates
    ObjectManager objManager = new StandardObjectManager(factory);

    Cave cave = objManager.getCave();

    // Make a section in the log file, marking the new session
    logger.info("=== SkyCave Server Request Handler starting...");
    logger.info("Cave Configuration =" + cave.describeConfiguration());

    // Welcome 
    System.out.println("=== SkyCave Daemon ==="); 
    System.out.println(" Cpf File = " + cpfFilename); 
    
    System.out.println(" Use Ctrl-c to terminate!"); 
    
    // and start the daemon...
    daemon = new Thread(objManager.getServerRequestHandler()); 
    daemon.start(); 
    
    // Ensure that its lifetime follows that of the main process
    daemon.join(); 
  }
}
