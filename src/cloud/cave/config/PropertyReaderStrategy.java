package cloud.cave.config;

/**
 * Strategy (FRS, p. 130) for accessing global properties set for
 * the project. For production, use the implementation that reads
 * chained property files.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public interface PropertyReaderStrategy {

  /**
   * Read the value of a property of the given name.
   * 
   * @param key
   *          name of the property whose value must be read
   * @return the value of the property
   */
  String getValue(String key);

}
