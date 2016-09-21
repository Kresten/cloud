package cloud.cave.doubles;

import cloud.cave.broker.CaveTimeOutException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.WeatherService;
import org.json.simple.JSONObject;

/**
 * Created by krest on 21-09-2016.
 */
public class FakeWeatherService implements WeatherService {
    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        throw new CaveTimeOutException("*** Weather service not available, sorry. Connection timeout. Try again later. ***", null);
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public ServerConfiguration getConfiguration() {
        return null;
    }
}
