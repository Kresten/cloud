package cloud.cave.broker;

/**
 * Created by krest on 20-09-2016.
 */
public class CaveTimeOutException extends CaveIPCException {
    public CaveTimeOutException(String message, Exception originalException) {
        super(message, originalException);
    }
}
