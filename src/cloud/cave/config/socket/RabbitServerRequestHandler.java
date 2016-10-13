package cloud.cave.config.socket;

import cloud.cave.broker.Invoker;
import cloud.cave.broker.Marshaling;
import cloud.cave.broker.ServerRequestHandler;
import cloud.cave.broker.StatusCode;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.RabbitMQConfig;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import com.rabbitmq.client.*;
import org.apache.http.entity.ContentType;
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
    private Invoker invoker;

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        invoker = objectManager.getInvoker();
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
        String request = null;
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            System.out.println("*** Channel connection established ***");
            channel.queueDeclare(RabbitMQConfig.RPC_QUEUE_NAME, false, false, false, null);
            channel.basicQos(1);

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(RabbitMQConfig.RPC_QUEUE_NAME, false, consumer);

            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                BasicProperties properties = delivery.getProperties();
                AMQP.BasicProperties replyProperties = new AMQP.BasicProperties.
                        Builder()
                        .contentType(String.valueOf(ContentType.APPLICATION_JSON))
                        .correlationId(properties.getCorrelationId())
                        .build();

                request = new String(delivery.getBody());
                System.out.println("--> [REQUEST] " + request);
                requestJson = (JSONObject) parser.parse(request);
                reply = invoker.handleRequest(requestJson);
                System.out.println("--< [REPLY] " + reply);

                channel.basicPublish("", properties.getReplyTo(), replyProperties, reply.toJSONString().getBytes());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            reply = Marshaling.createInvalidReplyWithExplanation(
                    StatusCode.SERVER_FAILURE, "JSON Parse error on " + request);
            System.out.println("--< [REPLY FAIL]: " + reply);
        }
    }
}
