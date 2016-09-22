package cloud.cave.service;

import cloud.cave.broker.CaveTimeOutException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * Created by Kresten on 22-09-2016.
 */
public class RealWeatherServiceWithCircuitBreaker extends RealWeatherService{

    private int connectionTimeout;
    private int socketTimeout;
    private ObjectManager objectManager;
    private CircuitBreaker circuitBreaker;

    public RealWeatherServiceWithCircuitBreaker() {
        this.connectionTimeout = 3;
        this.socketTimeout = 5;
        circuitBreaker = new WeatherCircuitBreaker();
        circuitBreaker.setInspector(objectManager.getInspector());
    }

    public RealWeatherServiceWithCircuitBreaker(int connectionTimeout, int socketTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
        circuitBreaker = new WeatherCircuitBreaker();
        circuitBreaker.setInspector(objectManager.getInspector());
    }

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        String urlAndPath = getUrlAndPath(groupName, playerID, region);
        HttpClient client = getHttpClientWithTimeout(connectionTimeout, socketTimeout);
        HttpGet request = new HttpGet(urlAndPath);
        HttpResponse response;
        try {
            if (circuitBreaker.getState().equals(CircuitBreakerState.OPEN) && !circuitBreaker.hasTimeOutPassed(System.currentTimeMillis())){
                throw new CaveTimeOutException("*** Weather service not available, sorry. Open state ***", null);
            }
            response = client.execute(request);
            String result = EntityUtils.toString(response.getEntity());
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(result);
            circuitBreaker.reset();
            return json;
        } catch (IOException e) {
            circuitBreaker.incrementFailureCount();
            throw new CaveTimeOutException("*** Weather service not available, sorry. Closed state ***", e);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}