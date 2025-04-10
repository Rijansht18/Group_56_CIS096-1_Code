package com.ecommerce.ui;

import com.ecommerce.model.User;
import com.ecommerce.service.SessionManager;
import com.ecommerce.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginScreen {
    private BorderPane root;
    private Stage primaryStage;
    private UserService userService;
    private SessionManager sessionManager;
    private TextField usernameField;
    private PasswordField passwordField;

    public LoginScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.userService = UserService.getInstance();
        this.sessionManager = SessionManager.getInstance();
        createUI();
    }

    private void createUI() {
        root = new BorderPane();
        root.setPadding(new Insets(20));
        primaryStage.setMaximized(true);

        // Create the header
        Text headerText = new Text("E-Commerce Application");
        headerText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        HBox header = new HBox(headerText);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));

        // Create the login form
        GridPane loginForm = new GridPane();
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setHgap(10);
        loginForm.setVgap(10);
        loginForm.setPadding(new Insets(20));

        Text loginTitle = new Text("Login");
        loginTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        loginForm.add(loginTitle, 0, 0, 2, 1);

        Label usernameLabel = new Label("Username:");
        loginForm.add(usernameLabel, 0, 1);

        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        loginForm.add(usernameField, 1, 1);

        Label passwordLabel = new Label("Password:");
        loginForm.add(passwordLabel, 0, 2);

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        loginForm.add(passwordField, 1, 2);

        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true); // Makes Enter key trigger this button
        HBox loginButtonBox = new HBox(10, loginButton);
        loginButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
        loginForm.add(loginButtonBox, 1, 4);

        Button registerButton = new Button("Register");
        HBox registerButtonBox = new HBox(10, registerButton);
        registerButtonBox.setAlignment(Pos.BOTTOM_LEFT);
        loginForm.add(registerButtonBox, 0, 4);

        // Single event handler for login button
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));
        registerButton.setOnAction(e -> showRegistrationScreen());

        // Add components to the root pane
        root.setTop(header);
        root.setCenter(loginForm);

        // Add a footer
        Text footerText = new Text("© 2025 E-Commerce Application");
        HBox footer = new HBox(footerText);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20));
        root.setBottom(footer);
    }

    private void handleLogin(String username, String password) {
        // Clear previous error styles
        usernameField.setStyle("");
        passwordField.setStyle("");

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please enter both username and password.");
            if (username.isEmpty()) {
                usernameField.setStyle("-fx-border-color: red;");
            }
            if (password.isEmpty()) {
                passwordField.setStyle("-fx-border-color: red;");
            }
            return;
        }

        Optional<User> userOpt = userService.authenticate(username, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            sessionManager.login(user);

            if (sessionManager.isAdmin()) {
                showAdminDashboard();
            } else {
                showCustomerDashboard();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid username or password.");
            usernameField.setStyle("-fx-border-color: red;");
            passwordField.setStyle("-fx-border-color: red;");
        }
    }

    private void showRegistrationScreen() {
        RegistrationScreen registrationScreen = new RegistrationScreen(primaryStage);
        Scene scene = new Scene(registrationScreen.getRoot(), 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showAdminDashboard() {
        AdminDashboard adminDashboard = new AdminDashboard(primaryStage);
        Scene scene = new Scene(adminDashboard.getRoot(), 1024, 768);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showCustomerDashboard() {
        CustomerDashboard customerDashboard = new CustomerDashboard(primaryStage);
        Scene scene = new Scene(customerDashboard.getRoot(), 1024, 768);
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