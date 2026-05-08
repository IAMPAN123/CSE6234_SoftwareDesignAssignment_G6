package com.pos.service;

import java.sql.SQLException;
import java.util.List;

import com.pos.db.ActionLogDAO;
import com.pos.db.ProductDAO;
import com.pos.db.TransactionDAO;
import com.pos.model.CartItem;
import com.pos.model.Product;
import com.pos.model.Receipt;
import com.pos.model.ShoppingCart;
import com.pos.model.Transaction;
import com.pos.payment.PaymentFactory;
import com.pos.payment.PaymentMethod;

public class CheckoutFacade {
    private ShoppingCart cart;
    private ProductDAO productDAO;
    private TransactionDAO transactionDAO;
    private ActionLogDAO actionLogDAO;

    public CheckoutFacade(ProductDAO productDAO, TransactionDAO transactionDAO) throws SQLException {
        this.cart = new ShoppingCart();
        this.productDAO = productDAO;
        this.transactionDAO = transactionDAO;
        this.actionLogDAO = new ActionLogDAO();
    }

    public void addToCart(Product product, int quantity) throws SQLException {
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }
        cart.addItem(product, quantity);
    }

    public void removeFromCart(CartItem item) {
        cart.removeItem(item);
    }

    public void clearCart() {
        cart.clear();
    }

    public ShoppingCart getCart() {
        return cart;
    }

    public Receipt checkout(String paymentType) throws SQLException {
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        PaymentMethod payment = PaymentFactory.createPayment(paymentType);
        double total = cart.getTotal();

        if (!payment.processPayment(total)) {
            throw new RuntimeException("Payment failed");
        }

        List<CartItem> items = cart.getItems();
        Receipt receipt = new Receipt(items, total, payment.getPaymentType());

        StringBuilder productsJson = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            productsJson.append("{")
                .append("\"name\":\"").append(item.getProduct().getName()).append("\",")
                .append("\"quantity\":").append(item.getQuantity()).append(",")
                .append("\"price\":").append(item.getProduct().getPrice())
                .append("}");
            if (i < items.size() - 1) productsJson.append(",");
        }
        productsJson.append("]");

        // Log the checkout action
        actionLogDAO.logAction("CHECKOUT",
        "Payment: " + paymentType + ", Total: RM" + String.format("%.2f", total),
        productsJson.toString());

        updateInventory(items);
        saveTransaction(receipt);

        cart.clear();

        return receipt;
    }

    private void updateInventory(List<CartItem> items) throws SQLException {
        for (CartItem item : items) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productDAO.updateProduct(product);
        }
    }

    private void saveTransaction(Receipt receipt) throws SQLException {
        StringBuilder itemsJson = new StringBuilder("[");
        List<CartItem> items = receipt.getItems();
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            itemsJson.append("{")
                .append("\"name\":\"").append(item.getProduct().getName()).append("\",")
                .append("\"quantity\":").append(item.getQuantity()).append(",")
                .append("\"price\":").append(item.getProduct().getPrice())
                .append("}");
            if (i < items.size() - 1) {
                itemsJson.append(",");
            }
        }
        itemsJson.append("]");

        Transaction transaction = new Transaction(
            receipt.getTimestamp(),
            itemsJson.toString(),
            receipt.getSubtotal(),
            receipt.getDiscount(),
            receipt.getTotal(),
            receipt.getPaymentMethod()
        );

        transactionDAO.addTransaction(transaction);
    }

    public CartSummary getCartSummary() {
        return new CartSummary(
            cart.getItemCount(),
            cart.getTotal(),
            cart.getItems().size()
        );
    }

    public static class CartSummary {
        private int itemCount;
        private double total;
        private int uniqueProducts;

        public CartSummary(int itemCount, double total, int uniqueProducts) {
            this.itemCount = itemCount;
            this.total = total;
            this.uniqueProducts = uniqueProducts;
        }

        public int getItemCount() {
            return itemCount;
        }

        public double getTotal() {
            return total;
        }

        public int getUniqueProducts() {
            return uniqueProducts;
        }
    }
}
