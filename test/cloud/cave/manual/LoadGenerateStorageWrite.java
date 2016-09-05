package cloud.cave.manual;

import java.io.File;

import cloud.cave.config.*;
import cloud.cave.domain.*;
import cloud.cave.server.CaveServant;

/**
 * Manual load generator. Generates 10.000 player.addRoom() requests to the
 * Cave, thereby forcing a large number of writes to the underlying storage
 * system. Use it to manually 'smoke out' MongoDB exceptions during replica
 * failover.
 * <p>
 * Do NOT use this on the production server :-)
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class LoadGenerateStorageWrite {
  public static void main(String[] args) {
    
    System.out.println("*** Load Generator: Generate writes in the storage ***");
    
    String cpfFilename = args[0];
    
    CaveServerFactory factory; 
    PropertyReaderStrategy envReader;

    envReader = new ChainedPropertyFileReaderStrategy(new File(cpfFilename));
    factory = new StandardServerFactory(envReader);
    ObjectManager objMgr = new StandardObjectManager(factory);
    
    // Create the server side cave instance
    Cave cave = new CaveServant(objMgr);
    
    System.out.println("--> Cave initialized; cfg = "+cave.describeConfiguration());

    // Login reserved user
    Login result = cave.login("reserved_aarskort", "cloudarch");
    System.out.println("--> login result: "+result);
    
    // assume it went ok
    Player player = result.getPlayer();

    System.out.println("--> player logged into cave");
    
    System.out.println("** Initialized, will start digging DOWN ***");
    
    // Generate load
    final int max = 10000;
    boolean wentOk = true;
    for (int i = 0; i < max; i++) {
      if (i%100 == 0) { System.out.print("."); }
      if (i%1000 == 0) { System.out.println(); }
      String roomDescription = "This is room no. "+i;
      wentOk = player.digRoom(Direction.DOWN, roomDescription);
      if ( ! wentOk ) {
        System.out.println("ERROR: The cave is not empty, failed on digging room at position: "+player.getPosition());
        System.exit(-1); // Fail fast...
      }
      // move down then
      player.move(Direction.DOWN);
    }
    System.out.println();
    System.out.println("*** Done. Remember to erase DB manually before attempting a new run. ***");
  }
}
