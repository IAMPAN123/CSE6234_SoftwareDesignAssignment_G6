package com.pos.payment;

public class PaymentFactory {
    public static PaymentMethod createPayment(String type) {
        switch (type.toUpperCase()) {
            case "CASH":
                return new CashPayment();
            case "CARD":
                return new CardPayment();
            case "EWALLET":
                return new EWalletPayment();
            default:
                throw new IllegalArgumentException("Unknown payment type: " + type);
        }
    }
}
