package com.ecommerce.model;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    private String address;
    private String phoneNumber;
    private List<Order> orderHistory;
    private Cart cart;

    public Customer(int id, String username, String password, String email, String fullName,
                    String address, String phoneNumber) {
        super(id, username, password, email, fullName);
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.orderHistory = new ArrayList<>();
        this.cart = new Cart(id);
    }

    // Getters and setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Order> getOrderHistory() {
        return orderHistory;
    }

    public void addOrder(Order order) {
        this.orderHistory.add(order);
    }

    public Cart getCart() {
        return cart;
    }

    @Override
    public String getUserType() {
        return "CUSTOMER";
    }
}