package com.ecommerce.service;

import com.ecommerce.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Private constructor to enforce singleton pattern
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Method to set the current user (login)
    public void login(User user) {
        this.currentUser = user;
    }

    // Method to clear the current user (logout)
    public void logout() {
        this.currentUser = null;
    }

    // Method to get the current user
    public User getCurrentUser() {
        return currentUser;
    }

    // Method to check if a user is logged in
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Method to check if the current user is an admin
    public boolean isAdmin() {
        return isLoggedIn() && "ADMIN".equals(currentUser.getUserType());
    }

    // Method to check if the current user is a customer
    public boolean isCustomer() {
        return isLoggedIn() && "CUSTOMER".equals(currentUser.getUserType());
    }
}