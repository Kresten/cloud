package cloud.cave.common;

public class CaveClassNotFoundException extends CaveException {

  public CaveClassNotFoundException(String reason) {
    super(reason);
  }

  public CaveClassNotFoundException(String message, Exception originalException) {
    super(message, originalException);
  }
  
  private static final long serialVersionUID = -9068193791706498404L;

}
