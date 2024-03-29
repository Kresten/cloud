package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.*;

import java.util.List;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.*;

/**
 * Initial template of TDD of students' exercises
 *
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class TestWall {

    private Cave cave;

    private Player player;

    @Before
    public void setUp() throws Exception {
        cave = CommonCaveTests.createTestDoubledConfiguredCave().getCave();

        Login loginResult = cave.login("mikkel_aarskort", "123");
        player = loginResult.getPlayer();
    }

    @Test
    public void shouldWriteToAndReadWall() {
        player.addMessage("This is message no. 1");
        List<String> wallContents = player.getMessageList(0);
        assertThat(wallContents.size(), is(1));
        assertThat(wallContents.get(0), containsString("This is message no. 1"));
    }

    @Test
    public void shouldWriteTwoMessagesToAndReadWall() {
        player.addMessage("This is message no. 1");
        player.addMessage("This is message no. 2");
        List<String> wallContents = player.getMessageList(0);
        assertThat(wallContents.size(), is(2));
        assertThat(wallContents.get(0), containsString("This is message no. 1"));
        assertThat(wallContents.get(1), containsString("This is message no. 2"));
    }

    @Test
    public void shouldGetBoundedMessages() {
        for (int i = 0; i < 30; i++){
            player.addMessage("This is message no. " + i);
        }
        List<String> wallContents = player.getMessageList(0);
        assertThat(wallContents.size(), is(10));
        assertThat(wallContents.get(0), containsString("This is message no. 0"));
        assertThat(wallContents.get(1), containsString("This is message no. 1"));
    }


}
