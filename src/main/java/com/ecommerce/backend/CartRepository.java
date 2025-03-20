package com.ecommerce.backend;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartRepository {

    private Connection connection;

    public CartRepository() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // Method to load cart items for a customer
    public List<CartItem> loadCartItems(int customerId) {
        List<CartItem> cartItems = new ArrayList<>();
        String sql = "SELECT * FROM cart_items WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");
                Product product = ProductService.getInstance().getProductById(productId).orElse(null);
                if (product != null) {
                    cartItems.add(new CartItem(product, quantity));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartItems;
    }

    // Method to save cart items for a customer
    public void saveCartItems(int customerId, List<CartItem> cartItems) {
        String deleteSql = "DELETE FROM cart_items WHERE customer_id = ?";
        String insertSql = "INSERT INTO cart_items (customer_id, product_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);
             PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            // Delete existing cart items for the customer
            deleteStmt.setInt(1, customerId);
            deleteStmt.executeUpdate();

            // Insert new cart items
            for (CartItem item : cartItems) {
                insertStmt.setInt(1, customerId);
                insertStmt.setInt(2, item.getProduct().getId());
                insertStmt.setInt(3, item.getQuantity());
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to clear cart items for a customer
    public void clearCart(int customerId) {
        String sql = "DELETE FROM cart_items WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}