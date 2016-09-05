package cloud.cave.common;

/**
 * All exceptions that may occur in the cave derive from this root exception.
 * <p>
 * As recommended by 'uncle bob' it is an unchecked exception. Robert C. Martin:
 * "Clean Code", p. 106.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */

public class CaveException extends RuntimeException {
  private static final long serialVersionUID = -1680564492563161641L;

  public CaveException(String reason) {
    super(reason);
  }

  public CaveException(String message, Exception originalException) {
    super(message, originalException);
  }
}
