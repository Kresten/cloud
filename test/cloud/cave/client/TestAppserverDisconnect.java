package cloud.cave.client;

import cloud.cave.broker.Invoker;
import cloud.cave.common.CommonCaveTests;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;
import cloud.cave.doubles.SaboteurCRHDecoratorWithCount;
import cloud.cave.server.StandardInvoker;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Created by krest on 24-09-2016.
 */
public class TestAppserverDisconnect {

    private Cave cave;
    private SaboteurCRHDecoratorWithCount saboteur;
    private ByteArrayOutputStream baos;
    private PrintStream ps;

    @Before
    public void setup() {
        ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();
        Invoker srh = new StandardInvoker(objMgr);
        LocalMethodCallClientRequestHandler crh = new LocalMethodCallClientRequestHandler(srh);
        saboteur = new SaboteurCRHDecoratorWithCount(crh);
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);

        cave = new CaveProxy(saboteur);
    }

    @Test
    public void shouldDisconnectCorrectly() {
        // The command sequence is
        // look, who, weather, sys, exec, n, s, e, w, d, u, back, u, p, h, z, dig, u, dig,
        // post, read, exec, exit
        String cmdList = "n\nn\nn\nq";

        CmdInterpreter cmd = new CmdInterpreter(cave, "magnus_aarskort", "312",
                ps, makeToInputStream(cmdList));
        cmd.readEvalLoop();

        String output = baos.toString();

        assertThat(output, containsString("You are in open forest, with a deep valley to one side."));
        assertThat(output, containsString("*** Sorry - I cannot do that as I am disconnected from the cave, please quit ***"));
        assertThat(output, containsString("Leaving SkyCave - Goodbye."));
    }

    private InputStream makeToInputStream(String cmdList) {
        InputStream is = new ByteArrayInputStream(cmdList.getBytes());
        return is;
    }
}
