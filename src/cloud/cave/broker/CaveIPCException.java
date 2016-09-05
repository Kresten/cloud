package cloud.cave.broker;

import cloud.cave.common.CaveException;

/**
 * All IPC (inter process communication) exceptions derive from this root
 * exception.
 * <p>
 * As recommended by 'uncle bob' it is an unchecked exception. Robert C. Martin:
 * "Clean Code", p. 106.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class CaveIPCException extends CaveException {

  private static final long serialVersionUID = -6391176877460888747L;
  
  public CaveIPCException(String message, Exception originalException) {
    super(message, originalException);
  }

}
