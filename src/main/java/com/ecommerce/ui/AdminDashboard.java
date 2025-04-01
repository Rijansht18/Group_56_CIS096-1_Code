package com.ecommerce.ui;

import com.ecommerce.model.Admin;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.SessionManager;
import com.ecommerce.service.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Optional;

public class AdminDashboard {
    private BorderPane root;
    private Stage primaryStage;
    private SessionManager sessionManager;
    private UserService userService;
    private ProductService productService;
    private OrderService orderService;
    private Admin admin;

    public AdminDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.sessionManager = SessionManager.getInstance();
        this.userService = UserService.getInstance();
        this.productService = ProductService.getInstance();
        this.orderService = OrderService.getInstance();

        // Get the current user as an Admin
        this.admin = (Admin) sessionManager.getCurrentUser();

        createUI();
    }

    private void createUI() {
        root = new BorderPane();

        // Create the header
        HBox header = createHeader();
        root.setTop(header);

        // Create the main content area with tabs
        TabPane tabPane = new TabPane();

        // Products tab
        Tab productsTab = new Tab("Manage Products");
        productsTab.setContent(createProductsPanel());
        productsTab.setClosable(false);

        // Orders tab
        Tab ordersTab = new Tab("Manage Orders");
        ordersTab.setContent(createOrdersPanel());
        ordersTab.setClosable(false);

        // Customers tab
        Tab customersTab = new Tab("Manage Customers");
        customersTab.setContent(createCustomersPanel());
        customersTab.setClosable(false);

        // Profile tab
        Tab profileTab = new Tab("My Profile");
        profileTab.setContent(createProfilePanel());
        profileTab.setClosable(false);

        tabPane.getTabs().addAll(productsTab, ordersTab, customersTab, profileTab);
        root.setCenter(tabPane);

        // Create the footer
        HBox footer = createFooter();
        root.setBottom(footer);
    }

    private HBox createHeader() {
        Text headerText = new Text("E-Commerce Application - Admin Dashboard");
        headerText.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox leftSide = new HBox(headerText);
        leftSide.setAlignment(Pos.CENTER_LEFT);

        Label welcomeLabel = new Label("Welcome, " + admin.getFullName() + " (" + admin.getRole() + ")");
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> handleLogout());

        HBox rightSide = new HBox(10, welcomeLabel, logoutButton);
        rightSide.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setSpacing(10);
        header.setStyle("-fx-background-color: #f0f0f0;");

        // Use all available width
        HBox.setHgrow(leftSide, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(rightSide, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(leftSide, rightSide);

        return header;
    }

    private HBox createFooter() {
        Text footerText = new Text("© 2023 E-Commerce Application");
        HBox footer = new HBox(footerText);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: #f0f0f0;");
        return footer;
    }

    private VBox createProductsPanel() {
        VBox productsPanel = new VBox(15);
        productsPanel.setPadding(new Insets(15));

        Text productsTitle = new Text("Manage Products");
        productsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Products table
        TableView<Product> productsTable = new TableView<>();

        TableColumn<Product, Integer> productIdColumn = new TableColumn<>("ID");
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> productNameColumn = new TableColumn<>("Name");
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Double> productPriceColumn = new TableColumn<>("Price");
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, Integer> productStockColumn = new TableColumn<>("Stock");
        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        TableColumn<Product, String> productCategoryColumn = new TableColumn<>("Category");
        productCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        productsTable.getColumns().addAll(productIdColumn, productNameColumn, productPriceColumn,
                productStockColumn, productCategoryColumn);

        // Populate the table with products
        productsTable.setItems(FXCollections.observableArrayList(productService.getAllProducts()));

        // Product form
        GridPane productForm = new GridPane();
        productForm.setHgap(10);
        productForm.setVgap(10);
        productForm.setPadding(new Insets(15));

        // Product name field
        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();

        // Product description field
        Label descriptionLabel = new Label("Description:");
        TextField descriptionField = new TextField();

        // Product price field
        Label priceLabel = new Label("Price:");
        TextField priceField = new TextField();

        // Product stock field
        Label stockLabel = new Label("Stock Quantity:");
        TextField stockField = new TextField();

        // Product category field
        Label categoryLabel = new Label("Category:");
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("Electronics", "Clothing", "Footwear", "Accessories");
        categoryComboBox.setValue("Electronics");

        // Product image URL field
        Label imageUrlLabel = new Label("Image URL:");
        TextField imageUrlField = new TextField();
        imageUrlField.setText("/images/default.jpg");

        // Add fields to the form
        productForm.add(nameLabel, 0, 0);
        productForm.add(nameField, 1, 0);
        productForm.add(descriptionLabel, 0, 1);
        productForm.add(descriptionField, 1, 1);
        productForm.add(priceLabel, 0, 2);
        productForm.add(priceField, 1, 2);
        productForm.add(stockLabel, 0, 3);
        productForm.add(stockField, 1, 3);
        productForm.add(categoryLabel, 0, 4);
        productForm.add(categoryComboBox, 1, 4);
        productForm.add(imageUrlLabel, 0, 5);
        productForm.add(imageUrlField, 1, 5);

        // Buttons
        Button addButton = new Button("Add Product");
        Button updateButton = new Button("Update Selected");
        Button deleteButton = new Button("Delete Selected");
        Button clearButton = new Button("Clear Form");

        HBox buttonBox = new HBox(10, addButton, updateButton, deleteButton, clearButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Add event handlers
        addButton.setOnAction(e -> {
            try {
                String name = nameField.getText();
                String description = descriptionField.getText();
                double price = Double.parseDouble(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());
                String category = categoryComboBox.getValue();
                String imageUrl = imageUrlField.getText();

                if (name.isEmpty() || description.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Name and description cannot be empty.");
                    return;
                }

                Product product = new Product(0, name, description, price, stock, category, imageUrl);
                productService.addProduct(product);

                productsTable.getItems().add(product);
                clearProductForm(nameField, descriptionField, priceField, stockField, categoryComboBox, imageUrlField);

                showAlert(Alert.AlertType.INFORMATION, "Product Added",
                        "Product has been added successfully.");
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Input Error",
                        "Price and stock quantity must be valid numbers.");
            }
        });

        updateButton.setOnAction(e -> {
            Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();

            if (selectedProduct == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection",
                        "Please select a product to update.");
                return;
            }

            try {
                String name = nameField.getText();
                String description = descriptionField.getText();
                double price = Double.parseDouble(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());
                String category = categoryComboBox.getValue();
                String imageUrl = imageUrlField.getText();

                if (name.isEmpty() || description.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Name and description cannot be empty.");
                    return;
                }

                selectedProduct.setName(name);
                selectedProduct.setDescription(description);
                selectedProduct.setPrice(price);
                selectedProduct.setStockQuantity(stock);
                selectedProduct.setCategory(category);
                selectedProduct.setImageUrl(imageUrl);

                productService.updateProduct(selectedProduct);
                productsTable.refresh();

                showAlert(Alert.AlertType.INFORMATION, "Product Updated",
                        "Product has been updated successfully.");
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Input Error",
                        "Price and stock quantity must be valid numbers.");
            }
        });

        deleteButton.setOnAction(e -> {
            Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();

            if (selectedProduct == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection",
                        "Please select a product to delete.");
                return;
            }

            productService.deleteProduct(selectedProduct.getId());
            productsTable.getItems().remove(selectedProduct);

            showAlert(Alert.AlertType.INFORMATION, "Product Deleted",
                    "Product has been deleted successfully.");
        });

        clearButton.setOnAction(e -> {
            clearProductForm(nameField, descriptionField, priceField, stockField, categoryComboBox, imageUrlField);
        });

        // Selection listener for the table
        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                descriptionField.setText(newSelection.getDescription());
                priceField.setText(String.valueOf(newSelection.getPrice()));
                stockField.setText(String.valueOf(newSelection.getStockQuantity()));
                categoryComboBox.setValue(newSelection.getCategory());
                imageUrlField.setText(newSelection.getImageUrl());
            }
        });

        productsPanel.getChildren().addAll(productsTitle, productsTable, productForm, buttonBox);

        return productsPanel;
    }

    private void clearProductForm(TextField nameField, TextField descriptionField,
                                  TextField priceField, TextField stockField,
                                  ComboBox<String> categoryComboBox, TextField imageUrlField) {
        nameField.clear();
        descriptionField.clear();
        priceField.clear();
        stockField.clear();
        categoryComboBox.setValue("Electronics");
        imageUrlField.setText("/images/default.jpg");
    }

    private VBox createOrdersPanel() {
        VBox ordersPanel = new VBox(15);
        ordersPanel.setPadding(new Insets(15));

        Text ordersTitle = new Text("Manage Orders");
        ordersTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Orders table
        TableView<Order> ordersTable = new TableView<>();

        TableColumn<Order, Integer> orderIdColumn = new TableColumn<>("Order ID");
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Order, Integer> customerIdColumn = new TableColumn<>("Customer ID");
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        TableColumn<Order, String> orderDateColumn = new TableColumn<>("Date");
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        TableColumn<Order, Double> orderTotalColumn = new TableColumn<>("Total");
        orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        TableColumn<Order, String> orderStatusColumn = new TableColumn<>("Status");
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Order, String> returnReasonColumn = new TableColumn<>("Return Reason");
        returnReasonColumn.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            if (order.isReturnRequested()) {
                return new SimpleStringProperty(order.getReturnReason());
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

//        TableColumn<Order, Void> approveReturnColumn = new TableColumn<>("Approve Return");
//        approveReturnColumn.setCellFactory(param -> new TableCell<>() {
//            private final Button approveButton = new Button("Approve");
//
//            {
//                approveButton.getStyleClass().add("action-button");
//                approveButton.setOnAction(event -> {
//                    Order order = getTableView().getItems().get(getIndex());
//                    if (order != null && order.canApproveReturn()) {
//                        boolean approved = orderService.approveReturn(order.getId());
//                        if (approved) {
//                            showAlert(Alert.AlertType.INFORMATION, "Success",
//                                    "Return approved successfully.");
//                        } else {
//                            showAlert(Alert.AlertType.ERROR, "Error",
//                                    "Failed to approve return.");
//                        }
//                    }
//                });
//            }
//
//            @Override
//            protected void updateItem(Void item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || getTableView().getItems().isEmpty()) {
//                    setGraphic(null);
//                } else {
//                    Order order = getTableView().getItems().get(getIndex());
//                    setGraphic(order != null && order.canApproveReturn() ? approveButton : null);
//                }
//            }
//        });

        ordersTable.getColumns().addAll(orderIdColumn, customerIdColumn, orderDateColumn,
                orderTotalColumn, orderStatusColumn,returnReasonColumn );

        // Populate the table with orders
        ordersTable.setItems(FXCollections.observableArrayList(orderService.getAllOrders()));

        // Order status update form
        GridPane orderForm = new GridPane();
        orderForm.setHgap(10);
        orderForm.setVgap(10);
        orderForm.setPadding(new Insets(15));

        Label orderIdLabel = new Label("Order ID:");
        TextField orderIdField = new TextField();
        orderIdField.setEditable(false);

        Label statusLabel = new Label("Status:");
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");

        // Add Approve Return button
        Button approveReturnButton = new Button("Approve Return");
        approveReturnButton.setOnAction(e -> {
            Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null && selectedOrder.isReturnRequested() && !selectedOrder.isReturnApproved()) {
                boolean approved = orderService.approveReturn(selectedOrder.getId());
                if (approved) {
                    ordersTable.refresh();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Return approved successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve return.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection",
                        "Please select a valid return request to approve.");
            }
        });

        orderForm.add(orderIdLabel, 0, 0);
        orderForm.add(orderIdField, 1, 0);
        orderForm.add(statusLabel, 0, 1);
        orderForm.add(statusComboBox, 1, 1);
        orderForm.add(approveReturnButton, 0, 2, 2, 1);

        // Buttons
        Button updateStatusButton = new Button("Update Status");
        Button refreshButton = new Button("Refresh Orders");

        HBox buttonBox = new HBox(10, updateStatusButton, refreshButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Add event handlers
        updateStatusButton.setOnAction(e -> {
            String orderId = orderIdField.getText();
            String newStatus = statusComboBox.getValue();

            if (orderId.isEmpty() || newStatus == null) {
                showAlert(Alert.AlertType.WARNING, "Input Error",
                        "Please select an order and a status.");
                return;
            }

            boolean updated = orderService.updateOrderStatus(Integer.parseInt(orderId), newStatus);

            if (updated) {
                // Refresh the orders table
                ordersTable.setItems(FXCollections.observableArrayList(orderService.getAllOrders()));
                showAlert(Alert.AlertType.INFORMATION, "Status Updated",
                        "Order status has been updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Error",
                        "Failed to update order status.");
            }
        });

        refreshButton.setOnAction(e -> {
            ordersTable.setItems(FXCollections.observableArrayList(orderService.getAllOrders()));
        });

        // Selection listener for the table
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                orderIdField.setText(String.valueOf(newSelection.getId()));
                statusComboBox.setValue(newSelection.getStatus());
            }
        });

        ordersPanel.getChildren().addAll(ordersTitle, ordersTable, orderForm, buttonBox);

        return ordersPanel;
    }

    private VBox createCustomersPanel() {
        VBox customersPanel = new VBox(15);
        customersPanel.setPadding(new Insets(15));

        Text customersTitle = new Text("Manage Customers");
        customersTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Customers table
        TableView<Customer> customersTable = new TableView<>();

        TableColumn<Customer, Integer> customerIdColumn = new TableColumn<>("ID");
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Customer, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Customer, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Customer, String> fullNameColumn = new TableColumn<>("Full Name");
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Customer, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        customersTable.getColumns().addAll(customerIdColumn, usernameColumn, emailColumn,
                fullNameColumn, phoneColumn);

        // Populate the table with customers
        customersTable.setItems(FXCollections.observableArrayList(userService.getAllCustomers()));

        // Customer details form
        GridPane customerForm = new GridPane();
        customerForm.setHgap(10);
        customerForm.setVgap(10);
        customerForm.setPadding(new Insets(15));

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setEditable(false);

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Label fullNameLabel = new Label("Full Name:");
        TextField fullNameField = new TextField();

        Label addressLabel = new Label("Address:");
        TextField addressField = new TextField();

        Label phoneLabel = new Label("Phone Number:");
        TextField phoneField = new TextField();

        customerForm.add(usernameLabel, 0, 0);
        customerForm.add(usernameField, 1, 0);
        customerForm.add(emailLabel, 0, 1);
        customerForm.add(emailField, 1, 1);
        customerForm.add(fullNameLabel, 0, 2);
        customerForm.add(fullNameField, 1, 2);
        customerForm.add(addressLabel, 0, 3);
        customerForm.add(addressField, 1, 3);
        customerForm.add(phoneLabel, 0, 4);
        customerForm.add(phoneField, 1, 4);

        // Buttons
        Button updateButton = new Button("Update Customer");
        Button deleteButton = new Button("Delete Customer");
        Button refreshButton = new Button("Refresh Customers");

        HBox buttonBox = new HBox(10, updateButton, deleteButton, refreshButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Add event handlers
        updateButton.setOnAction(e -> {
            Customer selectedCustomer = customersTable.getSelectionModel().getSelectedItem();

            if (selectedCustomer == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection",
                        "Please select a customer to update.");
                return;
            }

            selectedCustomer.setEmail(emailField.getText());
            selectedCustomer.setFullName(fullNameField.getText());
            selectedCustomer.setAddress(addressField.getText());
            selectedCustomer.setPhoneNumber(phoneField.getText());

            userService.updateUser(selectedCustomer);
            customersTable.refresh();

            showAlert(Alert.AlertType.INFORMATION, "Customer Updated",
                    "Customer has been updated successfully.");
        });

        deleteButton.setOnAction(e -> {
            Customer selectedCustomer = customersTable.getSelectionModel().getSelectedItem();

            if (selectedCustomer == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection",
                        "Please select a customer to delete.");
                return;
            }

            userService.deleteUser(selectedCustomer.getUsername());
            customersTable.getItems().remove(selectedCustomer);

            showAlert(Alert.AlertType.INFORMATION, "Customer Deleted",
                    "Customer has been deleted successfully.");
        });

        refreshButton.setOnAction(e -> {
            customersTable.setItems(FXCollections.observableArrayList(userService.getAllCustomers()));
        });

        // Selection listener for the table
        customersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                usernameField.setText(newSelection.getUsername());
                emailField.setText(newSelection.getEmail());
                fullNameField.setText(newSelection.getFullName());
                addressField.setText(newSelection.getAddress());
                phoneField.setText(newSelection.getPhoneNumber());
            }
        });

        customersPanel.getChildren().addAll(customersTitle, customersTable, customerForm, buttonBox);

        return customersPanel;
    }

    private VBox createProfilePanel() {
        VBox profilePanel = new VBox(15);
        profilePanel.setPadding(new Insets(15));

        Text profileTitle = new Text("Your Profile");
        profileTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Profile form
        GridPane profileForm = new GridPane();
        profileForm.setHgap(10);
        profileForm.setVgap(10);
        profileForm.setPadding(new Insets(15));

        // Username field
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField(admin.getUsername());
        usernameField.setEditable(false);

        // Email field
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField(admin.getEmail());

        // Full name field
        Label fullNameLabel = new Label("Full Name:");
        TextField fullNameField = new TextField(admin.getFullName());

        // Role field
        Label roleLabel = new Label("Role:");
        TextField roleField = new TextField(admin.getRole());

        // Department field
        Label departmentLabel = new Label("Department:");
        TextField departmentField = new TextField(admin.getDepartment());

        // Add fields to the form
        profileForm.add(usernameLabel, 0, 0);
        profileForm.add(usernameField, 1, 0);
        profileForm.add(emailLabel, 0, 1);
        profileForm.add(emailField, 1, 1);
        profileForm.add(fullNameLabel, 0, 2);
        profileForm.add(fullNameField, 1, 2);
        profileForm.add(roleLabel, 0, 3);
        profileForm.add(roleField, 1, 3);
        profileForm.add(departmentLabel, 0, 4);
        profileForm.add(departmentField, 1, 4);

        // Update button
        Button updateButton = new Button("Update Profile");
        updateButton.setMaxWidth(Double.MAX_VALUE);

        // Add event handler
        updateButton.setOnAction(e -> {
            admin.setEmail(emailField.getText());
            admin.setFullName(fullNameField.getText());
            admin.setRole(roleField.getText());
            admin.setDepartment(departmentField.getText());

            userService.updateUser(admin);

            showAlert(Alert.AlertType.INFORMATION, "Profile Updated",
                    "Your profile has been updated successfully.");
        });

        profilePanel.getChildren().addAll(profileTitle, profileForm, updateButton);

        return profilePanel;
    }

    private void handleLogout() {
        sessionManager.logout();

        LoginScreen loginScreen = new LoginScreen(primaryStage);
        Scene scene = new Scene(loginScreen.getRoot(), 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public BorderPane getRoot() {
        return root;
    }
}