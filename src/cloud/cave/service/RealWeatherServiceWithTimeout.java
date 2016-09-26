package cloud.cave.service;

import cloud.cave.broker.CaveTimeOutException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import org.json.simple.JSONObject;

/**
 * Created by Kresten on 22-09-2016.
 */
public class RealWeatherServiceWithTimeout implements WeatherService {

    private WeatherService decoratee;

    public RealWeatherServiceWithTimeout(){
        this.decoratee = new RealWeatherService(3, 5);
    }

    public RealWeatherServiceWithTimeout(WeatherService decoratee) {
        this.decoratee = decoratee;
    }

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        try {
            return decoratee.requestWeather(groupName,playerID, region);
        }
        catch (CaveTimeOutException e){
            JSONObject json = new JSONObject();
            json.put("authenticated", false);
            json.put("errorMessage", e.getMessage());
            return json;
        }
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        decoratee.initialize(objectManager, config);
    }

    @Override
    public void disconnect() {
        decoratee.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return decoratee.getConfiguration();
    }
}
