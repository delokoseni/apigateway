package org.example;

import com.rabbitmq.client.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Orders {

    private static final String ORDER_QUEUE_NAME = "order_queue";
    private static final String KITCHEN_QUEUE_NAME = "kitchen_queue";
    private static int ORDER_NUMBER = 1;
    private static final String FILE_NAME = "orders_log.txt";

    public static void main(String[] args) throws IOException, TimeoutException {
        Files.deleteIfExists(Paths.get(FILE_NAME));
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(ORDER_QUEUE_NAME, true, false, false, null);
        channel.queueDeclare(KITCHEN_QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            int orderNumber = saveToDB(message);
            String kitchenMessage = orderNumber + "|" + message;
            channel.basicPublish("", KITCHEN_QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    kitchenMessage.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + kitchenMessage + "'");
        };
        channel.basicConsume(ORDER_QUEUE_NAME, true, deliverCallback, consumerTag -> { });

        DeliverCallback kitchenCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received kitchen response'" + message + "'");
            String[] parts = message.split("\\|");
            if (parts.length >= 2) {
                int orderNumber = Integer.parseInt(parts[0]);
                editOrderStatus(orderNumber);
            }
        };
        channel.basicConsume(KITCHEN_QUEUE_NAME, true, kitchenCallback, consumerTag -> { });
    }

    private static int saveToDB(String order) {
        int orderNumber = ORDER_NUMBER++;
        try (FileWriter writer = new FileWriter(FILE_NAME, true)) {
            String line = String.format("Заказ №%d | %s | Статус: Выполняется%n", orderNumber, order);
            writer.write(line);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return orderNumber;
    }

    public static void editOrderStatus(int orderNumber) {
        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Заказ №" + orderNumber + " ")) {
                        int idx = line.indexOf("Статус:");
                        if (idx != -1) {
                            line = line.substring(0, idx) + "Статус: " + "Выполнен";
                        }
                    }
                    lines.add(line);
                }
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
                for (String l : lines) {
                    writer.write(l);
                    writer.newLine();
                }
            }
            System.out.println("Статус заказа №" + orderNumber + " изменён на '" + "Выполнен" + "'");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}