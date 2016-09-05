package cloud.cave.common;

public class CaveConfigurationNotSetException extends CaveException {

  private static final long serialVersionUID = -2747237182710093603L;

  public CaveConfigurationNotSetException(String reason) {
    super(reason);
  }

}
