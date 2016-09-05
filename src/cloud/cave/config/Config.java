package cloud.cave.config;

import cloud.cave.common.*;

/**
 * Config encapsulates the names of CPF file property keys that must be set in
 * order for the factories to create proper delegate configurations of the
 * server and client side of the cave system.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class Config {

  /**
   * Property that must be set to 'name:port' of the endpoint for
   * the application server. In case of a cluster, separate each endpoint with
   * ';'.
   */
  public static final String SKYCAVE_APPSERVER = "SKYCAVE_APPSERVER";

  /**
   * Property that must be set to the 'name:port' of the database
   * server. Separate with ';' in case of a cluster.
   */
  public static final String SKYCAVE_DBSERVER = "SKYCAVE_DBSERVER";

  /**
   * Property that must be set to the 'name:port' of the
   * subscription server end point.
   */
  public static final String SKYCAVE_SUBSCRIPTIONSERVER = "SKYCAVE_SUBSCRIPTIONSERVER";

  /**
   * Property that must be set to the 'name:port' of the weather
   * server end point.
   */
  public static final String SKYCAVE_WEATHERSERVER = "SKYCAVE_WEATHERSERVER";

  /**
   * Property that must be set to the fully qualified class name of
   * the class implementing the cave storage interface. This class must be in
   * the classpath and will be loaded at runtime by the ServerFactory.
   */
  public static final String SKYCAVE_CAVESTORAGE_IMPLEMENTATION = "SKYCAVE_CAVESTORAGE_IMPLEMENTATION";

  /**
   * Property that must be set to the fully qualified class name of
   * the class implementing the subscription service interface. This class must
   * be in the classpath and will be loaded at runtime by the ServerFactory.
   */
  public static final String SKYCAVE_SUBSCRIPTION_IMPLEMENTATION = "SKYCAVE_SUBSCRIPTION_IMPLEMENTATION";

  /**
   * Property that must be set to the fully qualified class name of
   * the class implementing the weather service interface. This class must be in
   * the classpath and will be loaded at runtime by the ServerFactory.
   */
  public static final String SKYCAVE_WEATHER_IMPLEMENATION = "SKYCAVE_WEATHER_IMPLEMENTATION";

  /**
   * Property that must be set to the fully qualified class name of
   * the class implementing the weather service interface. This class must be in
   * the classpath and will be loaded at runtime by the ServerFactory.
   */
  public static final String SKYCAVE_SERVERREQUESTHANDLER_IMPLEMENTATION = "SKYCAVE_SERVERREQUESTHANDLER_IMPLEMENTATION"; 

  /**
   * Property that must be set to the fully qualified class name of
   * the class implementing the client request handler interface. This class must be in
   * the classpath and will be loaded at runtime by the ClientFactory.
   */
  public static final String SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION = "SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION";

  /**
   * Property key whose value must be a fully qualified class name of the class
   * implementing the PlayerSessionCache. This class must be in the classpath
   * and will be loaded at runtime by the ServerFactory.
   */
  public static final String SKYCAVE_PLAYERSESSIONCACHE_IMPLEMENTATION = "SKYCAVE_PLAYERSESSIONCACHE_IMPLEMENTATION";

  /**
   * Property key whose value defines the hostname/ip of a session cache server.
   */
  public static final String SKYCAVE_PLAYERSESSIONCACHESERVER = "SKYCAVE_PLAYERSESSIONCACHESERVER";

  /**
   * Property key whose value must be a fully qualified class name of the class
   * implementing the Inspector. This class must be in the classpath
   * and will be loaded at runtime by the ServerFactory.
   */
  public static final String SKYCAVE_INSPECTOR_IMPLEMENTATION = "SKYCAVE_INSPECTOR_IMPLEMENTATION";

  /** Property key whoe value defines the hostname/ip of a potential
   * external inspector.
   */
  public static final String SKYCAVE_INSPECTORSERVER = "SKYCAVE_INSPECTORSERVER";
  
  /**
   * Read a property using the given reader strategy. Fail immediately in case
   * the property is not set.
   * 
   * @param propertyReader
   *          the property reader strategy to be used to read properties
   * @param key
   *          the key for the property to be read
   * @return the value of the property with the given key
   * @throws CaveConfigurationNotSetException
   *           in case the property is not set
   */
  public static String failFastRead(PropertyReaderStrategy propertyReader, String key) {
    String value = propertyReader.getValue(key);
    if (value == null || value.equals("")) {
      throw new CaveConfigurationNotSetException("ConfigurationError: The configuration is not defined because"
          + " the configuration property with key '" + key + "' is not set.");
    }
    return value;
  }

  /**
   * Generic method to load and instantiate object of type T which is on the
   * path given by a property.
   * 
   * @param <T>
   *          type parameter defining the class type of the object to
   *          instantiate
   * @param propertyReader
   *          the strategy for reading the property
   * @param keyOfProperty
   *          the key of the property that holds the full path to the class to
   *          load
   * @param theObject
   *          actually a dummy but its type tells the method the generic type
   * @return object of type T loaded from the fully qualified type name given by
   *         the property
   */
  @SuppressWarnings("unchecked")
  public static <T> T loadAndInstantiate(PropertyReaderStrategy propertyReader,
      String keyOfProperty,
      T theObject) {
    // read full path of class to load
    String qualifiedNameOfType;
    qualifiedNameOfType = 
        Config.failFastRead(propertyReader, keyOfProperty);

    // Use java reflection to read in the class
    Class<?> theClass = null;
    try {
      theClass = Class.forName(qualifiedNameOfType);
    } catch (ClassNotFoundException e) {
      throw new CaveClassNotFoundException("Factory error: Class '"
          +qualifiedNameOfType+"' is not found."+
          "Property key : "+keyOfProperty);
    }
    
    // Next, instantiate object from the class 
    try {
      theObject = (T) theClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new CaveClassInstantiationException("Factory error: Class '"
          +qualifiedNameOfType+"' could not be instantiated!", e);
    }
    
    return theObject;
  }
}
