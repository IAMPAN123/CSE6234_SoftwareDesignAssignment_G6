package com.pos.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Receipt {
    private LocalDateTime timestamp;
    private List<CartItem> items;
    private double subtotal;
    private double discount;
    private double total;
    private String paymentMethod;

    public Receipt(List<CartItem> items, double total, String paymentMethod) {
        this.timestamp = LocalDateTime.now();
        this.items = items;
        this.subtotal = total;
        this.discount = 0;
        this.total = total;
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<CartItem> getItems() {
        return items;
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

    public String getFormattedText() {
        StringBuilder sb = new StringBuilder();
        sb.append("================================\n");
        sb.append("         GROUP 6 SHOP RECEIPT\n");
        sb.append("================================\n");
        sb.append("Date: ").append(timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("================================\n\n");

        sb.append("Items:\n");
        for (CartItem item : items) {
            sb.append(String.format("%-25s %5d x RM%.2f = RM%.2f\n",
                item.getProduct().getName().substring(0, Math.min(25, item.getProduct().getName().length())),
                item.getQuantity(),
                item.getProduct().getPrice(),
                item.getSubtotal()));
        }

        sb.append("\n================================\n");
        sb.append(String.format("Subtotal:        RM %.2f\n", subtotal));
        sb.append(String.format("Discount:        -RM %.2f\n", discount));
        sb.append(String.format("Total:           RM %.2f\n", total));
        sb.append("================================\n");
        sb.append("Payment Method: ").append(paymentMethod).append("\n");
        sb.append("================================\n");
        sb.append("Thank you for your purchase!\n");
        sb.append("================================\n");

        return sb.toString();
    }

    @Override
    public String toString() {
        return getFormattedText();
    }
}
