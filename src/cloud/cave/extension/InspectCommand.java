package cloud.cave.extension;

import java.util.List;

import org.json.simple.JSONObject;

import cloud.cave.broker.*;
import cloud.cave.common.Inspector;

/**
 * Use the inspector through a command. Avoid introducing a lot of new
 * dispatching, avoids extending the Cave interface, and besides it is mostly
 * automated testing methods that will use the inspector anyway...
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class InspectCommand extends AbstractCommand {

  public static final String INSPECT_COMMAND_CLASSNAME = "InspectCommand";
  public static final String WRITE = "write";
  public static final String READ = "read";
  public static final String RESET = "reset";

  @Override
  public JSONObject execute(String... parameters) {
    String subcommand = parameters[0];
    String topic = parameters[1];
    
    Inspector inspector = objectManager.getInspector();
    
    JSONObject reply;
    // WRITE
    if (subcommand.equals(WRITE)) {
      String entry = parameters[2];
      inspector.write(topic, entry);
      reply =
          Marshaling.createValidReplyWithReturnValue("Wrote on topic " + topic + ".");
      
    } // READ 
    else if (subcommand.equals(READ)) {
      List<String> result = inspector.read(topic);

      String[] contents = result.toArray(new String[result.size()]);

      reply = Marshaling.createValidReplyWithReturnValue("Inspector log on topic "+topic+" is:", contents);

    } // RESET
    else if (subcommand.equals(RESET)) {
      inspector.reset(topic);
      reply =
          Marshaling.createValidReplyWithReturnValue("Reset log on topic " + topic + ".");
      
    } // ERROR
    else {
      reply =
          Marshaling.createInvalidReplyWithExplanation(StatusCode.SERVER_FAILED_TO_INSTANTIATE_COMMAND, 
              "The subcommand '"+subcommand+"' is not known.");
    }

    return reply;
  }
}
