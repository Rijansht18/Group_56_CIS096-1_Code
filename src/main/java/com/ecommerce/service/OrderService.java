package com.ecommerce.service;

import com.ecommerce.backend.DatabaseManager;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderService {

    private static OrderService instance;
    private Connection connection;

    private OrderService() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public static OrderService getInstance() {
        if (instance == null) {
            instance = new OrderService();
        }
        return instance;
    }

    // Method to create a new order from a customer's cart
    public Order createOrder(Customer customer, String shippingAddress) {
        Cart cart = customer.getCart();
        if (cart.isEmpty()) {
            return null; // Can't create an order with an empty cart
        }

        // Check if all products are in stock
        boolean allInStock = cart.getItems().stream()
                .allMatch(item -> ProductService.getInstance().isProductInStock(item.getProduct().getId(), item.getQuantity()));

        if (!allInStock) {
            return null; // Some products are not in stock
        }

        // Reduce stock for all products
        cart.getItems().forEach(item ->
                ProductService.getInstance().reduceStock(item.getProduct().getId(), item.getQuantity()));

        // Create the order
        String sql = "INSERT INTO orders (customer_id, order_date, total_amount, shipping_address, status) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, customer.getId());
            stmt.setObject(2, LocalDateTime.now());
            stmt.setDouble(3, cart.getTotalPrice());
            stmt.setString(4, shippingAddress);
            stmt.setString(5, "PENDING");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        Order order = new Order(orderId, customer.getId(), cart.getItems(), shippingAddress);
                        customer.addOrder(order);
                        cart.clear();
                        return order;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to get an order by ID
    public Optional<Order> getOrderById(int id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Order(
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        new ArrayList<>(), // Items are not loaded here for simplicity
                        rs.getString("shipping_address")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Method to get all orders
    public List<Order> getAllOrders() {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orderList.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderList;
    }

    // Method to get orders by customer ID
    public List<Order> getOrdersByCustomerId(int customerId) {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orderList.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderList;
    }

    // Method to update an order's status
    public boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to cancel an order
    public boolean cancelOrder(int orderId) {
        return updateOrderStatus(orderId, "CANCELLED");
    }

    // Method to get orders by status
    public List<Order> getOrdersByStatus(String status) {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orderList.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        new ArrayList<>(), // Items are not loaded here for simplicity
                        rs.getString("shipping_address")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderList;
    }

    // Helper method to map a ResultSet row to an Order object
    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        Order order = new Order(
                rs.getInt("id"),
                rs.getInt("customer_id"),
                new ArrayList<>(), // Items are not loaded here for simplicity
                rs.getString("shipping_address")
        );
        order.setStatus(rs.getString("status")); // Map the status field
        order.setTotalAmount(rs.getDouble("total_amount")); // Map the total amount field
        return order;
    }
}