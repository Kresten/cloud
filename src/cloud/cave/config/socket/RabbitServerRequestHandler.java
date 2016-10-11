package cloud.cave.config.socket;

import cloud.cave.broker.Marshaling;
import cloud.cave.broker.ServerRequestHandler;
import cloud.cave.broker.StatusCode;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.RabbitMQConfig;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import com.rabbitmq.client.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Kresten on 11-10-2016.
 */
public class RabbitServerRequestHandler implements ServerRequestHandler {

    private ServerConfiguration serverConfiguration;
    private ConnectionFactory factory;
    private JSONParser parser;
    private ObjectManager objectManager;
    private int portNumber;
    private String hostName;

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.objectManager = objectManager;
        this.serverConfiguration = config;
        ServerData serverData = serverConfiguration.get(0);
        hostName = serverData.getHostName();
        portNumber = serverData.getPortNumber();
        parser = new JSONParser();
    }

    @Override
    public void run() {
        factory = new ConnectionFactory();
        factory.setHost(hostName);
        factory.setPort(portNumber);
        JSONObject requestJson;
        JSONObject reply;
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(RabbitMQConfig.RPC_QUEUE_NAME, false, false, false, null);
            channel.basicQos(1);
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(RabbitMQConfig.RPC_QUEUE_NAME, false, consumer);

            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                BasicProperties properties = delivery.getProperties();
                AMQP.BasicProperties replyProperties = new AMQP.BasicProperties.
                        Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();

                String body = new String(delivery.getBody());
                requestJson = (JSONObject) parser.parse(body);
                reply = objectManager.getInvoker().handleRequest(requestJson);
                channel.basicPublish("", properties.getReplyTo(), replyProperties, reply.toJSONString().getBytes());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            }
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            reply = Marshaling.createInvalidReplyWithExplanation(
                    StatusCode.SERVER_FAILURE, "JSON Parse error");
        }
    }
}
