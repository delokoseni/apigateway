package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrdersTest {
    private static final String FILE_NAME = "orders_log.txt";

    @BeforeEach
    void setUp() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write("Заказ №1 | Картошка фри - 1, Бургер - 2 | Статус: Выполняется");
            writer.newLine();
            writer.write("Заказ №2 | Картошка фри - 2, Бургер - 1 | Статус: Выполняется");
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(FILE_NAME));
    }

    @Test
    void testEditOrderStatus() throws IOException {
        Orders.editOrderStatus(2);
        List<String> lines = Files.readAllLines(Paths.get(FILE_NAME));
        assertEquals(2, lines.size());
        assertEquals("Заказ №1 | Картошка фри - 1, Бургер - 2 | Статус: Выполняется", lines.get(0));
        assertEquals("Заказ №2 | Картошка фри - 2, Бургер - 1 | Статус: Выполнен", lines.get(1));
    }
}
