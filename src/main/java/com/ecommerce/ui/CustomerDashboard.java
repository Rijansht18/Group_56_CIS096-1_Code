package com.ecommerce.ui;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.SessionManager;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.collections.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;
import javafx.util.Pair;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerDashboard {
    private BorderPane root;
    private Stage primaryStage;
    private SessionManager sessionManager;
    private ProductService productService;
    private OrderService orderService;
    private Customer customer;
    private TableView<Order> ordersTable;

    // UI Components
    private FlowPane productsFlowPane;
    private Map<Integer, VBox> productCardMap = new HashMap<>();
    private ComboBox<String> categoryComboBox;
    private TextField searchField;

    public CustomerDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.sessionManager = SessionManager.getInstance();
        this.productService = ProductService.getInstance();
        this.orderService = OrderService.getInstance();
        this.customer = (Customer) sessionManager.getCurrentUser();

        createUI();
    }

    private void createUI() {
        root = new BorderPane();
        primaryStage.setMaximized(true);

        // Create the header
        HBox header = createHeader();
        root.setTop(header);

        // Create the main content area with tabs
        TabPane tabPane = new TabPane();

        // Products tab
        Tab productsTab = new Tab("Products");
        productsTab.setContent(createProductsPanel());
        productsTab.setClosable(false);

        // Cart tab
        Tab cartTab = new Tab("Shopping Cart");
        cartTab.setContent(createCartPanel());
        cartTab.setClosable(false);

        // Orders tab
        Tab ordersTab = new Tab("My Orders");
        ordersTab.setContent(createOrdersPanel());
        ordersTab.setClosable(false);

        // Profile tab
        Tab profileTab = new Tab("My Profile");
        profileTab.setContent(createProfilePanel());
        profileTab.setClosable(false);

        tabPane.getTabs().addAll(productsTab, cartTab, ordersTab, profileTab);
        root.setCenter(tabPane);

        // Create the footer
        HBox footer = createFooter();
        root.setBottom(footer);
    }

    private HBox createHeader() {
        Text headerText = new Text("E-Commerce Application - Customer Dashboard");
        headerText.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox leftSide = new HBox(headerText);
        leftSide.setAlignment(Pos.CENTER_LEFT);

        Label welcomeLabel = new Label("Welcome, " + customer.getFullName());
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> handleLogout());

        HBox rightSide = new HBox(10, welcomeLabel, logoutButton);
        rightSide.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setSpacing(10);
        header.setStyle("-fx-background-color: #f0f0f0;");

        HBox.setHgrow(leftSide, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(rightSide, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(leftSide, rightSide);
        return header;
    }

    private HBox createFooter() {
        Text footerText = new Text("© 2025 E-Commerce Application");
        HBox footer = new HBox(footerText);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: #f0f0f0;");
        return footer;
    }

    private ScrollPane createProductsPanel() {
        VBox productsPanel = new VBox(15);
        productsPanel.setPadding(new Insets(15));

        // Initialize the FlowPane
        productsFlowPane = new FlowPane();
        productsFlowPane.setHgap(15);
        productsFlowPane.setVgap(15);
        productsFlowPane.setPadding(new Insets(15));

        // Search and filter section
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // Initialize class-level fields
        searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.setPrefWidth(300);

        // Add listener for real-time filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts();
        });

        // Enable Enter key to perform search
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                filterProducts();
            }
        });

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> {
            filterProducts();
        });

        Label categoryLabel = new Label("Category:");
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("All Categories", "Electronics", "Clothing", "Footwear", "Accessories");
        categoryComboBox.setValue("All Categories");

        // Add listener for category changes
        categoryComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts();
        });

        searchBox.getChildren().addAll(searchField, searchButton, categoryLabel, categoryComboBox);

        // Initial load of products
        refreshProductsView();

        productsPanel.getChildren().addAll(searchBox, new Separator(), productsFlowPane);

        ScrollPane scrollPane = new ScrollPane(productsPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        return scrollPane;
    }

    private void filterProducts() {
        String searchQuery = searchField.getText();
        String category = categoryComboBox.getValue();

        List<Product> filteredProducts;

        if ((searchQuery == null || searchQuery.isEmpty()) && "All Categories".equals(category)) {
            filteredProducts = productService.getAllProducts();
        } else if (!(searchQuery == null || searchQuery.isEmpty()) && "All Categories".equals(category)) {
            filteredProducts = productService.searchProducts(searchQuery);
        } else if ((searchQuery == null || searchQuery.isEmpty()) && !"All Categories".equals(category)) {
            filteredProducts = productService.getProductsByCategory(category);
        } else {
            filteredProducts = productService.searchProducts(searchQuery).stream()
                    .filter(p -> p.getCategory().equals(category))
                    .toList();
        }

        productsFlowPane.getChildren().clear();
        for (Product product : filteredProducts) {
            productsFlowPane.getChildren().add(createProductCard(product));
        }
    }


    private void refreshProductsView() {
        if (productsFlowPane != null) {
            productsFlowPane.getChildren().clear();
            List<Product> products = productService.getAllProducts();
            for (Product product : products) {
                VBox card = createProductCard(product);
                productsFlowPane.getChildren().add(card);
                productCardMap.put(product.getId(), card);
            }
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10;");
        card.setPrefWidth(300);
        card.setPrefHeight(300);

        // Product image
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(product.getImageUrl());
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(300);
            imageView.setFitHeight(180);
        } catch (Exception e) {
            imageView.setImage(new Image("/images/default.jpg"));
        }

        // Product name
        Text nameText = new Text(product.getName());
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Product price and stock
        HBox infoBox = new HBox(10);
        Text priceText = new Text(String.format("$%.2f", product.getPrice()));
        Label stockLabel = new Label("Stock: " + product.getStockQuantity());
        stockLabel.setStyle(product.getStockQuantity() > 0 ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        infoBox.getChildren().addAll(priceText, stockLabel);

        // Add to cart button
        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setMaxWidth(Double.MAX_VALUE);

        if (product.getStockQuantity() <= 0) {
            addToCartButton.setDisable(true);
            addToCartButton.setText("Out of Stock");
        }

        addToCartButton.setOnAction(e -> {
            if (product.getStockQuantity() > 0) {
                customer.getCart().addProduct(product, 1);
                refreshProductsView();
                showAlert(Alert.AlertType.INFORMATION, "Product Added",
                        product.getName() + " has been added to your cart.");
            }
        });

        card.getChildren().addAll(imageView, nameText, infoBox, addToCartButton);
        return card;
    }

    private VBox createCartPanel() {
        VBox cartPanel = new VBox(15);
        cartPanel.setPadding(new Insets(15));

        Text cartTitle = new Text("Your Shopping Cart");
        cartTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Cart summary labels
        Label totalItemsValue = new Label();
        Label totalPriceValue = new Label();

        // Cart items list with quantity controls
        ListView<CartItem> cartListView = new ListView<>();
        refreshCartItems(cartListView, totalItemsValue, totalPriceValue);

        cartListView.setCellFactory(param -> new ListCell<CartItem>() {
            private Label subtotalLabel = new Label();

            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox itemBox = new HBox(10);
                    itemBox.setAlignment(Pos.CENTER_LEFT);

                    // Product information
                    Label nameLabel = new Label(item.getProduct().getName());
                    Label priceLabel = new Label(String.format("$%.2f", item.getProduct().getPrice()));

                    // Quantity controls
                    Spinner<Integer> quantitySpinner = new Spinner<>(1, 100, item.getQuantity());
                    quantitySpinner.setEditable(true);

                    // Initialize subtotal
                    updateSubtotalLabel(item, subtotalLabel);

                    quantitySpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (newValue != null && !newValue.equals(oldValue)) {
                            item.setQuantity(newValue);
                            updateSubtotalLabel(item, subtotalLabel);
                            updateCartTotals(totalItemsValue, totalPriceValue);
                        }
                    });

                    // Remove button
                    Button removeButton = new Button("Remove");
                    removeButton.setOnAction(e -> {
                        customer.getCart().removeProduct(item.getProduct().getId());
                        refreshCartItems(cartListView, totalItemsValue, totalPriceValue);
                        refreshProductsView();
                    });

                    itemBox.getChildren().addAll(
                            nameLabel,
                            priceLabel,
                            new Label("Qty:"),
                            quantitySpinner,
                            new Label("Subtotal:"),
                            subtotalLabel,
                            removeButton
                    );
                    setGraphic(itemBox);
                }
            }
        });

        // Cart summary
        GridPane cartSummary = new GridPane();
        cartSummary.setHgap(10);
        cartSummary.setVgap(10);
        cartSummary.setPadding(new Insets(15));

        Label totalItemsLabel = new Label("Total Items:");
        Label totalPriceLabel = new Label("Total Price:");

        cartSummary.add(totalItemsLabel, 0, 0);
        cartSummary.add(totalItemsValue, 1, 0);
        cartSummary.add(totalPriceLabel, 0, 1);
        cartSummary.add(totalPriceValue, 1, 1);

        // Checkout button
        Button checkoutButton = new Button("Proceed to Checkout");
        checkoutButton.setMaxWidth(Double.MAX_VALUE);
        checkoutButton.setOnAction(e -> {
            if (customer.getCart().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Cart",
                        "Your cart is empty. Please add some products before checkout.");
            } else {
                showCheckoutDialog();
                refreshCartItems(cartListView, totalItemsValue, totalPriceValue);
                refreshProductsView();
            }
        });

        // Refresh button
        Button refreshButton = new Button("Refresh Cart");
        refreshButton.setMaxWidth(Double.MAX_VALUE);
        refreshButton.setOnAction(e -> refreshCartItems(cartListView, totalItemsValue, totalPriceValue));

        cartPanel.getChildren().addAll(cartTitle, cartListView, cartSummary, checkoutButton, refreshButton);
        return cartPanel;
    }

    private void updateSubtotalLabel(CartItem item, Label subtotalLabel) {
        subtotalLabel.setText(String.format("$%.2f", item.getSubtotal()));
    }

    private void refreshCartItems(ListView<CartItem> cartListView, Label totalItemsValue, Label totalPriceValue) {
        cartListView.setItems(FXCollections.observableArrayList(customer.getCart().getItems()));
        updateCartTotals(totalItemsValue, totalPriceValue);
    }

    private void updateCartTotals(Label totalItemsValue, Label totalPriceValue) {
        totalItemsValue.setText(String.valueOf(customer.getCart().getTotalItems()));
        totalPriceValue.setText(String.format("$%.2f", customer.getCart().getTotalPrice()));
    }

    private VBox createOrdersPanel() {
        VBox ordersPanel = new VBox(15);
        ordersPanel.setPadding(new Insets(15));

        Text ordersTitle = new Text("Your Orders");
        ordersTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Initialize the class-level ordersTable instead of local variable
        ordersTable = new TableView<>();

        // Order ID column
        TableColumn<Order, Integer> orderIdColumn = new TableColumn<>("Order ID");
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Order Date column
        TableColumn<Order, LocalDateTime> orderDateColumn = new TableColumn<>("Date");
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });

        // Delivery Date column
        TableColumn<Order, LocalDateTime> deliveryDateColumn = new TableColumn<>("Delivery Date");
        deliveryDateColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
        deliveryDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });

        // Total Amount column
        TableColumn<Order, Double> orderTotalColumn = new TableColumn<>("Total");
        orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        orderTotalColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        // Status column
        TableColumn<Order, String> orderStatusColumn = new TableColumn<>("Status");
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Return Status column
        TableColumn<Order, String> returnStatusColumn = new TableColumn<>("Return Status");
        returnStatusColumn.setCellValueFactory(cell -> {
            Order order = cell.getValue();
            if (order == null) {
                return new SimpleStringProperty("N/A");
            }
            if (order.isReturnApproved()) {
                return new SimpleStringProperty("Approved");
            } else if (order.isReturnRequested()) {
                return new SimpleStringProperty("Pending: " + order.getReturnReason());
            }
            return new SimpleStringProperty("N/A");
        });

        ordersTable.getColumns().addAll(orderIdColumn, orderDateColumn, deliveryDateColumn,
                orderTotalColumn, orderStatusColumn, returnStatusColumn);

        // Separate Cancel Action Column
        TableColumn<Order, Void> cancelActionColumn = new TableColumn<>("Cancel");
        cancelActionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button cancelButton = new Button("Cancel");

            {
                cancelButton.getStyleClass().add("action-button");
                cancelButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleCancelOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().isEmpty()) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    setGraphic(order != null &&
                            (order.getStatus().equalsIgnoreCase("PENDING") ||
                                    order.getStatus().equalsIgnoreCase("PROCESSING"))
                            ? cancelButton : null);
                }
            }
        });

