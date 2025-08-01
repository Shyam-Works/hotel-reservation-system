package com.example.project1.controller;

import com.example.project1.dao.FeedbackDao; // Make sure this path is correct
import com.example.project1.util.AlertUtil; // Assuming you have an AlertUtil class
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox; // Using ChoiceBox as per your FXML
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KioskFeedbackController {

    private static final Logger LOGGER = Logger.getLogger(KioskFeedbackController.class.getName());

    @FXML
    private TextField reservationIdField; // This is actually your phone number field in FXML
    @FXML
    private TextArea commentsArea;
    @FXML
    private ChoiceBox<String> ratingChoiceBox; // Type is String as per your FXML

    private FeedbackDao feedbackDao;

    @FXML
    public void initialize() {
        feedbackDao = new FeedbackDao(); // Initialize the DAO when the controller loads

        // The ChoiceBox items are already defined in FXML, so you don't need to add them here.
        // You can, however, set a default value if you wish:
        // ratingChoiceBox.getSelectionModel().select("5"); // Selects '5' as default
    }

    @FXML
    private void handleSubmitFeedback(ActionEvent event) {
        String phoneNumber = reservationIdField.getText().trim(); // Get phone number
        String ratingString = ratingChoiceBox.getValue(); // Get selected rating as String
        String comment = commentsArea.getText().trim();

        if (phoneNumber.isEmpty()) {
            AlertUtil.showErrorAlert("Input Error", "Phone Number is required.");
            return;
        }
        // Basic phone number validation (can be enhanced)
        if (!phoneNumber.matches("\\d+")) { // Only digits allowed for simplicity
            AlertUtil.showErrorAlert("Input Error", "Please enter a valid phone number (digits only).");
            return;
        }

        if (ratingString == null || ratingString.isEmpty()) {
            AlertUtil.showErrorAlert("Input Error", "Please select a rating from 1 to 5.");
            return;
        }

        int rating = Integer.parseInt(ratingString); // Convert rating to int

        // Attempt to save feedback
        if (feedbackDao.insertFeedback(phoneNumber, rating, comment)) {
            AlertUtil.showInformationAlert("Feedback Submitted", "Thank you for your valuable feedback!");
            navigateToWelcome(event); // Go back to welcome screen on success
        } else {
            AlertUtil.showErrorAlert("Database Error", "Failed to submit feedback. Please try again.");
            LOGGER.log(Level.SEVERE, "Failed to insert feedback for phone: {0}", phoneNumber);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateToWelcome(event); // Go back to welcome screen
    }

    /**
     * Helper method to navigate back to the KioskWelcome.fxml.
     */
    private void navigateToWelcome(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/KioskWelcome.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "KioskWelcome.fxml not found! Check the path.");
                LOGGER.log(Level.SEVERE, "KioskWelcome.fxml not found during feedback navigation.");
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