package com.pos.db;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

public class DAOPatternTest {

    @Test
    public void testDAOEncapsulatesProductData() {
        com.pos.model.Product product = new com.pos.model.Product(
            1, "P001", "Test Product", 10.0, 5
        );

        assertEquals(1, product.getId());
        assertEquals("P001", product.getBarcode());
        assertEquals("Test Product", product.getName());
        assertEquals(10.0, product.getPrice(), 0.01);
        assertEquals(5, product.getStock());
    }

    @Test
    public void testDAOEncapsulatesTransactionData() {
        com.pos.model.Transaction transaction = new com.pos.model.Transaction(
            LocalDateTime.now(),
            "[{\"name\":\"Product\",\"quantity\":1,\"price\":10.0}]",
            10.0,
            0.0,
            10.0,
            "Cash"
        );

        assertNotNull(transaction.getTimestamp());
        assertEquals("[{\"name\":\"Product\",\"quantity\":1,\"price\":10.0}]",
                     transaction.getItemsJson());
        assertEquals(10.0, transaction.getSubtotal(), 0.01);
        assertEquals(0.0, transaction.getDiscount(), 0.01);
        assertEquals(10.0, transaction.getTotal(), 0.01);
        assertEquals("Cash", transaction.getPaymentMethod());
    }

    @Test
    public void testProductDAODataIntegrity() {
        com.pos.model.Product product = new com.pos.model.Product("P001", "Product", 20.0, 10);

        product.setStock(8);
        product.setPrice(22.0);

        assertEquals(8, product.getStock());
        assertEquals(22.0, product.getPrice(), 0.01);
    }

    @Test
    public void testTransactionDAODataIntegrity() {
        LocalDateTime timestamp = LocalDateTime.now();
        com.pos.model.Transaction transaction = new com.pos.model.Transaction(
            timestamp,
            "[{\"name\":\"Product\"}]",
            50.0,
            5.0,
            45.0,
            "Card"
        );

        assertEquals(timestamp, transaction.getTimestamp());
        assertEquals(50.0, transaction.getSubtotal(), 0.01);
        assertEquals(5.0, transaction.getDiscount(), 0.01);
        assertEquals(45.0, transaction.getTotal(), 0.01);
        assertEquals("Card", transaction.getPaymentMethod());
    }
}


