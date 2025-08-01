package com.example.project1.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType; // To specify the type of alert
public class KioskWelcomeController {

    private static final Logger LOGGER = Logger.getLogger(KioskWelcomeController.class.getName());

    @FXML
    private Label videoPlaceholderLabel; // Still present for the text placeholder
    @FXML
    private Button startBookingButton;
    @FXML
    private Button rulesButton;
    @FXML
    private Button feedbackButton;
    @FXML
    private Button adminLoginButton; // Ensure this fx:id is added to KioskWelcome.fxml

    @FXML
    public void initialize() {
        videoPlaceholderLabel.setVisible(true);
    }

    @FXML
    private void handleStartBooking(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Load BookingStep1.fxml from the correct resource path
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep1.fxml");
            if (fxmlLocation == null) {
                LOGGER.log(Level.SEVERE, "BookingStep1.fxml not found! Check the path: " + "/com/example/project1/BookingStep1.fxml");
                showAlert("Navigation Error", "BookingStep1.fxml not found! Cannot proceed.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Booking Step 1");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load BookingStep1.fxml.", e);
            showAlert("Navigation Error", "Could not load the booking start page: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowRules(ActionEvent event) {
        LOGGER.log(Level.INFO, "Displaying Rules & Regulations...");
        showAlert("Rules & Regulations", "Placeholder for Rules & Regulations content.");
        // TODO: Implement logic to open a new window or scene for Rules & Regulations
    }

    @FXML
    private void handleLeaveFeedback(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Load KioskFeedback.fxml from the correct resource path
            URL fxmlLocation = getClass().getResource("/com/example/project1/KioskFeedback.fxml");
            if (fxmlLocation == null) {
                LOGGER.log(Level.SEVERE, "KioskFeedback.fxml not found! Check the path: " + "/com/example/project1/KioskFeedback.fxml");
                showAlert("Navigation Error", "KioskFeedback.fxml not found! Cannot proceed.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Leave Feedback");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load KioskFeedback.fxml.", e);
            showAlert("Navigation Error", "Could not load the feedback page: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdminLogin(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Load AdminLogin.fxml from the correct resource path
            URL fxmlLocation = getClass().getResource("/com/example/project1/AdminLogin.fxml");
            if (fxmlLocation == null) {
                LOGGER.log(Level.SEVERE, "AdminLogin.fxml not found! Check the path: " + "/com/example/project1/AdminLogin.fxml");
                showAlert("Navigation Error", "AdminLogin.fxml not found! Cannot proceed.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Admin Login");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load AdminLogin.fxml.", e);
            showAlert("Navigation Error", "Could not load the admin login page: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}