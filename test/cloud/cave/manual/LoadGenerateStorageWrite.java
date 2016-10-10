package cloud.cave.manual;

import java.io.File;

import cloud.cave.broker.ClientRequestHandler;
import cloud.cave.client.CaveProxy;
import cloud.cave.config.*;
import cloud.cave.domain.*;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;
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

  private static final int NO_ROOMS_TO_DIG = 10000;

  public static void main(String[] args) {

    String cpfFilename = args[0];

    System.out.println("*** Load Generator: Generate writes in the storage ***");
    System.out.println("  Cpf = "+cpfFilename);

    CaveServerFactory factory;
    PropertyReaderStrategy envReader;

    // Create the server side delegates based upon the CPF configuration
    envReader = new ChainedPropertyFileReaderStrategy(new File(cpfFilename));
    factory = new StandardServerFactory(envReader);
    ObjectManager objMgr = new StandardObjectManager(factory);

    // Create the server side cave instance
    Cave cave = new CaveServant(objMgr);
    System.out.println("--> CaveServant initialized; cfg = "+cave.describeConfiguration());

    // Create a client request handler that does in-VM
    // calls to the server side
    ClientRequestHandler crh = new LocalMethodCallClientRequestHandler(objMgr.getInvoker());

    // Create the cave proxy
    Cave caveProxy = new CaveProxy(crh);

    // and login Mikkel
    Login result = caveProxy.login("mikkel_aarskort", "123");
    System.out.println("--> login result: "+result);

    if (! LoginResult.isValidLogin(result.getResultCode())) {
      System.out.println("Not a valid user, remember you MUST use the TestStubSubscriptionService");
      System.exit(-1);
    }

    // Now we know the player is valid, let us go to work...
    Player player = result.getPlayer();

    System.out.println("--> player logged into cave");

    System.out.println("*** Initialized, will start digging DOWN. Do the stepDown while writing! ***");

    // Generate the load by digging a lot of rooms...
    final int max = NO_ROOMS_TO_DIG;
    boolean wentOk = true;
    for (int i = 0; i < max; i++) {
      if (i%100 == 0) { System.out.print("."); }
      if (i%1000 == 0) { System.out.println(); }
      String roomDescription = "This is room no. "+i;
      wentOk = player.digRoom(Direction.DOWN, roomDescription);
      if ( ! wentOk ) {
        System.out.println("ERROR: The cave is not empty, failed on digging room at position: " + player.getPosition());
      }
      // move down then
      player.move(Direction.DOWN);
    }
    System.out.println();
    System.out.println("*** Done. Remember to erase DB manually before attempting a new run. ***");
  }
}