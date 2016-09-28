package cloud.cave.server.common;

/**
 * Created by Kresten on 28-09-2016.
 */
public class RealTimeStrategy implements TimeStrategy {
    @Override
    public long getTime() {
        return System.currentTimeMillis();
    }
}
