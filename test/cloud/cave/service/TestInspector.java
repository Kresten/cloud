package cloud.cave.service;

import org.junit.*;

import cloud.cave.common.Inspector;
import cloud.cave.server.SimpleInspector;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.List;

/** TDD of the Inspector's single server in-memory implementation.
 * 
 * @author Henrik Baerbak Christensen, Computer Science, Aarhus University
 *
 */
public class TestInspector {

  private static final String IPC_TOPIC = "ipc";
  private static final String EXERCISE1_TOPIC = "weather-circuit-breaker";
  
  private static final String MSG_1 = "ipc message 1";
  private static final String MSG_2 = "ipc message 2";

  private Inspector clouseau;

  @Before
  public void setup() {
    clouseau = new SimpleInspector();
  }

  @Test
  public void shouldAllowWriteAndReadOnSingleTopic() {
    
    clouseau.write(IPC_TOPIC, MSG_1);
    clouseau.write(IPC_TOPIC, MSG_2);
    
    List<String> allEntriesForIPC = clouseau.read(IPC_TOPIC);
    
    assertThat(allEntriesForIPC, notNullValue());
    assertThat(allEntriesForIPC.size(), is(2));
    assertThat(allEntriesForIPC.get(0), is(MSG_1));
    assertThat(allEntriesForIPC.get(1), is(MSG_2));
  }

  @Test
  public void shouldAllowWriteAndReadOnMultipleTopics() {
    clouseau.write(IPC_TOPIC, MSG_2);
    clouseau.write(EXERCISE1_TOPIC, MSG_1);
    
    List<String> theLog = clouseau.read(IPC_TOPIC);
    
    assertThat(theLog, notNullValue());
    assertThat(theLog.size(), is(1));
    assertThat(theLog.get(0), is(MSG_2));
    
    theLog = clouseau.read(EXERCISE1_TOPIC);
    
    assertThat(theLog, notNullValue());
    assertThat(theLog.size(), is(1));
    assertThat(theLog.get(0), is(MSG_1));
  }
  
  @Test
  public void shouldAllowResets() {
    clouseau.write(IPC_TOPIC, MSG_2);
    clouseau.write(IPC_TOPIC, MSG_1);
    clouseau.write(IPC_TOPIC, MSG_2);

    List<String> theLog = clouseau.read(IPC_TOPIC);
    assertThat(theLog.size(), is(3));
    assertThat(theLog.get(0), is(MSG_2));
    
    clouseau.reset(IPC_TOPIC);
    clouseau.write(IPC_TOPIC, MSG_1);

    theLog = clouseau.read(IPC_TOPIC);
    assertThat(theLog.size(), is(1));
    assertThat(theLog.get(0), is(MSG_1));
    
  }

  @Test
  public void shouldNeverReturnNullObjects() {
    List<String> theLog = clouseau.read("weather-timeout");
    assertThat(theLog.size(), is(0));
  }

}
