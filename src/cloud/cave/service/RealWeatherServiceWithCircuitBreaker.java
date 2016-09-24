package cloud.cave.service;

import cloud.cave.broker.CaveTimeOutException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import org.json.simple.JSONObject;

/**
 * Created by Kresten on 22-09-2016.
 */
public class RealWeatherServiceWithCircuitBreaker implements WeatherService{

    private WeatherService decoratee;
    private CircuitBreaker circuitBreaker;

    public RealWeatherServiceWithCircuitBreaker(WeatherService decoratee) {
        this.decoratee = decoratee;
        circuitBreaker = new WeatherCircuitBreaker();
    }

    public RealWeatherServiceWithCircuitBreaker(WeatherService decoratee, double timeToWait) {
        this.decoratee = decoratee;
        circuitBreaker = new WeatherCircuitBreaker(timeToWait);
    }

    public RealWeatherServiceWithCircuitBreaker() {
        this.decoratee = new RealWeatherService(3, 5);
        circuitBreaker = new WeatherCircuitBreaker();
    }

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        try {
            if (circuitBreaker.getState().equals(CircuitBreakerState.OPEN) && !circuitBreaker.hasTimeOutPassed(System.currentTimeMillis())) {
                throw new CaveTimeOutException("*** Weather service not available, sorry. (Open Circuit) ***", null);
            }
            JSONObject json = decoratee.requestWeather(groupName, playerID, region);
            circuitBreaker.reset();
            return json;
        } catch (CaveTimeOutException e) {
            circuitBreaker.incrementFailureCount();
            JSONObject json = new JSONObject();
            json.put("authenticated", false);
            String errorMessage = e.getMessage();
            if(!errorMessage.equals("*** Weather service not available, sorry. (Open Circuit) ***")){
                errorMessage = "*** Weather service not available, sorry. (Closed Circuit) ***";
            }
            json.put("errorMessage", errorMessage);
            return json;
        }
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        decoratee.initialize(objectManager, config);
        circuitBreaker.setInspector(objectManager.getInspector());
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