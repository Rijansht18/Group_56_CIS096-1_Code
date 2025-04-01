package com.ecommerce.model;

public class Product {
    private int id; // Changed from String to int
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private String category;
    private String imageUrl;

    public Product(int id, String name, String description, double price, int stockQuantity,
                   String category, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Method to check if product is in stock
    public boolean isInStock() {
        return stockQuantity > 0;
    }

    // Method to reduce stock when purchased
    public boolean reduceStock(int quantity) {
        if (quantity <= stockQuantity) {
            stockQuantity -= quantity;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return name + " - $" + price;
    }
}