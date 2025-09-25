package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Orders {

    private static final String ORDER_QUEUE_NAME = "order_queue";
    private static int ORDER_NUMBER = 1;
    private static final String FILE_NAME = "orders_log.txt";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(ORDER_QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            saveToDB(message);
        };
        channel.basicConsume(ORDER_QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }

    private static void saveToDB(String order) {
        try (FileWriter writer = new FileWriter(FILE_NAME, true)) {
            String line = String.format("Заказ №%d | %s | Статус: Выполняется%n", ORDER_NUMBER++, order);
            writer.write(line);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}