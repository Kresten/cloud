package cloud.cave.doubles;

import cloud.cave.broker.CaveIPCException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.service.SubscriptionService;

/**
 * Created by Kresten on 26-09-2016.
 */
public class SaboteurSubscriptionAvailability implements SubscriptionService {

    private ServerConfiguration configuration;
    private int count;

    @Override
    public SubscriptionRecord lookup(String loginName, String password) {
        count++;
        System.out.println("COUNT = " + count);
        if (count == 2) {
            return new SubscriptionRecord("user-001", "Mikkel", "grp01", Region.AARHUS);
        } else {
            throw new CaveIPCException("*** Subscription server down, please try again later ***", null);
        }
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
