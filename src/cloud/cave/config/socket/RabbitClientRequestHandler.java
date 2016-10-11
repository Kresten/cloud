package cloud.cave.config.socket;

import cloud.cave.broker.CaveIPCException;
import cloud.cave.broker.ClientRequestHandler;
import cloud.cave.server.common.ServerConfiguration;
import com.rabbitmq.client.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static java.util.UUID.randomUUID;

/**
 * Created by Kresten on 11-10-2016.
 */
public class RabbitClientRequestHandler implements ClientRequestHandler {

    private Channel channel;
    private Connection connection;
    private String replyQueueName;
    private QueueingConsumer consumer;
    private JSONParser parser;
    private String hostName;
    private int portNumber;

    @Override
    public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson) throws CaveIPCException {
        JSONObject response = null;
        String corrId = randomUUID().toString();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);
        factory.setPort(portNumber);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            replyQueueName = channel.queueDeclare().getQueue();
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(replyQueueName, true, consumer);
            AMQP.BasicProperties properties = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();


            channel.basicPublish("", replyQueueName, properties, requestJson.toJSONString().getBytes());
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    String body = new String(delivery.getBody());
                    response = (JSONObject) parser.parse(body);
                    break;
                }
            }

        } catch (IOException | InterruptedException | ParseException | TimeoutException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public void initialize(ServerConfiguration config) {
        hostName = config.get(0).getHostName();
        portNumber = config.get(0).getPortNumber();
        parser = new JSONParser();
    }
}
