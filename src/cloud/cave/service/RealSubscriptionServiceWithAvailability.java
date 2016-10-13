package cloud.cave.service;

import cloud.cave.broker.CaveIPCException;
import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.server.common.SubscriptionResult;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kresten on 26-09-2016.
 */
public class RealSubscriptionServiceWithAvailability implements SubscriptionService {

    //local "database" of loginNames and if they have been logged in earlier or not.
    private Map<String, SubscriptionPair> subscriptionMap;
    private SubscriptionService decoratee;

    public RealSubscriptionServiceWithAvailability() {
        subscriptionMap = new HashMap<>();
        this.decoratee = new RealSubscriptionService();
    }

    public RealSubscriptionServiceWithAvailability(SubscriptionService decoratee) {
        subscriptionMap = new HashMap<>();
        this.decoratee = decoratee;
    }

    @Override
    public SubscriptionRecord lookup(String loginName, String password) {
        try {
            SubscriptionRecord record = decoratee.lookup(loginName, password);
            SubscriptionPair subscriptionPair = new SubscriptionPair(password, record);
            if (!record.getErrorCode().equals(SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN)) {
                subscriptionMap.put(loginName, subscriptionPair);
            }
            return record;
        } catch (CaveIPCException e) {
            if (subscriptionMap.containsKey(loginName)) {
                SubscriptionPair subscriptionPair = subscriptionMap.get(loginName);
                if (BCrypt.checkpw(password, subscriptionPair.bCryptHash)){
                    return subscriptionPair.subscriptionRecord;
                }
            }
            throw e;
        }
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        decoratee.initialize(objectManager, config);
    }

    @Override
    public void disconnect() {
        decoratee.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return decoratee.getConfiguration();
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
}