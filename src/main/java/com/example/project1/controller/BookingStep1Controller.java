package com.example.project1.controller;

import com.example.project1.model.BookingSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.*;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingStep1Controller {

    private static final Logger LOGGER = Logger.getLogger(BookingStep1Controller.class.getName());

    @FXML
    private TextField numberOfGuestsField;

    private BookingSession bookingSession;

    public void setBookingSession(BookingSession session) {
        this.bookingSession = session;
        if (this.bookingSession != null && this.bookingSession.getNumberOfGuests() > 0) {
            numberOfGuestsField.setText(String.valueOf(this.bookingSession.getNumberOfGuests()));
        }
        LOGGER.log(Level.INFO, "Booking Session received (Step 1): {0}", bookingSession);
    }

    @FXML
    private void handleNextStep(ActionEvent event) {
        try {
            int numberOfGuests = Integer.parseInt(numberOfGuestsField.getText().trim());

            if (numberOfGuests <= 0) {
                showAlert("Input Error", "Number of guests must be a positive number.");
                return;
            }

            if (this.bookingSession == null) {
                this.bookingSession = new BookingSession();
                LOGGER.log(Level.INFO, "New BookingSession initialized in Step 1.");
            }
            this.bookingSession.setNumberOfGuests(numberOfGuests);
            LOGGER.log(Level.INFO, "Booking Session (Step 1 Updated): {0}", bookingSession);

            // Load the next FXML page (BookingStep2.fxml for date selection)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep2.fxml");
            if (fxmlLocation == null) {
                showAlert("Navigation Error", "BookingStep2.fxml not found! Check the path.");
                LOGGER.log(Level.SEVERE, "BookingStep2.fxml not found for navigation from Step 1.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            // Pass the BookingSession to the next controller
            BookingStep2Controller nextController = fxmlLoader.getController();
            nextController.setBookingSession(bookingSession);

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Booking Step 2: Select Dates");
            stage.show();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid number for the number of guests.");
            LOGGER.log(Level.WARNING, "Invalid number format for guests: {0}", e.getMessage());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the next booking page: {0}", e.getMessage());
            showAlert("Navigation Error", "Could not load the next booking page: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Corrected: Navigate back to KioskWelcome.fxml
            URL fxmlLocation = getClass().getResource("/com/example/project1/KioskWelcome.fxml");
            if (fxmlLocation == null) {
                showAlert("Navigation Error", "KioskWelcome.fxml not found! Cannot go back.");
                LOGGER.log(Level.SEVERE, "KioskWelcome.fxml not found for back navigation from Step 1.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            // KioskWelcomeController might not need the booking session, depending on your design.
            // For simplicity, we won't pass it back to the welcome screen for now.

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Welcome");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the welcome page.", e);
            showAlert("Navigation Error", "Could not load the welcome page: " + e.getMessage());
        }
    }


    @FXML
    private void handleLeaveFeedback(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Load your KioskFeedback.fxml here. Ensure the path is correct.
            URL fxmlLocation = getClass().getResource("/com/example/project1/KioskFeedback.fxml");
            if (fxmlLocation == null) {
                showAlert("Navigation Error", "KioskFeedback.fxml not found! Check the path.");
                LOGGER.log(Level.SEVERE, "KioskFeedback.fxml not found for navigation from Welcome.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Leave Feedback");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load feedback page.", e);
            showAlert("Navigation Error", "Could not load feedback page: " + e.getMessage());
        }
    }

    @FXML private TextField guestNameField;
    @FXML private TextField roomField;
    @FXML private DatePicker checkInDatePicker;
    @FXML private ChoiceBox<String> statusChoiceBox;

    public void prefillForm(String guestName, String room, String checkInDate, String status) {
        guestNameField.setText(guestName);
        roomField.setText(room);

        // Parse the check-in date string to LocalDate
        try {
            LocalDate date = LocalDate.parse(checkInDate); // Assuming format is "yyyy-MM-dd"
            checkInDatePicker.setValue(date);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format: " + checkInDate);
        }

        statusChoiceBox.setValue(status);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


