package com.pos.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.pos.model.ShoppingCart.CartListener;

public class ObserverPatternTest {

    private ShoppingCart cart;
    private TestCartListener listener1;
    private TestCartListener listener2;

    private static class TestCartListener implements CartListener {
        public int updateCount = 0;

        @Override
        public void onCartUpdated() {
            updateCount++;
        }
    }

    @Before
    public void setUp() {
        cart = new ShoppingCart();
        listener1 = new TestCartListener();
        listener2 = new TestCartListener();
    }

    @Test
    public void testListenerNotifiedOnAddItem() {
        cart.addListener(listener1);

        Product product = new Product(1, "P001", "Test Product", 10.0, 5);
        cart.addItem(product, 1);

        assertEquals(1, listener1.updateCount);
    }

    @Test
    public void testMultipleListenersNotified() {
        cart.addListener(listener1);
        cart.addListener(listener2);

        Product product = new Product(1, "P001", "Test Product", 10.0, 5);
        cart.addItem(product, 1);

        assertEquals(1, listener1.updateCount);
        assertEquals(1, listener2.updateCount);
    }

    @Test
    public void testListenerNotifiedOnRemoveItem() {
        Product product = new Product(1, "P001", "Test Product", 10.0, 5);
        CartItem item = new CartItem(product, 1);

        cart.addListener(listener1);
        cart.removeItem(item);

        assertEquals(1, listener1.updateCount);
    }

    @Test
    public void testListenerNotifiedOnClear() {
        Product product = new Product(1, "P001", "Test Product", 10.0, 5);
        cart.addItem(product, 1);

        cart.addListener(listener1);
        cart.clear();

        assertEquals(1, listener1.updateCount);
    }

    @Test
    public void testMultipleAddItemsNotifyListener() {
        cart.addListener(listener1);

        Product product1 = new Product(1, "P001", "Product 1", 10.0, 5);
        Product product2 = new Product(2, "P002", "Product 2", 20.0, 5);

        cart.addItem(product1, 1);
        cart.addItem(product2, 1);

        assertEquals(2, listener1.updateCount);
    }

    @Test
    public void testListenerAddedAfterActionsNotNotified() {
        Product product = new Product(1, "P001", "Test Product", 10.0, 5);
        cart.addItem(product, 1);

        cart.addListener(listener1);
        cart.addItem(product, 1);

        assertEquals(1, listener1.updateCount);
    }

    @Test
    public void testObserverPatternDecoupling() {
        TestCartListener listener3 = new TestCartListener();
        TestCartListener listener4 = new TestCartListener();

        cart.addListener(listener3);
        cart.addListener(listener4);

        Product product = new Product(1, "P001", "Test", 10.0, 5);
        cart.addItem(product, 1);
        cart.addItem(product, 1);
        cart.clear();

        assertEquals(3, listener3.updateCount);
        assertEquals(3, listener4.updateCount);
    }
}

