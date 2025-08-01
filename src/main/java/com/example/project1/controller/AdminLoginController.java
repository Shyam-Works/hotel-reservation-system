package com.example.project1.controller;

import com.example.project1.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminLoginController {

    private static final Logger LOGGER = Logger.getLogger(AdminLoginController.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password123";

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password cannot be empty.");
            return;
        }

        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            LOGGER.log(Level.INFO, "Admin login successful for user: {0}", username);
            errorLabel.setText("");
            navigateToAdminDashboard(event); // Navigate to the dashboard
        } else {
            LOGGER.log(Level.WARNING, "Admin login failed for user: {0}", username);
            errorLabel.setText("Invalid username or password.");
        }
    }

    private void navigateToAdminDashboard(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // --- This is the change: Load the AdminDashboard.fxml ---
            URL fxmlLocation = getClass().getResource("/com/example/project1/AdminDashboard.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "AdminDashboard.fxml not found!");
                LOGGER.log(Level.SEVERE, "AdminDashboard.fxml not found after successful login.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load Admin Dashboard page.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load Admin Dashboard: " + e.getMessage());
        }
    }

    // You might want to add a "Back" button to your AdminLogin.fxml and link it to this method
    @FXML
    private void handleBackToWelcome(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/KioskWelcome.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "KioskWelcome.fxml not found!");
                LOGGER.log(Level.SEVERE, "KioskWelcome.fxml not found for back navigation from Admin Login.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Welcome");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load welcome page.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load welcome page: " + e.getMessage());
        }
    }
}