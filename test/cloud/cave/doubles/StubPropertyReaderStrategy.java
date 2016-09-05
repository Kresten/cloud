package cloud.cave.doubles;

import java.util.*;

import cloud.cave.config.PropertyReaderStrategy;

/**
 * Test stub (FRS, chapter 12) AND a spy (FRS, sidebar 12.1) for reading
 * properties and for verifying that the proper values are read.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class StubPropertyReaderStrategy implements
    PropertyReaderStrategy {

  private class Pair {
    public String key; public String value;
    public Pair(String key, String value) {
      this.key = key; this.value = value;
    }
  }
  
  private List<Pair> valueOfNextRead;
  int index;
  
  public StubPropertyReaderStrategy() {
      valueOfNextRead = new ArrayList<Pair>();
      index = 0;
  }

  @Override
  public String getValue(String key) {
    // Verify that UnitUnderTest is trying to access the expected
    // property (Spy behavior)
    Pair pair = valueOfNextRead.get(index);
    String expected = pair.key;
    if (! expected.equals(key)) {
      throw new RuntimeException("StubPropertyReaderStrategy: Expected property "+expected+" but "+
          "instead property "+key+" was attemted to be read.");
    }
    index++;
    // And return the value of the env variable (Stub behaviour)
    return pair.value;
  }


  public void setNextExpectation(String key, String value) {
    valueOfNextRead.add(new Pair(key,value));
  }

}
