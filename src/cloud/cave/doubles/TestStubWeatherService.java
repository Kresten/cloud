package cloud.cave.doubles;

import org.json.simple.JSONObject;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.WeatherService;

/**
 * Stub that outputs example weather information in a format identical to the
 * real cave weather service. This stub is hard-coded to accept only one of
 * the two groups defined in the stub registration service!
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class TestStubWeatherService implements WeatherService {

  private ServerConfiguration configuration;

  @SuppressWarnings("unchecked")
  @Override
  public JSONObject requestWeather(String groupName, String playerID,
      Region region) {
    // Ex of output from the weather service:
    // {"windspeed":"1.8","time":"Wed, 17 Jun 2015 16:50:26 +0200","weather":"Light Rain","winddirection":"ESE","feelslike":"12.5","temperature":"12.5"}

    JSONObject weather = new JSONObject();

    // Validate player id
    if (groupName.equals("grp01")) {
      weather.put("authenticated", "true");
      weather.put("errorMessage", "OK");

      weather.put("windspeed", "1.2");
      weather.put("winddirection", "West");
      weather.put("weather", "Clear");
      weather.put("temperature", "27.4");
      weather.put("feelslike", "-2.7");
      weather.put("time", "Thu, 05 Mar 2015 09:38:37 +0100");
    } else {
      weather.put("authenticated", "false");
      weather.put("errorMessage", "GroupName " + groupName + " or playerID "
          + playerID + " is not authenticated");
    }
    return weather;
  }

  @Override
  public void initialize(ObjectManager objMgr, ServerConfiguration config) {
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
