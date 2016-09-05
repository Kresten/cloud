package cloud.cave.common;

import java.util.List;

import cloud.cave.service.ExternalService;

/** The Inspector is responsible for inspecting the inner
 * workings of the implementing system holding it - thus
 * providing a 'specialized interface' as testability tactics,
 * from Bass 2013.
 * <p>
 * In the cloud computing and architecture course it serves a central
 * role as exercises are required to log specific entries (strings)
 * into the inspector log under special topics for a given exercise
 * to allow testing by an outside agent.
 * <p>
 * The inspector allows its clients to write lists of entries
 * into specific topics, i.e. a topic is a key into a list
 * of strings that are written.
 * <p>
 * In preparation for later exercises, it can be an external
 * service.
 * 
 * @author Henrik Baerbak Christensen, Computer Science, Aarhus University
 *
 */
public interface Inspector extends ExternalService {
  public static final String CFG_TOPIC = "cfg";
  public static final String IPC_TOPIC = "ipc";

  /**
   * Write a log entry into the inspector log for given topic
   * 
   * @param topic
   *          the topic to log this entry under
   * @param logEntry
   *          the log entry itself
   */
  void write(String topic, String logEntry);

  /**
   * Return all entries logged under this topic. POSTCONDITION: The returned
   * List is always non-null, an empty list is returned in case no contents is
   * associated with the topic.
   * 
   * @param topic
   *          the topic wanted
   * @return list of all entries for this topic
   */
  List<String> read(String topic);

  /** Clear all entries for this topic
   * 
   * @param topic the topic to clear
   */
  void reset(String topic);
}
