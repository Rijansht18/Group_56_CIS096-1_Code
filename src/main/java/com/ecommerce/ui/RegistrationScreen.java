package com.ecommerce.ui;

import com.ecommerce.model.Customer;
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

public class RegistrationScreen {
    private BorderPane root;
    private Stage primaryStage;
    private UserService userService;

    public RegistrationScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.userService = UserService.getInstance();
        createUI();
    }

    private void createUI() {
        root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Create the header
        Text headerText = new Text("E-Commerce Application");
        headerText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        HBox header = new HBox(headerText);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        
        // Create the registration form
        GridPane registrationForm = new GridPane();
        registrationForm.setAlignment(Pos.CENTER);
        registrationForm.setHgap(10);
        registrationForm.setVgap(10);
        registrationForm.setPadding(new Insets(20));
        
        Text registrationTitle = new Text("Customer Registration");
        registrationTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        registrationForm.add(registrationTitle, 0, 0, 2, 1);
        
        // Username field
        Label usernameLabel = new Label("Username:");
        registrationForm.add(usernameLabel, 0, 1);
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        registrationForm.add(usernameField, 1, 1);
        
        // Password field
        Label passwordLabel = new Label("Password:");
        registrationForm.add(passwordLabel, 0, 2);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");
        registrationForm.add(passwordField, 1, 2);
        
        // Confirm password field
        Label confirmPasswordLabel = new Label("Confirm Password:");
        registrationForm.add(confirmPasswordLabel, 0, 3);
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        registrationForm.add(confirmPasswordField, 1, 3);
        
        // Email field
        Label emailLabel = new Label("Email:");
        registrationForm.add(emailLabel, 0, 4);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        registrationForm.add(emailField, 1, 4);
        
        // Full name field
        Label fullNameLabel = new Label("Full Name:");
        registrationForm.add(fullNameLabel, 0, 5);
        
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Enter your full name");
        registrationForm.add(fullNameField, 1, 5);
        
        // Address field
        Label addressLabel = new Label("Address:");
        registrationForm.add(addressLabel, 0, 6);
        
        TextField addressField = new TextField();
        addressField.setPromptText("Enter your address");
        registrationForm.add(addressField, 1, 6);
        
        // Phone number field
        Label phoneLabel = new Label("Phone Number:");
        registrationForm.add(phoneLabel, 0, 7);
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter your phone number");
        registrationForm.add(phoneField, 1, 7);
        
        // Buttons
        Button registerButton = new Button("Register");
        Button backButton = new Button("Back to Login");
        
        HBox buttonBox = new HBox(10, backButton, registerButton);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        registrationForm.add(buttonBox, 1, 9);
        
        // Add event handlers
        registerButton.setOnAction(e -> handleRegistration(
                usernameField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText(),
                emailField.getText(),
                fullNameField.getText(),
                addressField.getText(),
                phoneField.getText()
        ));
        
        backButton.setOnAction(e -> showLoginScreen());
        
        // Add components to the root pane
        root.setTop(header);
        root.setCenter(registrationForm);
        
        // Add a footer
        Text footerText = new Text("© 2023 E-Commerce Application");
        HBox footer = new HBox(footerText);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20));
        root.setBottom(footer);
    }

    private void handleRegistration(String username, String password, String confirmPassword,
                                   String email, String fullName, String address, String phoneNumber) {
        // Validate input
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                email.isEmpty() || fullName.isEmpty() || address.isEmpty() || phoneNumber.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill in all fields.");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Passwords do not match.");
            return;
        }
        
        // Register the customer
        Customer customer = userService.registerCustomer(username, password, email, fullName, address, phoneNumber);
        
        if (customer != null) {
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", 
                    "You have successfully registered. Please login with your credentials.");
            showLoginScreen();
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Error", 
                    "Username already exists. Please choose a different username.");
        }
    }

    private void showLoginScreen() {
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