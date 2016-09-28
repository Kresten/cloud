package cloud.cave.server.common;

/**
 * Created by Kresten on 28-09-2016.
 */
public class FakeTimeStrategy implements TimeStrategy {

    private long time = 0;

    @Override
    public long getTime() {
        return time;
    }

    public void incrementTime(){
        time++;
    }
}
