package cloud.cave.broker;

/**
 * The Requestor role on the client side, encapsulate creation (marhalling),
 * handling and sending request messages on behalf of the client proxies.
 * The Requestor sends messages using its associated ClientRequestHandler.
 * <p>
 * In SkyCave, it is just a marker interface, as our proxies play
 * both ClientProxy and Requestor roles.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public interface Requestor {

}
