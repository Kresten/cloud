package cloud.cave.service;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;


/**
 * Created by Kresten on 19-09-2016.
 */
public class RealWeatherService implements WeatherService {

    private ServerConfiguration configuration;

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        ServerData serverData = configuration.get(0);

        String url = "http://" + serverData.getHostName() + ":" + serverData.getPortNumber();
        String path = "/weather/api/v2/" + groupName + "/" + playerID + "/" + getCorrectRegion(region);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url + path);
        HttpResponse response;
        try {
            response = client.execute(request);
            String result = EntityUtils.toString(response.getEntity());
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(result);
            System.out.println(json.get("authenticated"));
            System.out.println(json.get("temperature"));
            System.out.println(json.get("feelslike"));
            System.out.println(json.get("winddirection"));
            return json;
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
