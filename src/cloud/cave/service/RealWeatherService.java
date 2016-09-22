package cloud.cave.service;

import cloud.cave.broker.CaveTimeOutException;
import cloud.cave.common.Inspector;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
    private ObjectManager objectManager;
    private int connectionTimeout;
    private int socketTimeout;

    public RealWeatherService() {
        this.connectionTimeout = 0;
        this.socketTimeout = 0;
    }


    public RealWeatherService(int connectionTimeout, int socketTimeout) {
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

    protected JSONObject executeRequest(HttpClient client, HttpGet request) throws IOException, ParseException {
        HttpResponse response;
        response = client.execute(request);
        String result = EntityUtils.toString(response.getEntity());
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(result);
        return json;
    }


    protected HttpClient getHttpClientWithTimeout(int connectionTimeout, int socketTimeout) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (connectionTimeout == 0 && socketTimeout == 0) {
            RequestConfig config = RequestConfig.custom().setConnectTimeout(connectionTimeout).setSocketTimeout(socketTimeout).build();
            builder.setDefaultRequestConfig(config);
        }
        return builder.build();
    }

    protected String getUrlAndPath(String groupName, String playerID, Region region) {
        ServerData serverData = getConfiguration().get(0);
        String url = "http://" + serverData.getHostName() + ":" + serverData.getPortNumber();
        String path = "/weather/api/v2/" + groupName + "/" + playerID + "/" + getCorrectRegion(region);
        return url + path;
    }

    protected String getCorrectRegion(Region region) {
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
