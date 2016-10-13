package cloud.cave.server;

import cloud.cave.broker.CaveStorageException;
import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.domain.Direction;
import cloud.cave.domain.Login;
import cloud.cave.domain.Player;
import cloud.cave.doubles.AllTestDoubleFactory;
import cloud.cave.doubles.FakeCaveStorage;
import cloud.cave.doubles.SaboteurCaveStorageDecorator;
import cloud.cave.service.CaveStorage;
import cloud.cave.service.RealCaveStorageFailoverDecorator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by krest on 11-10-2016.
 */
public class TestAvailabilityFailover {

    private CaveServant cave;
    private Login result;
    private Player player;
    private CaveStorage decoratedSab;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        CaveStorage saboteur = new SaboteurCaveStorageDecorator(new FakeCaveStorage());
        decoratedSab = new RealCaveStorageFailoverDecorator(saboteur);
        CaveServerFactory factory = new AllTestDoubleFactory() {
            @Override
            public CaveStorage createCaveStorageConnector(ObjectManager objMgr) {
                CaveStorage storage = decoratedSab;
                storage.initialize(null, null);
                return storage;
            }
        };

        ObjectManager objMgr = new StandardObjectManager(factory);
        cave = new CaveServant(objMgr);
        result = cave.login("magnus_aarskort", "312");
        player = result.getPlayer();
    }

    @Test
    public void shouldThrowExceptionOnComputeCountOfActivePlayers() {
        assertThat(decoratedSab.computeCountOfActivePlayers(), is(1));
        exception.expect(CaveStorageException.class);
        decoratedSab.computeCountOfActivePlayers();
    }

    @Test
    public void shouldThrowExceptionOnAddMessage() {
        player.addMessage("This is message no. 1");
        exception.expect(CaveStorageException.class);
        player.addMessage("This is message no. 1");
    }

    @Test
    public void shouldThrowExceptionOnDigRoom() {
        player.digRoom(Direction.DOWN, "This room is OK");
        exception.expect(CaveStorageException.class);
        player.digRoom(Direction.WEST, "This room throws exception");
    }

    @Test
    public void shouldThrowExceptionWhenGettingMessages() {
        player.addMessage("This is message no. 1");
        exception.expect(CaveStorageException.class);
        player.getMessageList(0);
    }

    @Test
    public void shouldThrowExceptionWhenGetting0Messages() {
        player.getMessageList(0);
        exception.expect(CaveStorageException.class);
        player.getMessageList(0);
    }

    @Test
    public void shouldThrowExceptionOnLogout() {
        exception.expect(CaveStorageException.class);
        cave.logout(player.getID());
    }

    @Test
    public void shouldThrowExceptionOnGetExitSet() {
        player.getExitSet();
        exception.expect(CaveStorageException.class);
        player.getExitSet();
    }

    @Test
    public void shouldThrowExceptionOnGetPlayersHere() {
        player.getPlayersHere();
        exception.expect(CaveStorageException.class);
        player.getPlayersHere();
    }

    @Test
    public void shouldThrowExceptionOnMove() {
        player.move(Direction.SOUTH);
        exception.expect(CaveStorageException.class);
        player.move(Direction.SOUTH);
    }

    @Test
    public void shouldThrowExceptionOnExecute() {
        exception.expect(CaveStorageException.class);
        player.execute("JumpCommand", "(0,1,0)");
    }
}
