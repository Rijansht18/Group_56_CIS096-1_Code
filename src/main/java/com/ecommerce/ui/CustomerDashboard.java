package com.ecommerce.ui;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.SessionManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

import java.time.LocalDateTime;
import java.util.List;

public class CustomerDashboard {
    private BorderPane root;
    private Stage primaryStage;
    private SessionManager sessionManager;
    private ProductService productService;
    private OrderService orderService;
    private Customer customer;

    public CustomerDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.sessionManager = SessionManager.getInstance();
        this.productService = ProductService.getInstance();
        this.orderService = OrderService.getInstance();

        // Get the current user as a Customer
        this.customer = (Customer) sessionManager.getCurrentUser();

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

    private ScrollPane createProductsPanel() {
        VBox productsPanel = new VBox(15);
        productsPanel.setPadding(new Insets(15));

        // Search and filter section
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.setPrefWidth(300);

        Button searchButton = new Button("Search");

        Label categoryLabel = new Label("Category:");

        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().add("All Categories");
        categoryComboBox.getItems().addAll("Electronics", "Clothing", "Footwear", "Accessories");
        categoryComboBox.setValue("All Categories");

        searchBox.getChildren().addAll(searchField, searchButton, categoryLabel, categoryComboBox);

        // Products display
        FlowPane productsFlowPane = new FlowPane();
        productsFlowPane.setHgap(15);
        productsFlowPane.setVgap(15);
        productsFlowPane.setPadding(new Insets(15));

        // Add product cards
        List<Product> products = productService.getAllProducts();
        for (Product product : products) {
            productsFlowPane.getChildren().add(createProductCard(product));
        }

        // Add event handlers
        searchButton.setOnAction(e -> {
            String searchQuery = searchField.getText();
            String category = categoryComboBox.getValue();

            List<Product> filteredProducts;
            if (searchQuery.isEmpty() && "All Categories".equals(category)) {
                filteredProducts = productService.getAllProducts();
            } else if (!searchQuery.isEmpty() && "All Categories".equals(category)) {
                filteredProducts = productService.searchProducts(searchQuery);
            } else if (searchQuery.isEmpty() && !"All Categories".equals(category)) {
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
        });

        categoryComboBox.setOnAction(e -> {
            String searchQuery = searchField.getText();
            String category = categoryComboBox.getValue();

            List<Product> filteredProducts;
            if ("All Categories".equals(category)) {
                if (searchQuery.isEmpty()) {
                    filteredProducts = productService.getAllProducts();
                } else {
                    filteredProducts = productService.searchProducts(searchQuery);
                }
            } else {
                if (searchQuery.isEmpty()) {
                    filteredProducts = productService.getProductsByCategory(category);
                } else {
                    filteredProducts = productService.searchProducts(searchQuery).stream()
                            .filter(p -> p.getCategory().equals(category))
                            .toList();
                }
            }

            productsFlowPane.getChildren().clear();
            for (Product product : filteredProducts) {
                productsFlowPane.getChildren().add(createProductCard(product));
            }
        });

        productsPanel.getChildren().addAll(searchBox, new Separator(), productsFlowPane);

        ScrollPane scrollPane = new ScrollPane(productsPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        return scrollPane;
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10;");

        // Load the product image
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(product.getImageUrl()); // Load image from URL
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            // If the image fails to load, display a placeholder
            imageView.setImage(new Image("/images/default.jpg")); // Default image
        }

        // Product name
        Text nameText = new Text(product.getName());
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Product price
        Text priceText = new Text(String.format("$%.2f", product.getPrice()));
        priceText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        // Add to cart button
        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setMaxWidth(Double.MAX_VALUE);
        addToCartButton.setOnAction(e -> {
            customer.getCart().addProduct(product, 1);
            showAlert(Alert.AlertType.INFORMATION, "Product Added",
                    product.getName() + " has been added to your cart.");
        });

        // Add components to the card
        card.getChildren().addAll(imageView, nameText, priceText, addToCartButton);
        return card;
    }

    private VBox createCartPanel() {
        VBox cartPanel = new VBox(15);
        cartPanel.setPadding(new Insets(15));

        Text cartTitle = new Text("Your Shopping Cart");
        cartTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Cart items list
        ListView<CartItem> cartListView = new ListView<>();
        cartListView.setItems(FXCollections.observableArrayList(customer.getCart().getItems()));
        cartListView.setCellFactory(param -> new CartItemCell());

        // Cart summary
        GridPane cartSummary = new GridPane();
        cartSummary.setHgap(10);
        cartSummary.setVgap(10);
        cartSummary.setPadding(new Insets(15));

        Label totalItemsLabel = new Label("Total Items:");
        Label totalItemsValue = new Label(String.valueOf(customer.getCart().getTotalItems()));

        Label totalPriceLabel = new Label("Total Price:");
        Label totalPriceValue = new Label(String.format("$%.2f", customer.getCart().getTotalPrice()));

        cartSummary.add(totalItemsLabel, 0, 0);
        cartSummary.add(totalItemsValue, 1, 0);
        cartSummary.add(totalPriceLabel, 0, 1);
        cartSummary.add(totalPriceValue, 1, 1);

        // Checkout button
        Button checkoutButton = new Button("Proceed to Checkout");
        checkoutButton.setMaxWidth(Double.MAX_VALUE);

        // Add event handler
        checkoutButton.setOnAction(e -> {
            if (customer.getCart().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Cart",
                        "Your cart is empty. Please add some products before checkout.");
            } else {
                showCheckoutDialog();
            }
        });

        // Refresh button
        Button refreshButton = new Button("Refresh Cart");
        refreshButton.setMaxWidth(Double.MAX_VALUE);

        // Add event handler
        refreshButton.setOnAction(e -> {
            cartListView.setItems(FXCollections.observableArrayList(customer.getCart().getItems()));
            totalItemsValue.setText(String.valueOf(customer.getCart().getTotalItems()));
            totalPriceValue.setText(String.format("$%.2f", customer.getCart().getTotalPrice()));
        });

        cartPanel.getChildren().addAll(cartTitle, cartListView, cartSummary, checkoutButton, refreshButton);

        return cartPanel;
    }

