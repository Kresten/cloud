package cloud.cave.server;

import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Login;
import cloud.cave.domain.LoginResult;
import cloud.cave.doubles.AllTestDoubleFactory;
import cloud.cave.doubles.SaboteurSubscriptionAvailability;
import cloud.cave.service.RealSubscriptionServiceWithAvailability;
import cloud.cave.service.SubscriptionService;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Kresten on 26-09-2016.
 */
public class TestSubscriptionAvailability {

    private Cave cave;
    private String loginName;
    private Login login;
    private SaboteurSubscriptionAvailability sabSub;

    @Before
    public void setUp() throws Exception {
         sabSub = new SaboteurSubscriptionAvailability();
        CaveServerFactory factory = new AllTestDoubleFactory() {
            @Override
            public SubscriptionService createSubscriptionServiceConnector(ObjectManager objMgr) {
                SubscriptionService service = new RealSubscriptionServiceWithAvailability(sabSub);
                service.initialize(null, null); // no config object required for the stub
                return service;
            }
        };
        ObjectManager objMgr = new StandardObjectManager(factory);
        cave = new CaveServant(objMgr);
        loginName = "mikkel_aarskort";
    }

    @Test
    public void shouldFailToLoginFirstTimeThenLoginWhenServiceIsUpAndAgainEvenWhenServiceIsDown() {
        login = cave.login(loginName, "123");
        assertThat(login.getResultCode(), is(LoginResult.LOGIN_FAILED_SERVER_ERROR));
        sabSub.setState(SaboteurSubscriptionAvailability.State.AVAILABLE);
        login = cave.login(loginName, "123");
        String id = login.getPlayer().getID();
        cave.logout(id);
        assertThat(login.getResultCode(), is(LoginResult.LOGIN_SUCCESS));
        sabSub.setState(SaboteurSubscriptionAvailability.State.UNAVAILABLE);
        login = cave.login(loginName, "123");
        assertThat(login.getResultCode(), is(LoginResult.LOGIN_SUCCESS));
    }

}
