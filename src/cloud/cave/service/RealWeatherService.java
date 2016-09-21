package cloud.cave.service;

import cloud.cave.broker.CaveTimeOutException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.SocketTimeoutException;


/**
 * Created by Kresten on 19-09-2016.
 */
public class RealWeatherService implements WeatherService {

    private ServerConfiguration configuration;
    private final int CONNECTION_TIMEOUT = 3000;
    private final int SOCKET_TIMEOUT = 5000;
    private ObjectManager objectManager;

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        ServerData serverData = configuration.get(0);
        String url = "http://" + serverData.getHostName() + ":" + serverData.getPortNumber();
        String path = "/weather/api/v2/" + groupName + "/" + playerID + "/" + getCorrectRegion(region);

        RequestConfig config = RequestConfig.custom().setConnectTimeout(CONNECTION_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultRequestConfig(config);
        HttpClient client = builder.build();

        HttpGet request = new HttpGet(url + path);
        HttpResponse response;
        try {
            response = client.execute(request);
            String result = EntityUtils.toString(response.getEntity());
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(result);
            return json;
        } catch (ConnectTimeoutException cte) {
            objectManager.getInspector().write("Weather timeout", "Weather timeout: Connection");
            throw new CaveTimeOutException("*** Weather service not available, sorry. Connection timeout. Try again later. ***", cte);
        } catch (SocketTimeoutException ste) {
            objectManager.getInspector().write("Weather timeout", "Weather timeout: Slow response");
            throw new CaveTimeOutException("*** Weather service not available, sorry. Slow response. Try again later. ***", ste);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getCorrectRegion(Region region) {
        switch (region) {
            case AARHUS:
                return "Arhus";
            case AALBORG:
                return "Aalborg";
            case COPENHAGEN:
                return "Copenhagen";
            case ODENSE:
                return "Odense";
            default:
                return "Incorrect region";
        }
    }


    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.objectManager = objectManager;
        this.configuration = config;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }
}
