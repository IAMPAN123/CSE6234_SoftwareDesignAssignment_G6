package com.pos.service;

import org.junit.Test;
import static org.junit.Assert.*;

import com.pos.model.Product;

public class FacadePatternTest {

    @Test
    public void testFacadeSimplifyCartOperations() {
        com.pos.model.ShoppingCart cart = new com.pos.model.ShoppingCart();
        Product product = new Product(1, "P001", "Test Product", 10.0, 5);

        cart.addItem(product, 2);

        assertEquals(1, cart.getItems().size());
        assertEquals(20.0, cart.getTotal(), 0.01);
    }

    @Test
    public void testFacadeAddMultipleItems() {
        com.pos.model.ShoppingCart cart = new com.pos.model.ShoppingCart();
        Product product1 = new Product(1, "P001", "Product 1", 10.0, 10);
        Product product2 = new Product(2, "P002", "Product 2", 20.0, 10);

        cart.addItem(product1, 2);
        cart.addItem(product2, 1);

        assertEquals(2, cart.getItems().size());
        assertEquals(40.0, cart.getTotal(), 0.01);
    }

    @Test
    public void testFacadeRemoveItem() {
        com.pos.model.ShoppingCart cart = new com.pos.model.ShoppingCart();
        Product product = new Product(1, "P001", "Test Product", 10.0, 5);
        cart.addItem(product, 2);

        cart.removeItem(cart.getItems().get(0));

        assertEquals(0, cart.getItems().size());
    }

    @Test
    public void testFacadeClearCart() {
        com.pos.model.ShoppingCart cart = new com.pos.model.ShoppingCart();
        Product product = new Product(1, "P001", "Test Product", 10.0, 5);
        cart.addItem(product, 2);

        assertFalse(cart.isEmpty());

        cart.clear();

        assertTrue(cart.isEmpty());
    }

    @Test
    public void testFacadeValidateStock() {
        Product product = new Product(1, "P001", "Test Product", 10.0, 2);

        assertTrue(product.getStock() >= 2);
        assertFalse(product.getStock() >= 5);
    }

    @Test
    public void testFacadeCartCalculations() {
        com.pos.model.ShoppingCart cart = new com.pos.model.ShoppingCart();
        Product product = new Product(1, "P001", "Test Product", 25.50, 10);

        cart.addItem(product, 3);

        assertEquals(76.50, cart.getTotal(), 0.01);
        assertEquals(3, cart.getItemCount());
    }
}


