package com.ecommerce.model;

public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Getters and setters
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Method to calculate subtotal for this item
    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    // Method to increase quantity
    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    // Method to decrease quantity
    public boolean decreaseQuantity(int amount) {
        if (this.quantity - amount >= 0) {
            this.quantity -= amount;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return product.getName() + " x " + quantity + " = $" + getSubtotal();
    }
}