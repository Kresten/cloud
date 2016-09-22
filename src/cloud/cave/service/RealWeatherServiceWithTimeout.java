package cloud.cave.service;

import cloud.cave.broker.CaveTimeOutException;
import cloud.cave.common.Inspector;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created by Kresten on 22-09-2016.
 */
public class RealWeatherServiceWithTimeout extends RealWeatherService{

    private int connectionTimeout;
    private int socketTimeout;
    private ObjectManager objectManager;

    public RealWeatherServiceWithTimeout(int connectionTimeout, int socketTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
    }

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        String urlAndPath = getUrlAndPath(groupName, playerID, region);
        HttpClient client = getHttpClientWithTimeout(connectionTimeout, socketTimeout);
        HttpGet request = new HttpGet(urlAndPath);
        try {
            return executeRequest(client, request);
        } catch (ConnectTimeoutException cte) {
            objectManager.getInspector().write(Inspector.WEATHER_TIMEOUT_TOPIC, "Weather timeout: Connection");
            throw new CaveTimeOutException("*** Weather service not available, sorry. Connection timeout. Try again later. ***", cte);
        } catch (SocketTimeoutException ste) {
            objectManager.getInspector().write(Inspector.WEATHER_TIMEOUT_TOPIC, "Weather timeout: Slow response");
            throw new CaveTimeOutException("*** Weather service not available, sorry. Slow response. Try again later. ***", ste);
        } catch (ParseException e1) {
            e1.printStackTrace();
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }
}
