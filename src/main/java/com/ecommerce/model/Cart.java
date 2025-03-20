package com.ecommerce.model;

import com.ecommerce.backend.CartRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {
    private List<CartItem> items;
    private int customerId;
    private CartRepository cartRepository;

    public Cart(int customerId) {
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.cartRepository = new CartRepository();
        loadCartFromDatabase(); // Load cart items from the database
    }

    // Load cart items from the database
    private void loadCartFromDatabase() {
        items = cartRepository.loadCartItems(customerId);
    }

    // Save cart items to the database
    private void saveCartToDatabase() {
        cartRepository.saveCartItems(customerId, items);
    }


    // Getters and setters
    public List<CartItem> getItems() {
        return items;
    }

    // Method to add a product to the cart
    public void addProduct(Product product, int quantity) {
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getProduct().getId() == product.getId())
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().increaseQuantity(quantity);
        } else {
            items.add(new CartItem(product, quantity));
        }
        saveCartToDatabase(); // Save cart to database
    }

    // Method to remove a product from the cart
    public boolean removeProduct(int productId) {
        boolean removed = items.removeIf(item -> item.getProduct().getId() == productId);
        if (removed) {
            saveCartToDatabase(); // Save cart to database
        }
        return removed;
    }

    // Method to calculate the total price of all items in the cart
    public double getTotalPrice() {
        return items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    // Method to get the total number of items in the cart
    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // Method to clear the cart
    public void clear() {
        items.clear();
        cartRepository.clearCart(customerId); // Clear cart items from the database
    }

    // Method to check if the cart is empty
    public boolean isEmpty() {
        return items.isEmpty();
    }
}