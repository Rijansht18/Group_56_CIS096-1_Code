package com.ecommerce.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3307/";
    private static final String DB_NAME = "ecommerceFunctional";
    private static final String DB_USER = "root"; // Replace with your MySQL username
    private static final String DB_PASSWORD = "Rijan@#$123"; // Replace with your MySQL password

    private Connection connection;
    private static DatabaseManager instance;

    // Private constructor to enforce singleton pattern
    private DatabaseManager() {
        initializeDatabase();
    }

    // Method to get the singleton instance
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            // Connect to MySQL server (without specifying a database)
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Create the database if it doesn't exist
            createDatabase();

            // Use the database
            connection.setCatalog(DB_NAME);

            // Create tables if they don't exist
            createTables();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String sql = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            stmt.executeUpdate(sql);
            System.out.println("Database created or already exists.");
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "user_type ENUM('ADMIN', 'CUSTOMER') NOT NULL, " +
                    "role VARCHAR(50), " +
                    "department VARCHAR(50), " +
                    "address VARCHAR(255), " +
                    "phone_number VARCHAR(20)" +
                    ")";
            stmt.executeUpdate(createUsersTable);
            System.out.println("Users table created or already exists.");

            // Create products table
            String createProductsTable = "CREATE TABLE IF NOT EXISTS products (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "description TEXT, " +
                    "price DECIMAL(10, 2) NOT NULL, " +
                    "stock_quantity INT NOT NULL, " +
                    "category VARCHAR(50), " +
                    "image_url VARCHAR(255)" +
                    ")";
            stmt.executeUpdate(createProductsTable);
            System.out.println("Products table created or already exists.");

            // Create orders table
            String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "customer_id INT NOT NULL, " +
                    "order_date DATETIME NOT NULL, " +
                    "total_amount DECIMAL(10, 2) NOT NULL, " +
                    "shipping_address VARCHAR(255) NOT NULL, " +
                    "status ENUM('PENDING','PROCESSING','SHIPPED','DELIVERED','CANCELLED','RETURN_REQUESTED','RETURNED') NOT NULL DEFAULT 'PENDING', " +
                    "delivery_date DATETIME NULL, " +
                    "return_requested BOOLEAN DEFAULT FALSE, " +
                    "return_reason TEXT NULL, " +
                    "return_approved BOOLEAN DEFAULT FALSE, " +
                    "FOREIGN KEY (customer_id) REFERENCES users(id)" +
                    ")";
            stmt.executeUpdate(createOrdersTable);
            System.out.println("Orders table created or already exists.");

            // Create order_items table
            String createOrderItemsTable = "CREATE TABLE IF NOT EXISTS order_items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "order_id INT NOT NULL, " +
                    "product_id INT NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "price DECIMAL(10,2) NOT NULL, " +
                    "FOREIGN KEY (order_id) REFERENCES orders(id), " +
                    "FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")";
            stmt.executeUpdate(createOrderItemsTable);
            System.out.println("Order items table created or already exists.");

            // Create cart_items table
            String createCartItemsTable = "CREATE TABLE IF NOT EXISTS cart_items (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "customer_id INT NOT NULL, " +
                    "product_id INT NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "FOREIGN KEY (customer_id) REFERENCES users(id), " +
                    "FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")";
            stmt.executeUpdate(createCartItemsTable);
            System.out.println("Cart items table created or already exists.");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}