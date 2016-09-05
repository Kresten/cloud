package cloud.cave.server;

import java.util.*;

import org.json.simple.JSONObject;

import cloud.cave.broker.*;
import cloud.cave.common.*;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;

/**
 * Servant implementation of Player (Servant role in Broker), that is, the
 * domain implementation on the server side.
 * <p>
 * Interacts with underlying persistent storage and cache for mutator methods,
 * and some of the more complex accessor methods.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class PlayerServant implements Player {

  /**
   * The classpath used to search for Command objects
   */
  public static final String EXTENSION_CLASSPATH = "cloud.cave.extension";

  private String ID;
  private String sessionId;

  // These attributes of the player are essentially
  // caching of the 'true' information which is stored in
  // the underlying cave storage.
  private String name;
  private String groupName;
  private Region region;
  private RoomRecord currentRoom;
  private String position;

  private ObjectManager objectManager;

  /* It makes sense to cache the storage connector as we
   * use it very often.
   */
  private CaveStorage storage;

  /**
   * Never call this constructor directly, use cave.login() instead! Create a
   * player instance bound to the given delegates for session caching,
   * persistence, etc.
   * 
   * @param playerID
   *          the player's id
   * @param objectManager
   *          the object manager holding all delegates
   */
  PlayerServant(String playerID, ObjectManager objectManager) {
    this.ID = playerID;
    this.objectManager = objectManager;
    this.storage = objectManager.getCaveStorage();

    refreshFromStorage();
  }

  @Override
  public String getName() {
    return name;
  }

  public String getGroupName() {
    return groupName;
  }

  @Override
  public String getID() {
    return ID;
  }

  @Override
  public String getShortRoomDescription() {
    return currentRoom.description;
  }

  @Override
  public String getLongRoomDescription() {
    throw new CaveException("getLongRoomDescription is not defined by the servant object.");
    /*
    String allOfIt = getShortRoomDescription()+"\nThere are exits in directions:\n";
    for ( Direction dir : getExitSet()) {
      allOfIt += "  "+dir+" ";
    }
    allOfIt += "\nYou see other players:\n";
    List<String> playerNameList = getPlayersHere();
    int count = 0;
    for ( String p : playerNameList ) {
      allOfIt += "  ["+count+"] " + p;
      count ++;
    }
    return allOfIt;*/
  }

  @Override
  public List<Direction> getExitSet() {
    // Cannot use cache, others may influence cave 
    return storage.getSetOfExitsFromRoom(position);
  }

  @Override
  public Region getRegion() {
    return region; // Use the cached value
  }

  @Override
  public String getPosition() {
    return position; // Use the cached value
  }

  @Override
  public List<String> getPlayersHere() {
    List<PlayerRecord> playerList = storage.computeListOfPlayersAt(getPosition());
    List<String> playerNameList = new ArrayList<String>();
    for (PlayerRecord record: playerList) {
      playerNameList.add(record.getPlayerName());
    }
    return playerNameList;
  }
  
  @Override
  public String getSessionID() {
    return sessionId; // Use the cached value
  }

  @Override
  public void addMessage(String message) {
    // TODO: Exercise - solve the 'wall' exercise
  }

  @Override
  public List<String> getMessageList() {
    // TODO: Exercise - solve the 'wall' exercise
    List<String> contents = new ArrayList<String>();
    contents.add("NOT IMPLEMENTED YET");
    return contents;
  }

  @Override
  public String getWeather() {
    JSONObject weatherAsJson = 
        objectManager.
        getWeatherService().
        requestWeather(getGroupName(), getID(), getRegion());
    String weather = convertToFormattedString(weatherAsJson);
    return weather;
  }
  
  /**
   * Convert a JSON object that represents weather in the format of the cave
   * weather service into the string representation defined for the cave UI.
   * 
   * @param currentObservation
   *          weather information formatted as JSON
   * @return formatted string describing the weather, or the
   *          error message in case something did not succeed
   */
  private String convertToFormattedString(JSONObject currentObservation) {

    String result = null;
    if (currentObservation.get("authenticated").equals("true")) {
      String temperature = currentObservation.get("temperature").toString();
      double tempDouble = Double.parseDouble(temperature);

      String feelslike = currentObservation.get("feelslike").toString();
      double feelDouble = Double.parseDouble(feelslike);

      String winddir = currentObservation.get("winddirection").toString();

      String windspeed = currentObservation.get("windspeed").toString();
      double windSpDouble = Double.parseDouble(windspeed);

      String weather = currentObservation.get("weather").toString();

      String time = currentObservation.get("time").toString();

      // For reproducible results, we default to US locale, otherwise
      // tests fail depending on computer locale
      result = String
          .format(
              Locale.US,
              "The weather in %s is %s, temperature %.1fC (feelslike %.1fC). Wind: %.1f m/s, direction %s. This report is dated: %s.",
              getRegion().toString(), weather, tempDouble, feelDouble,
              windSpDouble, winddir, time);
    } else {
      result = "The weather service failed with message:\n"
          + currentObservation.get("errorMessage");
    }
    return result;
  }

  @Override
  public boolean move(Direction direction) {
    // Convert present room position into Point3 which
    // allows computations
    Point3 presentPosition = Point3.parseString(position);
    
    // Clone it; we need the values of both present and
    // new position
    Point3 newPosition = (Point3) presentPosition.clone();
    
    // Calculate a new position given the movement direction
    newPosition.translate(direction);
    // convert to the new position in string format
    String newPositionAsString = newPosition.getPositionString();
    // get the room in that direction
    RoomRecord newRoom = storage.getRoom(newPositionAsString);
    
    // if it is null, then there is no room in that direction
    // and we return without any state modifications
    if ( newRoom == null ) { return false; }
    
    // push the position of the room we are leaving onto
    // back track stack.
    objectManager.getPlayerSessionCache().pushPosition(getID(),
        presentPosition);

    updateStateAndStorageToNewPosition(newPositionAsString, newRoom);
    
    return true;
  }
  
  @Override
  public boolean backtrack() {
    // Pop the position of 'last visited room' from the
    // cache
    Point3 previousPosition = objectManager.getPlayerSessionCache().
        popPosition(getID());
    
    // If the cache is empty we do nothing
    if (previousPosition == null) { return false; }
    
    String newPositionAsString = previousPosition.getPositionString();
    
    // get the room in that direction
    RoomRecord newRoom = storage.getRoom(newPositionAsString);
    
    updateStateAndStorageToNewPosition(newPositionAsString, newRoom);

    return true;
  }

  private void updateStateAndStorageToNewPosition(String newPositionAsString,
      RoomRecord newRoom) {
    // update internal state variables
    position = newPositionAsString;
    currentRoom = newRoom;

    // and update this player's position in the storage
    PlayerRecord pRecord = storage.getPlayerByID(getID());
    pRecord.setPositionAsString(position);
    storage.updatePlayerRecord(pRecord);
  }

  @Override
  public boolean digRoom(Direction direction, String description) {
    // Calculate the offsets in the given direction
    Point3 p = Point3.parseString(position);
    p.translate( direction);
    RoomRecord room = new RoomRecord(description);
    return storage.addRoom(p.getPositionString(), room);
  }

  @Override
  public JSONObject execute(String commandName, String... parameters) {
    // Compute the qualified path of the command class that shall be loaded
    String qualifiedClassName = EXTENSION_CLASSPATH + "." + commandName;

    // Load it
    Class<?> theClass = null;
    try {
      theClass = Class.forName(qualifiedClassName);
    } catch (ClassNotFoundException e) {
      return Marshaling.createInvalidReplyWithExplanation(StatusCode.SERVER_FAILED_TO_LOAD_COMMAND,
          "Player.execute failed to load Command class: "+commandName);
    }
    
    // Next, instantiate the command object
    Command command = null; 
    try {
      command = (Command) theClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      return Marshaling.createInvalidReplyWithExplanation(StatusCode.SERVER_FAILED_TO_INSTANTIATE_COMMAND,
          "Player.execute failed to instantiate Command object: "+commandName);
    }
    
    // Initialize the command object
    command.setPlayerID(getID());
    command.setObjectManager(objectManager);
    
    // And execute the command...
    JSONObject reply = command.execute(parameters);
    
    // as the command may update any aspect of the player' data
    // and as we cache it here locally, invalidate the caching
    refreshFromStorage();
    
    return reply;
  }
  
  /**
   * Query the storage for the player record associated with the player ID, and
   * update all cached instance variables according to the read state.
   */
  private void refreshFromStorage() {
    PlayerRecord pr = storage.getPlayerByID(ID);
    name = pr.getPlayerName();
    groupName = pr.getGroupName();
    position = pr.getPositionAsString();
    region = pr.getRegion();
    sessionId = pr.getSessionId(); 

    currentRoom = storage.getRoom(position);
  }

  @Override
  public String toString() {
    return "PlayerServant [storage=" + storage + ", name=" + name
        + ", ID=" + ID + ", region=" + region + ", currentRoom=" + currentRoom
        + ", position=" + position + ", sessionId=" + sessionId + "]";
  }

}
