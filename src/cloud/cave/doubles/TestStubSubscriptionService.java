package cloud.cave.doubles;

import java.util.*;

import org.mindrot.jbcrypt.BCrypt;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;

/**
 * A test stub implementation of the subscription storage. It initially knows
 * only three loginNames, and their associated passwords and playerNames.
 * <p>
 * Note that the hardcoded playerIDs (user-001 .. user-003) are also
 * hardcoded in the stub weather service, thus changing these will 
 * require rewriting a lot of test code and stub code.
 * <p>
 * Note that the implementation here does NOT store passwords but uses jBCrypt
 * to store password hashes, which is a standard technique to guard
 * passwords safely even if a database's contents is stolen
 *  
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class TestStubSubscriptionService implements SubscriptionService {
  
  public TestStubSubscriptionService() {
    super();
    subscriptionMap = new HashMap<String,SubscriptionPair>();
    // populate with the three users known by all test cases
    subscriptionMap.put("mikkel_aarskort", 
        new SubscriptionPair("123",
        new SubscriptionRecord("user-001","Mikkel", "grp01", Region.AARHUS)));
    subscriptionMap.put("magnus_aarskort", 
        new SubscriptionPair("312",
        new SubscriptionRecord("user-002","Magnus", "grp01", Region.COPENHAGEN)));
    subscriptionMap.put("mathilde_aarskort", 
        new SubscriptionPair("321",
        new SubscriptionRecord("user-003","Mathilde", "grp02", Region.AALBORG)));
    // and populate with a single 'reserved' user which is used by the
    // course's automatic testing system. Leave this reserved login
    // in the test stub because otherwise our grading system will not
    // pass its tests and then you will not get the proper points for
    // your score. The reserved user is not used by any of the test
    // cases.
    subscriptionMap.put("reserved_aarskort", 
        new SubscriptionPair("cloudarch",
        new SubscriptionRecord("user-reserved","ReservedCrunchUser", "zzz0", Region.AARHUS)));
    
  }

  private class SubscriptionPair {
    public SubscriptionPair(String password, SubscriptionRecord record) {
      String salt = BCrypt.gensalt(4); // Preferring faster over security
      String hash = BCrypt.hashpw(password, salt);
      
      this.bCryptHash = hash;
      this.subscriptionRecord = record;
    }
    public String bCryptHash;
    public SubscriptionRecord subscriptionRecord;
  }
  
  /** A database 'table' that has loginName as primary key (key)
   * and the subscription record as value.
   */
  private Map<String, SubscriptionPair> subscriptionMap;
  private ServerConfiguration configuration;

  @Override
  public SubscriptionRecord lookup(String loginName, String password) {
    SubscriptionPair pair = subscriptionMap.get(loginName);

    // Verify that loginName+pwd match a valid subscription
    if (pair == null || 
        ! BCrypt.checkpw(password, pair.bCryptHash)) { 
      return new SubscriptionRecord( SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN ); 
    }
    
    return pair.subscriptionRecord;
  }
  
  public String toString() {
    return "TestStubSubscriptionService (Only knows three fixed testing users)";
  }

  @Override
  public ServerConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public void initialize(ObjectManager objMgr, ServerConfiguration config) {
    this.configuration = config;
  }

  @Override
  public void disconnect() {
    // No op
  }
}