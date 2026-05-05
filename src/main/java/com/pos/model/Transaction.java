package com.pos.model;

import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private LocalDateTime timestamp;
    private String itemsJson;
    private double subtotal;
    private double discount;
    private double total;
    private String paymentMethod;

    public Transaction(LocalDateTime timestamp, String itemsJson, double subtotal, double discount, double total, String paymentMethod) {
        this.timestamp = timestamp;
        this.itemsJson = itemsJson;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.paymentMethod = paymentMethod;
    }

    public Transaction(int id, LocalDateTime timestamp, String itemsJson, double subtotal, double discount, double total, String paymentMethod) {
        this.id = id;
        this.timestamp = timestamp;
        this.itemsJson = itemsJson;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.paymentMethod = paymentMethod;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getItemsJson() {
        return itemsJson;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public double getTotal() {
        return total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
