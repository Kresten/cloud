package cloud.cave.server;

import cloud.cave.broker.Invoker;
import cloud.cave.client.CaveProxy;
import cloud.cave.client.CmdInterpreter;
import cloud.cave.common.CommonCaveTests;
import cloud.cave.config.ObjectManager;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Created by krest on 25-09-2016.
 */
public class TestBoundedMessageList {

    private ByteArrayOutputStream baos;
    private PrintStream ps;
    private CaveProxy caveProxy;
    private String cmdList;

    @Before
    public void setup() {
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);

        ObjectManager om = CommonCaveTests.createTestDoubledConfiguredCave();
        Invoker srh = new StandardInvoker(om);
        LocalMethodCallClientRequestHandler crh = new LocalMethodCallClientRequestHandler(srh);
        caveProxy = new CaveProxy(crh);
        cmdList = "";
        for (int i = 0; i < 25; i++){
            cmdList += "\npost Message "+i;
        }
    }

    @Test
    public void shouldGet10FirstMessagesOnly() {
        cmdList += "\nread\nq";

        CmdInterpreter cmd = new CmdInterpreter(caveProxy, "magnus_aarskort", "312",
                ps, makeToInputStream(cmdList));
        cmd.readEvalLoop();

        String output = baos.toString();

        for (int i = 0; i < 10; i++){
            assertThat(output, containsString("[Magnus] Message "+i));
        }
        assertThat(output, not(containsString("[Magnus] Message 10")));
    }

    @Test
    public void shouldGet20FirstMessagesOnly() {
        cmdList += "\nread\nread\nq";

        CmdInterpreter cmd = new CmdInterpreter(caveProxy, "magnus_aarskort", "312",
                ps, makeToInputStream(cmdList));
        cmd.readEvalLoop();

        String output = baos.toString();

        for (int i = 0; i < 20; i++){
            assertThat(output, containsString("[Magnus] Message "+i));
        }
        assertThat(output, not(containsString("[Magnus] Message 20")));
    }

    @Test
    public void shouldGet25FirstMessages() {
        cmdList += "\nread\nread\nread\nq";

        CmdInterpreter cmd = new CmdInterpreter(caveProxy, "magnus_aarskort", "312",
                ps, makeToInputStream(cmdList));
        cmd.readEvalLoop();

        String output = baos.toString();

        for (int i = 0; i < 25; i++){
            assertThat(output, containsString("[Magnus] Message "+i));
        }
        assertThat(output, not(containsString("[Magnus] Message 25")));
    }

    @Test
    public void shouldOnlyGet10FirstMessagesWhenOtherCommandsAreWrittenInBetweenReads() {
        cmdList += "\nread\nl\nread\nq";

        CmdInterpreter cmd = new CmdInterpreter(caveProxy, "magnus_aarskort", "312",
                ps, makeToInputStream(cmdList));
        cmd.readEvalLoop();

        String output = baos.toString();

        for (int i = 0; i < 10; i++){
            assertThat(output, containsString("[Magnus] Message "+i));
        }
        assertThat(output, not(containsString("[Magnus] Message 10")));
    }

    private InputStream makeToInputStream(String cmdList) {
        InputStream is = new ByteArrayInputStream(cmdList.getBytes());
        return is;
    }
}

