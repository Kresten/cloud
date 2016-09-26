package cloud.cave.service;

import cloud.cave.broker.CaveIPCException;
import cloud.cave.config.ObjectManager;
import cloud.cave.doubles.SaboteurSubscriptionAvailability;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.server.common.SubscriptionResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kresten on 26-09-2016.
 */
public class RealSubscriptionServiceWithAvailability implements SubscriptionService {

    //local "database" of loginNames and if they have been logged in earlier or not.
    private Map<String, SubscriptionRecord> subscriptionMap;
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
            if (!record.getErrorCode().equals(SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN)) {
                subscriptionMap.put(loginName, record);
            }
            return record;
        } catch (CaveIPCException e) {
            if (subscriptionMap.containsKey(loginName)) {
                System.out.println("*** Subscription server is currently down, but known player logged in through local database ***");
                return subscriptionMap.get(loginName);
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
}