// Separate Return Action Column (only shows for delivered orders within 7 days)
        TableColumn<Order, Void> returnActionColumn = new TableColumn<>("Return");
        returnActionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button returnButton = new Button("Return");

            {
                returnButton.getStyleClass().add("action-button");
                returnButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleReturnRequest(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().isEmpty()) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    boolean showReturn = order != null &&
                            order.getStatus().equalsIgnoreCase("DELIVERED") &&
                            order.getDeliveryDate() != null &&
                            order.getDeliveryDate().isAfter(LocalDateTime.now().minusDays(7)) &&
                            !order.isReturnRequested();
                    setGraphic(showReturn ? returnButton : null);
                }
            }
        });

// Add just these two action columns to the table
        ordersTable.getColumns().addAll(cancelActionColumn, returnActionColumn);

        // Load initial data
        refreshOrdersTable();

        // Refresh button
        Button refreshButton = new Button("Refresh Orders");
        refreshButton.setOnAction(e -> refreshOrdersTable());

        ordersPanel.getChildren().addAll(ordersTitle, ordersTable, refreshButton);
        return ordersPanel;
    }

    private void handleCancelOrder(Order order) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Cancellation");
        confirmation.setHeaderText("Cancel Order #" + order.getId());
        confirmation.setContentText("Are you sure you want to cancel this order?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = orderService.cancelOrder(order.getId());
            if (success) {
                refreshOrdersTable();
                refreshProductsView();
                showAlert(Alert.AlertType.INFORMATION, "Success", // Updated to use 3 parameters
                        "Order #" + order.getId() + " has been cancelled successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", // Updated to use 3 parameters
                        "Could not cancel order. It may have already been processed.");
            }
        }
    }

    private void refreshOrdersTable() {
        if (ordersTable != null) {
            List<Order> updatedOrders = orderService.getOrdersByCustomerId(customer.getId());
            ordersTable.setItems(FXCollections.observableArrayList(updatedOrders));

            // Maintain any existing sort order
            ObservableList<TableColumn<Order, ?>> sortOrder = ordersTable.getSortOrder();
            ordersTable.sort();
            ordersTable.getSortOrder().setAll(sortOrder);
        }
    }

    private void handleReturnRequest(Order order) {
        // Create a dialog for the return reason
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Return Request");
        dialog.setHeaderText("Request Return for Order #" + order.getId());
        dialog.setContentText("Please enter the reason for return:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            boolean success = orderService.requestReturn(order.getId(), reason);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Return Requested",
                        "Your return request has been submitted for approval.");
                refreshOrdersTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Request Failed",
                        "Could not process return request. Please ensure:\n" +
                                "- Order was delivered within 7 days\n" +
                                "- Return hasn't already been requested");
            }
        });
    }

    private void handleAddToCart(Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            showAlert(Alert.AlertType.WARNING, "Insufficient Stock",
                    "Not enough stock available for " + product.getName());
            return;
        }

        // Reduce stock in database
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productService.updateProduct(product);

        // Add to cart
        customer.getCart().addProduct(product, quantity);
        refreshProductsView();

        showAlert(Alert.AlertType.INFORMATION, "Product Added",
                product.getName() + " (x" + quantity + ") has been added to your cart.");
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
        TextField usernameField = new TextField(customer.getUsername());
        usernameField.setEditable(false);

        // Email field
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField(customer.getEmail());

        // Full name field
        Label fullNameLabel = new Label("Full Name:");
        TextField fullNameField = new TextField(customer.getFullName());

        // Address field
        Label addressLabel = new Label("Address:");
        TextField addressField = new TextField(customer.getAddress());

        // Phone number field
        Label phoneLabel = new Label("Phone Number:");
        TextField phoneField = new TextField(customer.getPhoneNumber());

        // Add fields to the form
        profileForm.add(usernameLabel, 0, 0);
        profileForm.add(usernameField, 1, 0);
        profileForm.add(emailLabel, 0, 1);
        profileForm.add(emailField, 1, 1);
        profileForm.add(fullNameLabel, 0, 2);
        profileForm.add(fullNameField, 1, 2);
        profileForm.add(addressLabel, 0, 3);
        profileForm.add(addressField, 1, 3);
        profileForm.add(phoneLabel, 0, 4);
        profileForm.add(phoneField, 1, 4);

        // Update button
        Button updateButton = new Button("Update Profile");
        updateButton.setMaxWidth(Double.MAX_VALUE);

        // Add event handler
        updateButton.setOnAction(e -> {
            customer.setEmail(emailField.getText());
            customer.setFullName(fullNameField.getText());
            customer.setAddress(addressField.getText());
            customer.setPhoneNumber(phoneField.getText());

            showAlert(Alert.AlertType.INFORMATION, "Profile Updated",
                    "Your profile has been updated successfully.");
        });

        profilePanel.getChildren().addAll(profileTitle, profileForm, updateButton);
        return profilePanel;
    }

    private void showCheckoutDialog() {
        if (customer.getCart().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart",
                    "Your cart is empty. Please add some products before checkout.");
            return;
        }

        // Create dialog for address and contact details
        Dialog<Pair<String, String>> addressDialog = new Dialog<>();
        addressDialog.setTitle("Shipping Details");
        addressDialog.setHeaderText("Enter your shipping information:");

        // Form components
        TextField addressField = new TextField(customer.getAddress());
        TextField phoneField = new TextField(customer.getPhoneNumber());

        // Form validation
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                phoneField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Shipping Address:"), 0, 0);
        grid.add(addressField, 1, 0);
        grid.add(new Label("Phone Number:"), 0, 1);
        grid.add(phoneField, 1, 1);

        addressDialog.getDialogPane().setContent(grid);
        addressDialog.getDialogPane().getButtonTypes().addAll(ButtonType.NEXT, ButtonType.CANCEL);

        // Convert result to address-phone pair
        addressDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.NEXT) {
                if (addressField.getText().isEmpty() || phoneField.getText().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields");
                    return null;
                }
                return new Pair<>(addressField.getText(), phoneField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = addressDialog.showAndWait();
        result.ifPresent(details -> showPaymentDialog(details.getKey(), details.getValue()));
    }

    private void showPaymentDialog(String shippingAddress, String phoneNumber) {
        Dialog<String> paymentDialog = new Dialog<>();
        paymentDialog.setTitle("Select Payment Method");
        paymentDialog.setHeaderText("Choose your payment option for order shipping to:\n" +
                shippingAddress + "\nPhone: " + phoneNumber);

        // Payment options
        ToggleGroup paymentGroup = new ToggleGroup();
        RadioButton codButton = new RadioButton("Cash on Delivery (COD)");
        RadioButton esewaButton = new RadioButton("eSewa (Coming Soon)");
        RadioButton khaltiButton = new RadioButton("Khalti (Coming Soon)");

        codButton.setToggleGroup(paymentGroup);
        esewaButton.setToggleGroup(paymentGroup);
        khaltiButton.setToggleGroup(paymentGroup);

        codButton.setSelected(true);
        esewaButton.setDisable(true);
        khaltiButton.setDisable(true);

        VBox paymentBox = new VBox(10, codButton, esewaButton, khaltiButton);
        paymentDialog.getDialogPane().setContent(paymentBox);

        // Add buttons
        paymentDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Convert result to payment method when OK is clicked
        paymentDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                RadioButton selected = (RadioButton) paymentGroup.getSelectedToggle();
                return selected.getText();
            }
            return null;
        });

        Optional<String> paymentResult = paymentDialog.showAndWait();
        paymentResult.ifPresent(paymentMethod -> {
            generateAndShowInvoice(shippingAddress, phoneNumber, paymentMethod);
        });
    }

    private void generateAndShowInvoice(String shippingAddress, String phoneNumber, String paymentMethod) {
        // Create invoice content
        StringBuilder invoiceContent = new StringBuilder();
        invoiceContent.append("=== E-Commerce Invoice ===\n\n");
        invoiceContent.append("Customer: ").append(customer.getFullName()).append("\n");
        invoiceContent.append("Phone: ").append(phoneNumber).append("\n");
        invoiceContent.append("Shipping Address: ").append(shippingAddress).append("\n");
        invoiceContent.append("Payment Method: ").append(paymentMethod).append("\n\n");
        invoiceContent.append("Order Items:\n");

        for (CartItem item : customer.getCart().getItems()) {
            invoiceContent.append(String.format("- %s x%d @ $%.2f = $%.2f\n",
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getProduct().getPrice(),
                    item.getSubtotal()));
        }

        invoiceContent.append("\nTotal: $").append(String.format("%.2f", customer.getCart().getTotalPrice())).append("\n");
        invoiceContent.append("\nThank you for your purchase!");

        // Show invoice in a dialog with download option
        Dialog<Void> invoiceDialog = new Dialog<>();
        invoiceDialog.setTitle("Order Invoice");
        invoiceDialog.setHeaderText("Your order has been placed successfully!");

        TextArea invoiceText = new TextArea(invoiceContent.toString());
        invoiceText.setEditable(false);
        invoiceText.setWrapText(true);
        invoiceText.setPrefSize(400, 300);

        Button downloadButton = new Button("Download Invoice as PDF");
        downloadButton.setOnAction(e -> {
            saveInvoiceAsPdf(shippingAddress, phoneNumber, paymentMethod, invoiceContent.toString());
        });

        VBox invoiceBox = new VBox(10, invoiceText, downloadButton);
        invoiceDialog.getDialogPane().setContent(invoiceBox);
        invoiceDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Create the order after showing invoice
        Order order = orderService.createOrder(customer, shippingAddress);
        if (order != null) {
            customer.getCart().clear();
            refreshProductsView();
        }

        invoiceDialog.showAndWait();
    }

    private void saveInvoiceAsPdf(String shippingAddress, String phoneNumber, String paymentMethod, String invoiceContent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Invoice");
            fileChooser.setInitialFileName("invoice_" + System.currentTimeMillis() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(primaryStage);
            if (file == null) return;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Add title
            Paragraph title = new Paragraph("E-Commerce Invoice",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Add order date (NEW)
            Paragraph orderDate = new Paragraph(
                    "Order Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            orderDate.setAlignment(Element.ALIGN_CENTER);
            document.add(orderDate);

            document.add(new Paragraph("\n"));

            // Add customer info
            Paragraph customerInfo = new Paragraph();
            customerInfo.add(new Chunk("Customer: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            customerInfo.add(customer.getFullName() + "\n");
            customerInfo.add(new Chunk("Phone: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            customerInfo.add(phoneNumber + "\n");
            customerInfo.add(new Chunk("Shipping Address: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            customerInfo.add(shippingAddress + "\n");
            customerInfo.add(new Chunk("Payment Method: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            customerInfo.add(paymentMethod + "\n");
            document.add(customerInfo);

            // Add order items table
            document.add(new Paragraph("\nOrder Details:"));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Table headers
            table.addCell(new Phrase("Product", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            table.addCell(new Phrase("Price", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            table.addCell(new Phrase("Qty", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            table.addCell(new Phrase("Subtotal", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));

            // Calculate total while adding items
            double totalAmount = 0.0;
            String[] lines = invoiceContent.split("\n");
            for (String line : lines) {
                if (line.startsWith("- ") && line.contains("@")) {
                    String[] parts = line.substring(2).split(" @ \\$| x| = \\$");
                    if (parts.length >= 4) {
                        String productName = parts[0].trim();
                        double price = Double.parseDouble(parts[2].trim());
                        int quantity = Integer.parseInt(parts[1].trim());
                        double subtotal = Double.parseDouble(parts[3].trim());

                        table.addCell(productName);
                        table.addCell("$" + String.format("%.2f", price));
                        table.addCell(String.valueOf(quantity));
                        table.addCell("$" + String.format("%.2f", subtotal));

                        totalAmount += subtotal;
                    }
                }
            }

            document.add(table);

            // Add total
            Paragraph total = new Paragraph();
            total.add(new Chunk("\nTotal: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            total.add("$" + String.format("%.2f", totalAmount));
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            // Add footer
            Paragraph footer = new Paragraph(
                    "\nThank you for your purchase!",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Invoice saved successfully!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save invoice: " + e.getMessage());
            e.printStackTrace();
        }
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