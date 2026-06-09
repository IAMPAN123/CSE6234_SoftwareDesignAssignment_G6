package com.pos.payment;

import org.junit.Test;
import static org.junit.Assert.*;

public class PaymentFactoryTest {

    @Test
    public void testCreateCashPayment() {
        PaymentMethod payment = PaymentFactory.createPayment("CASH");
        assertNotNull(payment);
        assertTrue(payment instanceof CashPayment);
        assertEquals("Cash", payment.getPaymentType());
    }

    @Test
    public void testCreateCardPayment() {
        PaymentMethod payment = PaymentFactory.createPayment("CARD");
        assertNotNull(payment);
        assertTrue(payment instanceof CardPayment);
        assertEquals("Card", payment.getPaymentType());
    }

    @Test
    public void testCreateEWalletPayment() {
        PaymentMethod payment = PaymentFactory.createPayment("EWALLET");
        assertNotNull(payment);
        assertTrue(payment instanceof EWalletPayment);
        assertEquals("E-Wallet", payment.getPaymentType());
    }

    @Test
    public void testCreatePaymentCaseInsensitive() {
        PaymentMethod payment1 = PaymentFactory.createPayment("cash");
        PaymentMethod payment2 = PaymentFactory.createPayment("CASH");
        PaymentMethod payment3 = PaymentFactory.createPayment("Cash");

        assertTrue(payment1 instanceof CashPayment);
        assertTrue(payment2 instanceof CashPayment);
        assertTrue(payment3 instanceof CashPayment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePaymentInvalidType() {
        PaymentFactory.createPayment("INVALID");
    }

    @Test(expected = Exception.class)
    public void testCreatePaymentNullType() {
        PaymentFactory.createPayment(null);
    }
}

