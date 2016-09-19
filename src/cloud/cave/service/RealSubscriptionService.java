package cloud.cave.service;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.server.common.SubscriptionResult;
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
 * Created by Kresten on 15-09-2016.
 */
public class RealSubscriptionService implements SubscriptionService {

    private ServerConfiguration configuration;

    @Override
    public SubscriptionRecord lookup(String loginName, String password) {
        ServerData serverData = configuration.get(0);
        String url = "http://" + serverData.getHostName() + ":" + serverData.getPortNumber();
        String path = "/api/v2/auth?loginName=" + loginName + "&password=" + password;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url + path);
        HttpResponse response;
        try {
            response = client.execute(request);
            String result = EntityUtils.toString(response.getEntity());
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(result);
            JSONObject subscription = (JSONObject) json.get("subscription");
            if ((boolean) json.get("success")) {
                return new SubscriptionRecord(
                        (String) subscription.get("playerID"),
                        (String) subscription.get("playerName"),
                        (String) subscription.get("groupName"),
                        Region.valueOf((String) subscription.get("region")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new SubscriptionRecord(SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN);
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.configuration = config;
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void disconnect() {

    }

}
