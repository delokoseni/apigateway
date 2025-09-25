package com.example.apigateway.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

@RestController
public class OrderController {

    private static final String ORDER_QUEUE_NAME = "order_queue";

    @PostMapping("/order")
    public ResponseEntity<String> order(@RequestBody String order) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {
                channel.queueDeclare(ORDER_QUEUE_NAME, true, false, false, null);

                channel.basicPublish("", ORDER_QUEUE_NAME,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        order.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + order + "'");
            }
            return ResponseEntity.ok("Order accepted");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error sending order: " + e.getMessage());
        }
    }

}