    private VBox createOrdersPanel() {
        VBox ordersPanel = new VBox(15);
        ordersPanel.setPadding(new Insets(15));

        Text ordersTitle = new Text("Your Orders");
        ordersTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Orders table
        TableView<Order> ordersTable = new TableView<>();

        // Order ID column
        TableColumn<Order, Integer> orderIdColumn = new TableColumn<>("Order ID");
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Order Date column
        TableColumn<Order, LocalDateTime> orderDateColumn = new TableColumn<>("Date");
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        // Total Amount column
        TableColumn<Order, Double> orderTotalColumn = new TableColumn<>("Total");
        orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // Status column
        TableColumn<Order, String> orderStatusColumn = new TableColumn<>("Status");
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add columns to the table
        ordersTable.getColumns().addAll(orderIdColumn, orderDateColumn, orderTotalColumn, orderStatusColumn);

        // Load orders for the current customer
        List<Order> orders = orderService.getOrdersByCustomerId(customer.getId());
        customer.getOrderHistory().clear();
        customer.getOrderHistory().addAll(orders);

        // Populate the table with the customer's orders
        ordersTable.setItems(FXCollections.observableArrayList(customer.getOrderHistory()));

        // Refresh button
        Button refreshButton = new Button("Refresh Orders");
        refreshButton.setMaxWidth(Double.MAX_VALUE);

        // Add event handler to refresh the orders table
        refreshButton.setOnAction(e -> {
            List<Order> updatedOrders = orderService.getOrdersByCustomerId(customer.getId());
            customer.getOrderHistory().clear();
            customer.getOrderHistory().addAll(updatedOrders);
            ordersTable.setItems(FXCollections.observableArrayList(customer.getOrderHistory()));
        });

        ordersPanel.getChildren().addAll(ordersTitle, ordersTable, refreshButton);
        return ordersPanel;
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
        // Check if the cart is empty
        if (customer.getCart().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart",
                    "Your cart is empty. Please add some products before checkout.");
            return;
        }

        // Create the order
        Order order = orderService.createOrder(customer, customer.getAddress());

        if (order != null) {
            // Reduce the stock for each product in the cart
            for (CartItem item : customer.getCart().getItems()) {
                Product product = item.getProduct();
                int quantity = item.getQuantity();
                productService.reduceStock(product.getId(), quantity);
            }

            // Clear the cart after checkout
            customer.getCart().clear(); // This will also clear the cart items from the database

            showAlert(Alert.AlertType.INFORMATION, "Order Placed",
                    "Your order has been placed successfully. Order ID: " + order.getId());
        } else {
            showAlert(Alert.AlertType.ERROR, "Checkout Error",
                    "There was an error processing your order. Please try again.");
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

    // Custom cell for displaying cart items
    private class CartItemCell extends javafx.scene.control.ListCell<CartItem> {
        @Override
        protected void updateItem(CartItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox cell = new HBox(10);
                cell.setPadding(new Insets(5));

                VBox details = new VBox(5);
                Text nameText = new Text(item.getProduct().getName());
                nameText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

                Text priceText = new Text(String.format("$%.2f x %d = $%.2f",
                        item.getProduct().getPrice(), item.getQuantity(), item.getSubtotal()));

                details.getChildren().addAll(nameText, priceText);

                Button removeButton = new Button("Remove");
                removeButton.setOnAction(e -> {
                    customer.getCart().removeProduct(item.getProduct().getId());
                    getListView().getItems().remove(item);
                });

                cell.getChildren().addAll(details, removeButton);
                HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

                setGraphic(cell);
            }
        }
    }
}