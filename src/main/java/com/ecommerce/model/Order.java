package com.ecommerce.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int id;
    private int customerId;
    private List<CartItem> items;
    private double totalAmount;
    private LocalDateTime orderDate;
    private String shippingAddress;
    private String status; // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    public Order(int id, int customerId, List<CartItem> items, String shippingAddress) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = calculateTotalAmount();
        this.orderDate = LocalDateTime.now();
        this.shippingAddress = shippingAddress;
        this.status = "PENDING"; // Default status
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Method to calculate the total amount of the order
    private double calculateTotalAmount() {
        return items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    // Method to get the total number of items in the order
    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Override
    public String toString() {
        return "Order #" + id + " - " + status + " - $" + totalAmount;
    }
}