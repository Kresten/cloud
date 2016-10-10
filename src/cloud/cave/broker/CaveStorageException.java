package cloud.cave.broker;

/**
 * Created by Kresten on 10-10-2016.
 */
public class CaveStorageException extends CaveIPCException {
    public CaveStorageException(String message, Exception originalException) {
        super(message, originalException);
    }
}
