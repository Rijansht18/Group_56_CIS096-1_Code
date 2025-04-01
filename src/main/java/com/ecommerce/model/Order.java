package com.ecommerce.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Order {
    private int id;
    private int customerId;
    private List<CartItem> items;
    private double totalAmount;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;  // New field
    private String shippingAddress;
    private String status; // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED, RETURN_REQUESTED, RETURNED
    private boolean returnRequested;     // New field
    private String returnReason;         // New field
    private boolean returnApproved;      // New field
    public static final String PENDING = "PENDING";
    public static final String PROCESSING = "PROCESSING";
    public static final String SHIPPED = "SHIPPED";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";
    public static final String RETURN_REQUESTED = "RETURN_REQUESTED";
    public static final String RETURNED = "RETURNED";

    // Constructor for creating new orders
    public Order(int id, int customerId, List<CartItem> items, String shippingAddress) {
        this(id, customerId, items, shippingAddress, calculateTotalAmount(items),
                LocalDateTime.now(), "PENDING");
    }

    // Full constructor for loading existing orders
    public Order(int id, int customerId, List<CartItem> items, String shippingAddress,
                 double totalAmount, LocalDateTime orderDate, String status) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.status = status;
        this.returnRequested = false;
        this.returnApproved = false;
    }

    // Additional constructor for database loading with all fields
    public Order(int id, int customerId, List<CartItem> items, String shippingAddress,
                 double totalAmount, LocalDateTime orderDate, LocalDateTime deliveryDate,
                 String status, boolean returnRequested, String returnReason,
                 boolean returnApproved) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.status = status;
        this.returnRequested = returnRequested;
        this.returnReason = returnReason;
        this.returnApproved = returnApproved;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) {
        this.items = items;
        this.totalAmount = calculateTotalAmount(items);
    }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isReturnRequested() { return returnRequested; }
    public void setReturnRequested(boolean returnRequested) { this.returnRequested = returnRequested; }

    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }

    public boolean isReturnApproved() { return returnApproved; }
    public void setReturnApproved(boolean returnApproved) { this.returnApproved = returnApproved; }

    // Business logic methods
    public boolean canBeCancelled() {
        return status.equals("PENDING") || status.equals("PROCESSING");
    }

    public boolean canBeReturned() {
        if (!status.equalsIgnoreCase("DELIVERED") || deliveryDate == null) {
            return false;
        }
        // Check if within 7 days of delivery
        return ChronoUnit.DAYS.between(deliveryDate, LocalDateTime.now()) <= 7;
    }

    // Add this method to check if return can be approved
    public boolean canApproveReturn() {
        return isReturnRequested() && !isReturnApproved() &&
                status.equalsIgnoreCase("RETURN_REQUESTED");
    }

    public void requestReturn(String reason) {
        if (canBeReturned()) {
            this.returnRequested = true;
            this.returnReason = reason;
            this.status = "RETURN_REQUESTED";
        }
    }

    public void approveReturn() {
        if (returnRequested && !returnApproved) {
            this.returnApproved = true;
            this.status = "RETURNED";
        }
    }

    // Helper methods
    private static double calculateTotalAmount(List<CartItem> items) {
        return items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Override
    public String toString() {
        String base = "Order #" + id + " - " + status + " - $" + totalAmount;
        if (returnRequested) {
            base += " - Return Requested";
            if (returnApproved) {
                base += " (Approved)";
            }
        }
        return base;
    }
}