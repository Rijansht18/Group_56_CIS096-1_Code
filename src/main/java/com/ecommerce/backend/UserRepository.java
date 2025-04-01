package com.ecommerce.backend;

import com.ecommerce.model.Admin;
import com.ecommerce.model.Customer;
import com.ecommerce.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private Connection connection;

    public UserRepository() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // Method to find a user by username
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Method to authenticate a user
    public Optional<User> authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Method to register a new customer
    public Customer registerCustomer(String username, String password, String email, String fullName,
                                     String address, String phoneNumber) {
        String sql = "INSERT INTO users (username, password, email, full_name, user_type, address, phone_number) " +
                "VALUES (?, ?, ?, ?, 'CUSTOMER', ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, fullName);
            stmt.setString(5, address);
            stmt.setString(6, phoneNumber);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        return new Customer(id, username, password, email, fullName, address, phoneNumber);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to register a new admin
    public Admin registerAdmin(String username, String password, String email, String fullName,
                               String role, String department) {
        String sql = "INSERT INTO users (username, password, email, full_name, user_type, role, department) " +
                "VALUES (?, ?, ?, ?, 'ADMIN', ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, fullName);
            stmt.setString(5, role);
            stmt.setString(6, department);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        return new Admin(id, username, password, email, fullName, role, department);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to get all users
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                userList.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    // Method to get all customers
    public List<Customer> getAllCustomers() {
        List<Customer> customerList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE user_type = 'CUSTOMER'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                customerList.add((Customer) mapRowToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customerList;
    }

    // Method to get all admins
    public List<Admin> getAllAdmins() {
        List<Admin> adminList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE user_type = 'ADMIN'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                adminList.add((Admin) mapRowToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adminList;
    }

    // Method to update a user
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET password = ?, email = ?, full_name = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getFullName());
            stmt.setInt(4, user.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to delete a user
    public boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Helper method to map a ResultSet row to a User object
    private User mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String email = rs.getString("email");
        String fullName = rs.getString("full_name");
        String userType = rs.getString("user_type");

        if ("ADMIN".equals(userType)) {
            return new Admin(id, username, password, email, fullName,
                    rs.getString("role"), rs.getString("department"));
        } else {
            return new Customer(id, username, password, email, fullName,
                    rs.getString("address"), rs.getString("phone_number"));
        }
    }
}