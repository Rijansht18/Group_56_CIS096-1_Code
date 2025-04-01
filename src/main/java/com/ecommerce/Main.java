package com.ecommerce;

import com.ecommerce.backend.DatabaseManager;
import com.ecommerce.ui.LoginScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize the database using the singleton instance
        DatabaseManager databaseManager = DatabaseManager.getInstance();

        // Set up the primary stage
        primaryStage.setTitle("E-Commerce Application");

        // Create the login screen
        LoginScreen loginScreen = new LoginScreen(primaryStage);
        Scene scene = new Scene(loginScreen.getRoot(), 800, 600);

        // Add CSS styling
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

        // Set the scene and show the stage
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();

        // Close the database connection when the application exits
        primaryStage.setOnCloseRequest(event -> databaseManager.closeConnection());
    }

    public static void main(String[] args) {
        launch(args);
    }
}