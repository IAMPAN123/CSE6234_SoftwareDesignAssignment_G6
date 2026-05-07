package com.pos.payment;

public interface PaymentMethod {
    boolean processPayment(double amount);
    String getPaymentType();
}
