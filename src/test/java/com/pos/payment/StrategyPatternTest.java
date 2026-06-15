package com.pos.payment;

import org.junit.Test;
import static org.junit.Assert.*;

public class StrategyPatternTest {

    @Test
    public void testCashPaymentStrategy() {
        PaymentMethod cashPayment = new CashPayment();
        assertTrue(cashPayment.processPayment(100.0));
        assertTrue(cashPayment.processPayment(0.01));
        assertEquals("Cash", cashPayment.getPaymentType());
    }

    @Test
    public void testCardPaymentStrategy() {
        PaymentMethod cardPayment = new CardPayment();
        assertTrue(cardPayment.processPayment(100.0));
        assertEquals("Card", cardPayment.getPaymentType());
    }

    @Test
    public void testEWalletPaymentStrategy() {
        PaymentMethod eWalletPayment = new EWalletPayment();
        assertTrue(eWalletPayment.processPayment(100.0));
        assertEquals("E-Wallet", eWalletPayment.getPaymentType());
    }

    @Test
    public void testStrategyInterchangeability() {
        PaymentMethod[] strategies = {
            new CashPayment(),
            new CardPayment(),
            new EWalletPayment()
        };

        double amount = 150.50;
        for (PaymentMethod strategy : strategies) {
            assertTrue("Strategy " + strategy.getPaymentType() + " should process payment",
                      strategy.processPayment(amount));
        }
    }

    @Test
    public void testPaymentTypeRetrieval() {
        assertEquals("Cash", new CashPayment().getPaymentType());
        assertEquals("Card", new CardPayment().getPaymentType());
        assertEquals("E-Wallet", new EWalletPayment().getPaymentType());
    }
}
