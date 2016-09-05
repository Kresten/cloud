package cloud.cave.common;

public class CaveClassInstantiationException extends CaveException {

  private static final long serialVersionUID = -5160149027839443710L;

  public CaveClassInstantiationException(String reason) {
    super(reason);
  }

  public CaveClassInstantiationException(String message, Exception originalException) {
    super(message, originalException);
  }
  
}
