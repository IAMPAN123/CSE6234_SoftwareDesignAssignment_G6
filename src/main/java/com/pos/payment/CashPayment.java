package com.pos.payment;

public class CashPayment implements PaymentMethod {
    @Override
    public boolean processPayment(double amount) {
        return true;
    }

    @Override
    public String getPaymentType() {
        return "Cash";
    }
}
