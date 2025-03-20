package com.ecommerce.backend;

import com.ecommerce.model.Admin;
import com.ecommerce.service.UserService;

public class InsertAdmin {

    public static void main(String[] args) {
        // Create an instance of UserService
        UserService userService = UserService.getInstance();

        // Admin details
        String username = "admin";
        String password = "admin123";
        String email = "admin@example.com";
        String fullName = "Admin User";
        String role = "Super Admin";
        String department = "Management";

        // Register the admin
        Admin admin = userService.registerAdmin(username, password, email, fullName, role, department);

        if (admin != null) {
            System.out.println("Admin successfully inserted into the database!");
            System.out.println("Admin ID: " + admin.getId());
            System.out.println("Username: " + admin.getUsername());
            System.out.println("Role: " + admin.getRole());
            System.out.println("Department: " + admin.getDepartment());
        } else {
            System.out.println("Failed to insert admin. The username might already exist.");
        }
    }
}