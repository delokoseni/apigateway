package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ThreadLocalRandom;

public class Kitchen {

    private static final String KITCHEN_QUEUE_NAME = "kitchen_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(KITCHEN_QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            int seconds = ThreadLocalRandom.current().nextInt(5, 11);
            try {
                Thread.sleep(seconds * 1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String[] parts = message.split("\\|", 2);
            int orderNumber = Integer.parseInt(parts[0]);
            String response = orderNumber + "|Выполнен";
            channel.basicPublish("",
                    KITCHEN_QUEUE_NAME,
                    null,
                    response.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent ready order: '" + response + "'");
        };
        channel.basicConsume(KITCHEN_QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}