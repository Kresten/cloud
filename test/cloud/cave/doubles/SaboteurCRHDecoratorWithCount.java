package cloud.cave.doubles;

import cloud.cave.broker.CaveIPCException;
import cloud.cave.broker.ClientRequestHandler;
import cloud.cave.server.common.ServerConfiguration;
import org.json.simple.JSONObject;

/**
 * Created by krest on 24-09-2016.
 */
public class SaboteurCRHDecoratorWithCount implements ClientRequestHandler {

    private ClientRequestHandler decoratee;
    private String exceptionMsg;
    private int count;

    public SaboteurCRHDecoratorWithCount(ClientRequestHandler decoratee) {
        this.decoratee = decoratee;
        exceptionMsg = null;
    }

    public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson)
            throws CaveIPCException {
        if (count == 3) {
            throw new CaveIPCException(exceptionMsg, null);
        }
        count++;
        return decoratee.sendRequestAndBlockUntilReply(requestJson);
    }

    @Override
    public void initialize(ServerConfiguration config) {
        // Not relevant, as this request handler is only used in
        // testing and under programmatic control
    }
}