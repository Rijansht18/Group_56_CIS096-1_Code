package com.ecommerce.service;

import com.ecommerce.backend.DatabaseManager;
import com.ecommerce.model.*;
import javafx.scene.control.Alert;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderService {

    private static OrderService instance;
    private Connection connection;
    private ProductService productService;

    private OrderService() {
        this.connection = DatabaseManager.getInstance().getConnection();
        this.productService = ProductService.getInstance();
    }

    public static OrderService getInstance() {
        if (instance == null) {
            instance = new OrderService();
        }
        return instance;
    }

    public Order createOrder(Customer customer, String shippingAddress) {
        Cart cart = customer.getCart();
        if (cart.isEmpty()) {
            return null;
        }

        // Verify all products are in stock
        for (CartItem item : cart.getItems()) {
            if (!productService.isProductInStock(item.getProduct().getId(), item.getQuantity())) {
                return null;
            }
        }

        // Create the order in database
        String sql = "INSERT INTO orders (customer_id, order_date, total_amount, shipping_address, status) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, customer.getId());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setDouble(3, cart.getTotalPrice());
            stmt.setString(4, shippingAddress);
            stmt.setString(5, "PENDING");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);

                        // Save order items
                        saveOrderItems(orderId, cart.getItems());

                        // Reduce stock quantities
                        reduceStockQuantities(cart.getItems());

                        // Create and return order object
                        Order order = new Order(
                                orderId,
                                customer.getId(),
                                cart.getItems(),
                                shippingAddress
                        );
                        order.setTotalAmount(cart.getTotalPrice());

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

    private void saveOrderItems(int orderId, List<CartItem> items) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (CartItem item : items) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, item.getProduct().getId());
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getProduct().getPrice());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void reduceStockQuantities(List<CartItem> items) {
        for (CartItem item : items) {
            productService.reduceStock(item.getProduct().getId(), item.getQuantity());
        }
    }

    public boolean cancelOrder(int orderId) {
        try {
            connection.setAutoCommit(false); // Start transaction

            Optional<Order> orderOpt = getOrderByIdWithItems(orderId); // Need to get items
            if (!orderOpt.isPresent()) {
                return false;
            }

            Order order = orderOpt.get();
            if (!order.canBeCancelled()) {
                return false;
            }

            // 1. Restore stock quantities
            for (CartItem item : order.getItems()) {
                productService.increaseStock(item.getProduct().getId(), item.getQuantity());
            }

            // 2. Update order status
            String updateSql = "UPDATE orders SET status = 'CANCELLED' WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                stmt.setInt(1, orderId);
                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    connection.commit(); // Commit transaction
                    return true;
                }
            }
            connection.rollback(); // Rollback if update failed
            return false;

        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback on error
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Optional<Order> getOrderByIdWithItems(int orderId) {
        // Same implementation as your existing getOrderById() method
        // that loads items along with the order
        return getOrderById(orderId);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void restoreStockQuantities(List<CartItem> items) {
        for (CartItem item : items) {
            productService.increaseStock(item.getProduct().getId(), item.getQuantity());
        }
    }

    public boolean requestReturn(int orderId, String reason) {
        try {
            connection.setAutoCommit(false);

            // First verify the order can be returned
            Optional<Order> orderOpt = getOrderById(orderId);
            if (!orderOpt.isPresent() ||
                    !orderOpt.get().getStatus().equalsIgnoreCase("DELIVERED") ||
                    orderOpt.get().getDeliveryDate() == null ||
                    orderOpt.get().getDeliveryDate().isBefore(LocalDateTime.now().minusDays(7))) {
                connection.rollback();
                return false;
            }

            // Use the exact enum value 'RETURN_REQUESTED'
            String sql = "UPDATE orders SET " +
                    "return_requested = TRUE, " +
                    "return_reason = ?, " +
                    "status = 'RETURN_REQUESTED' " +
                    "WHERE id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, reason);
                stmt.setInt(2, orderId);
                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    connection.commit();
                    return true;
                }
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean approveReturn(int orderId) {
        try {
            connection.setAutoCommit(false);

            Optional<Order> orderOpt = getOrderByIdWithItems(orderId);
            if (!orderOpt.isPresent() || !orderOpt.get().isReturnRequested()) {
                connection.rollback();
                return false;
            }

            Order order = orderOpt.get();

            // Restore stock quantities
            for (CartItem item : order.getItems()) {
                if (!productService.increaseStock(item.getProduct().getId(), item.getQuantity())) {
                    connection.rollback();
                    return false;
                }
            }

            // Use the exact enum value 'RETURNED'
            String sql = "UPDATE orders SET " +
                    "return_approved = TRUE, " +
                    "status = 'RETURNED' " +
                    "WHERE id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, orderId);
                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    connection.commit();
                    return true;
                }
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Optional<Order> getOrderById(int id) {
        String sql = "SELECT o.*, oi.product_id, oi.quantity, oi.price, " +
                "p.name as product_name, p.description, p.category, p.image_url " +
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE o.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            Order order = null;
            List<CartItem> items = new ArrayList<>();

            while (rs.next()) {
                if (order == null) {
                    order = new Order(
                            rs.getInt("id"),
                            rs.getInt("customer_id"),
                            items,
                            rs.getString("shipping_address")
                    );
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                    order.setStatus(rs.getString("status"));

                    if (rs.getTimestamp("delivery_date") != null) {
                        order.setDeliveryDate(rs.getTimestamp("delivery_date").toLocalDateTime());
                    }
                    order.setReturnRequested(rs.getBoolean("return_requested"));
                    order.setReturnReason(rs.getString("return_reason"));
                    order.setReturnApproved(rs.getBoolean("return_approved"));
                }

                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        0, // Stock not needed here
                        rs.getString("category"),
                        rs.getString("image_url")
                );

                items.add(new CartItem(product, rs.getInt("quantity")));
            }

            return Optional.ofNullable(order);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ?, delivery_date = CASE WHEN ? = 'DELIVERED' THEN NOW() ELSE delivery_date END WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setString(2, newStatus);
            stmt.setInt(3, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setDeliveryDate(int orderId, LocalDateTime deliveryDate) {
        String sql = "UPDATE orders SET delivery_date = ?, status = 'DELIVERED' WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(deliveryDate));
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        new ArrayList<>(),
                        rs.getString("shipping_address")
                );
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                order.setStatus(rs.getString("status"));

                if (rs.getTimestamp("delivery_date") != null) {
                    order.setDeliveryDate(rs.getTimestamp("delivery_date").toLocalDateTime());
                }
                order.setReturnRequested(rs.getBoolean("return_requested"));
                order.setReturnReason(rs.getString("return_reason"));
                order.setReturnApproved(rs.getBoolean("return_approved"));

                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersByCustomerId(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        new ArrayList<>(),
                        rs.getString("shipping_address")
                );
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                order.setStatus(rs.getString("status"));

                if (rs.getTimestamp("delivery_date") != null) {
                    order.setDeliveryDate(rs.getTimestamp("delivery_date").toLocalDateTime());
                }
                order.setReturnRequested(rs.getBoolean("return_requested"));
                order.setReturnReason(rs.getString("return_reason"));
                order.setReturnApproved(rs.getBoolean("return_approved"));

                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    private List<CartItem> getOrderItems(int orderId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, p.name, p.description, p.price, p.category, p.image_url " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        0, // Current stock not needed here
                        rs.getString("category"),
                        rs.getString("image_url")
                );
                items.add(new CartItem(product, rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}