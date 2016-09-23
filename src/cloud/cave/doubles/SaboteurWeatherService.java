package cloud.cave.doubles;

import cloud.cave.broker.CaveTimeOutException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.WeatherService;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.simple.JSONObject;

/**
 * Created by krest on 21-09-2016.
 */
public class SaboteurWeatherService implements WeatherService {
    private ServerConfiguration configuration;
    private ObjectManager objectManager;

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        throw new CaveTimeOutException("*** Weather service not available, sorry. Connection timeout. Try again later. ***", null);
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.configuration = config;
        this.objectManager = objectManager;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }
}
