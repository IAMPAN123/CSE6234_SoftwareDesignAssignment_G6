package com.pos.payment;

public class EWalletPayment implements PaymentMethod {
    @Override
    public boolean processPayment(double amount) {
        return true;
    }

    @Override
    public String getPaymentType() {
        return "E-Wallet";
    }
}
