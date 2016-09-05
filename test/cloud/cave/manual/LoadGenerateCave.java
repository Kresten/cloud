package cloud.cave.manual;

import java.io.File;
import java.util.List;

import cloud.cave.broker.ClientRequestHandler;
import cloud.cave.client.CaveProxy;
import cloud.cave.config.*;
import cloud.cave.domain.*;

/**
 * Manual load generator on the Cave daemon. You can configure
 * how many users to start, as well as how many iterations
 * of a small scenario that each player should execute.
 *
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class LoadGenerateCave {

  public static void main(String[] args) {
    System.out.println("*** Load Generator: Generate random user actions on the cave ***");
    if (args.length < 2) {
      System.out.println("Usage: LoadGenerateCave [cpf file] [count player] [iterations]");
      System.exit(-1);
    }
    
    String cpfFilename = args[0];
    int countPlayer = Integer.parseInt(args[1]);
    int countIteration = Integer.parseInt(args[2]);
    
    LoadGenerateCave loader = new LoadGenerateCave(cpfFilename);
    loader.loadWith(countPlayer, countIteration);
  }

  private CaveProxy cave;
  
  public LoadGenerateCave(String cpfFilename) {
    CaveClientFactory factory; 
    PropertyReaderStrategy envReader;

    envReader = new ChainedPropertyFileReaderStrategy(new File(cpfFilename));
    factory = new StandardClientFactory(envReader);
    
    ClientRequestHandler requestHandler = factory.createClientRequestHandler();
    cave = new CaveProxy(requestHandler);
    
    String cfg = cave.describeConfiguration();
    System.out.println("--> Cave initialized; cfg = "+cfg);
    if (! cfg.contains("NullSubscriptionService")) {
      System.out.println("*** ERROR: You can only load generate on cave, if it is configured with the");
      System.out.println("*** ERROR:   NullSubscriptionService.");
      System.exit(-1);
    }
  }

  Thread[] players = null;
  private void loadWith(int countPlayers, int countIterations) {
    players = new Thread[countPlayers];
    
    // create workers and start them
    for(int i = 0; i < countPlayers; i++) {
      // Generate a random player name with very little probability
      // of overlap in case we run multiple instances of this program
      // at the same time
      int randomNumber = (int)(Math.random()*99999);
      String loginName = "Player # " + i + "/" + randomNumber;
      Runnable worker = new SinglePlayerWorker(cave, loginName, countIterations);
      Thread t = new Thread(worker);
      t.start();
      players[i] = t;
      pauseABit();
    }
    
    // join all to main thread
    for(int i = 0; i < countPlayers; i++) {
      try {
        players[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  
  public static void pauseABit() {
    try {
      long sleeptime = 850 + (long) (Math.random()*500L); // 850-1250 ms 
      Thread.sleep(sleeptime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}

/** Worker thread for a single player, exploring the cave
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
class SinglePlayerWorker implements Runnable {

  private String loginName;
  private int countIterations;
  private Cave cave;

  public SinglePlayerWorker(Cave cave, String loginName, int countIterations) {
    this.cave = cave;
    this.loginName = loginName;
    this.countIterations = countIterations;
  }

  @Override
  public void run() {
    Login loginResult = null;
    
    // We simulate multiple client but within the
    // SAME program using multiple threads. The
    // client side abstracts are NOT written for
    // concurrency (as clients are expected to have
    // only a single thread). Therefore we must
    // avoid deadlocks/race conditions and other
    // nasty concurrency issues using 'client-side
    // synchronization': As the methods
    // are not synchronized themselves, we
    // synchronize them from the client
    // side...
    synchronized (this) {
      loginResult = cave.login( loginName, "no-care");
    }
    
    System.out.println("*** Entering player "+loginName);
    Player p = loginResult.getPlayer();
    for (int i = 0; i < countIterations; i++) {
      synchronized(this) { 
        exploreTheCave(p, i); 
      }
    }
    
    synchronized (this) {
      cave.logout(p.getID());
    }
    System.out.println("*** Leaving player "+loginName);
  }

  private void exploreTheCave(Player player, int iteration) {
    Direction d; List<Direction> exits; boolean isValid;
    
    // move to a random existing room
    exits = player.getExitSet();
    int n = randomBetween0AndN(exits.size());
    d = exits.get( n );
    isValid = player.move(d);
    assert isValid == true;

    System.out.println("- Player "+ player.getName()+ " moved "+d+"\n  - to '"+player.getShortRoomDescription()+"'");
    pauseABit();
    
    // try to dig a room
    exits = player.getExitSet();
    if ( exits.size() < 6 ) {
      // find a direction without a room
      for( Direction potential : Direction.values() ) {
        if ( ! exits.contains(potential)) {
          d = potential;
        }
      }

      // Dig the room
      isValid = player.digRoom(d, "You are in the room made by "+player.getName()+" in iteration "+iteration);
      assert isValid == true ;
      System.out.println("- Player "+ player.getName()+ " dug room at "+player.getPosition());
      // and move there to avoid being too much stuck
      assert player.move(d);
    }
    pauseABit();
    // look around!
    player.getLongRoomDescription();
    player.getExitSet();
    pauseABit();
    player.getPlayersHere();
    player.getPosition();
  }

  private void pauseABit() {
    LoadGenerateCave.pauseABit();
  }

  private int randomBetween0AndN(int n) {
    return (int) (Math.random()*n);
  }


}
