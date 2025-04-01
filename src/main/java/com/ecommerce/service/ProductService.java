package com.ecommerce.service;

import com.ecommerce.backend.DatabaseManager;
import com.ecommerce.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductService {

    private static ProductService instance;
    private Connection connection;

    private ProductService() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public static ProductService getInstance() {
        if (instance == null) {
            instance = new ProductService();
        }
        return instance;
    }

    // Method to add a product
    public void addProduct(Product product) {
        String sql = "INSERT INTO products (name, description, price, stock_quantity, category, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStockQuantity());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getImageUrl());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        product.setId(id); // Set the auto-generated ID
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to get a product by ID
    public Optional<Product> getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getString("category"),
                        rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Method to get all products
    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getString("category"),
                        rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    // Method to get products by category
    public List<Product> getProductsByCategory(String category) {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getString("category"),
                        rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    // Method to search products by name or description
    public List<Product> searchProducts(String query) {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ? OR description LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getString("category"),
                        rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    // Method to update a product
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock_quantity = ?, " +
                "category = ?, image_url = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStockQuantity());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getImageUrl());
            stmt.setInt(7, product.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to delete a product
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to check if a product is in stock
    public boolean isProductInStock(int id, int quantity) {
        String sql = "SELECT stock_quantity FROM products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int stockQuantity = rs.getInt("stock_quantity");
                return stockQuantity >= quantity;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to reduce stock when a product is purchased
    public boolean reduceStock(int productId, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean increaseStock(int productId, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean decreaseStock(int productId, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}