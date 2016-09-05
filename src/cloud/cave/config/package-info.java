/**
 * The central configuration roles and implementations - factories, Chained
 * Property File readers, and object manager. Notably abstract factories are
 * defined for both the client side
 * {@link cloud.cave.config.StandardClientFactory} and the server side
 * {@link cloud.cave.config.StandardServerFactory}. Both relies on reading
 * properties defined in CPF files which must be read as the first step by the
 * application server (daemon) or the client (cmd). These are explained in the
 * Config constants. The default CPF reader is
 * {@link cloud.cave.config.ChainedPropertyFileReaderStrategy}
 * <p>
 * The delegates created by the factories are stored in an instance of the
 * ObjectManager which becomes an application wide 'yellow pages' / DNS of all
 * delegates.
 * <p>
 * Thus, the initialisation sequence in SkyCave is always to create a Factory
 * instance with a property reader, and pass it on to the object manager.
 * 
 * @see cloud.cave.config.Config
 * @see cloud.cave.config.ObjectManager
 * 
 */
package cloud.cave.config;

