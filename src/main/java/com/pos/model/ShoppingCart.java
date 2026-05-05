package com.pos.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShoppingCart {
    private List<CartItem> items;
    private List<CartListener> listeners;

    public ShoppingCart() {
        this.items = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    public void addItem(Product product, int quantity) {
        Optional<CartItem> existing = items.stream()
            .filter(item -> item.getProduct().getId() == product.getId())
            .findFirst();

        if (existing.isPresent()) {
            existing.get().incrementQuantity();
        } else {
            items.add(new CartItem(product, quantity));
        }
        notifyListeners();
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        notifyListeners();
    }

    public void clear() {
        items.clear();
        notifyListeners();
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public double getTotal() {
        return items.stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public int getItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void addListener(CartListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (CartListener listener : listeners) {
            listener.onCartUpdated();
        }
    }

    public interface CartListener {
        void onCartUpdated();
    }
}
