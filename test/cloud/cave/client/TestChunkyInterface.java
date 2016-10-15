package cloud.cave.client;

import cloud.cave.broker.ClientRequestHandler;
import cloud.cave.common.CommonCaveTests;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Direction;
import cloud.cave.domain.Login;
import cloud.cave.domain.Region;
import cloud.cave.doubles.LoadSpyClientRequestHandler;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by krest on 15-10-2016.
 */
public class TestChunkyInterface {
    private PlayerProxy player;
    private LoadSpyClientRequestHandler spy;

    @Before
    public void setUp() throws Exception {
        // Create the server tier
        ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();

        // create the client request handler as a test double that
        // simply uses method calls to call the 'server side'
        ClientRequestHandler crh =
                new LocalMethodCallClientRequestHandler(objMgr.getInvoker());
        spy = new LoadSpyClientRequestHandler(crh);

        // Create the cave proxy, and login mikkel
        Cave caveProxy = new CaveProxy(spy);
        Login loginResult = caveProxy.login( "mikkel_aarskort", "123");

        player = (PlayerProxy) loginResult.getPlayer();
    }

    @Test
    public void shouldSendNoRequestsForRoomDesc() {
        spy.reset();
        assertThat(player.getShortRoomDescription(), containsString("You are standing at the end of a road before a small brick building."));
        assertThat(spy.getRequestsSent(), is(0));
    }

    @Test
    public void shouldSendNoRequestsForPosition() {
        spy.reset();
        assertThat(player.getPosition(), containsString("(0,0,0)"));
        assertThat(spy.getRequestsSent(), is(0));
    }

    @Test
    public void shouldSendNoRequestsForRegion() {
        spy.reset();
        assertThat(player.getRegion(), is(Region.AARHUS));
        assertThat(spy.getRequestsSent(), is(0));
    }

    @Test
    public void shouldSendNoRequestsForRoomDescEvenWithMove() {
        spy.reset();
        assertThat(player.getShortRoomDescription(), containsString("You are standing at the end of a road before a small brick building."));
        assertThat(spy.getSent(), is(0));
        assertThat(spy.getReived(), is(0));
        assertThat(spy.getRequestsSent(), is(0));
        player.move(Direction.NORTH);
        assertThat(spy.getSent(), is(140));
        assertThat(spy.getReived(), is(152));
        assertThat(spy.getRequestsSent(), is(1));
        assertThat(player.getShortRoomDescription(), containsString("You are in open forest, with a deep valley to one side."));
        assertThat(spy.getSent(), is(140));
        assertThat(spy.getReived(), is(152));
        assertThat(spy.getRequestsSent(), is(1));

    }
}

