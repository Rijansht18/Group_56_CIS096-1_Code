package com.ecommerce.model;

public class Admin extends User {
    private String role;
    private String department;

    public Admin(int id, String username, String password, String email, String fullName,
                 String role, String department) {
        super(id, username, password, email, fullName);
        this.role = role;
        this.department = department;
    }

    // Getters and setters
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String getUserType() {
        return "ADMIN";
    }
}