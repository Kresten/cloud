package cloud.cave.service;

import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.server.common.SubscriptionResult;

/**
 * Created by Kresten on 15-09-2016.
 */
public class RealSubscriptionService implements SubscriptionService {

    @Override
    public SubscriptionRecord lookup(String loginName, String password) {

        return new SubscriptionRecord(SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN);
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public ServerConfiguration getConfiguration() {
        return null;
    }
}
