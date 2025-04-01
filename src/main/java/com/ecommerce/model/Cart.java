package com.ecommerce.model;

import com.ecommerce.backend.CartRepository;
import com.ecommerce.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {
    private ObservableList<CartItem> items;
    private int customerId;
    private CartRepository cartRepository;

    public Cart(int customerId) {
        this.customerId = customerId;
        this.items = FXCollections.observableArrayList();
        this.cartRepository = new CartRepository();
        loadCartFromDatabase();
    }

    private void loadCartFromDatabase() {
        List<CartItem> loadedItems = cartRepository.loadCartItems(customerId);
        items.setAll(loadedItems);
    }

    private void saveCartToDatabase() {
        cartRepository.saveCartItems(customerId, new ArrayList<>(items));
    }

    public ObservableList<CartItem> getItems() {
        return items;
    }

    public void addProduct(Product product, int quantity) {
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getProduct().getId() == product.getId())
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().increaseQuantity(quantity);
        } else {
            items.add(new CartItem(product, quantity));
        }
        ProductService.getInstance().decreaseStock(product.getId(), quantity);
        saveCartToDatabase();
    }

    public boolean removeProduct(int productId) {
        Optional<CartItem> itemToRemove = items.stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst();

        if (itemToRemove.isPresent()) {
            ProductService.getInstance().increaseStock(productId, itemToRemove.get().getQuantity());
            items.remove(itemToRemove.get());
            saveCartToDatabase();
            return true;
        }
        return false;
    }

    public double getTotalPrice() {
        return items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public void clear() {
        items.clear();
        cartRepository.clearCart(customerId);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // Add listener support
    public void addListener(ListChangeListener<CartItem> listener) {
        items.addListener(listener);
    }
}