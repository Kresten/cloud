package cloud.cave.service;

import org.json.simple.JSONObject;

import cloud.cave.domain.Region;

/** Interface encapsulating the weather service.
 * <p>
 * The online weather service for SkyCave is at
 * <p>
 * http://caveweather.baerbak.com:8182/cave/weather/api/v1/(groupName)/(playerID)/(region)
 * <p>
 * where (region) is one of (Arhus/Copenhagen/Odense/Aalborg).
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */

public interface WeatherService extends ExternalService {

  /**
   * Request the weather service and get the JSON representation back.
   * 
   * @param groupName
   *          name of the group of the player requesting the weather
   * @param playerID
   *          ID of the player requesting the weather
   * 
   * @param region
   *          the region the weather is requested for
   * @return json object that represents the weather at the region OR json
   *         object that indicate that the group+playerID was not authenticated
   *         for the weather service.
   */
  JSONObject requestWeather(String groupName, String playerID, Region region);

}
