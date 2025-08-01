package com.example.project1.controller;

import com.example.project1.model.BookingSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingStep2Controller {

    private static final Logger LOGGER = Logger.getLogger(BookingStep2Controller.class.getName());

    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;

    private BookingSession bookingSession;

    @FXML
    public void initialize() {
        // Set date restrictions for check-in date picker - disable past dates
        checkInDatePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.isBefore(today));
            }
        });

        // Initially, check-out date picker allows dates from today onwards
        updateCheckOutDatePicker();

        // Add listener to update check-out picker when check-in date changes
        checkInDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                // If check-out date is before or same as new check-in date, clear it
                LocalDate currentCheckOut = checkOutDatePicker.getValue();
                if (currentCheckOut != null && !currentCheckOut.isAfter(newDate)) {
                    checkOutDatePicker.setValue(null);
                }
                // Update the check-out date picker restrictions
                updateCheckOutDatePicker();
            }
        });
    }

    private void updateCheckOutDatePicker() {
        checkOutDatePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                LocalDate checkInDate = checkInDatePicker.getValue();

                if (checkInDate == null) {
                    // If no check-in date is selected, disable all dates before today
                    setDisable(empty || date.isBefore(today));
                } else {
                    // Disable dates before or on the check-in date
                    setDisable(empty || !date.isAfter(checkInDate));
                }
            }
        });
    }

    public void setBookingSession(BookingSession session) {
        this.bookingSession = session;
        LOGGER.log(Level.INFO, "Booking Session received (Step 2): {0}", bookingSession);

        // Populate fields if navigating back to this page
        if (bookingSession != null) {
            if (bookingSession.getCheckInDate() != null) {
                checkInDatePicker.setValue(bookingSession.getCheckInDate());
            }
            if (bookingSession.getCheckOutDate() != null) {
                checkOutDatePicker.setValue(bookingSession.getCheckOutDate());
            }
        }
    }

    @FXML
    private void handleNextStep(ActionEvent event) {
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        // Validate date selections
        if (checkIn == null) {
            showAlert("Input Error", "Please select a check-in date.");
            return;
        }
        if (checkOut == null) {
            showAlert("Input Error", "Please select a check-out date.");
            return;
        }
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            showAlert("Input Error", "Check-out date must be after check-in date.");
            return;
        }

        long numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (numberOfNights <= 0) {
            showAlert("Input Error", "The booking must be for at least one night.");
            return;
        }

        // Save data to session
        if (this.bookingSession == null) {
            this.bookingSession = new BookingSession();
            LOGGER.log(Level.WARNING, "Booking Session was null in Step 2, initializing new one.");
        }
        this.bookingSession.setCheckInDate(checkIn);
        this.bookingSession.setCheckOutDate(checkOut);
        LOGGER.log(Level.INFO, "Booking Session (Step 2 Updated): {0}", bookingSession);

        try {
            // Load the next FXML page (BookingStep3.fxml for room selection)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep3.fxml");
            if (fxmlLocation == null) {
                showAlert("Navigation Error", "BookingStep3.fxml not found! Check the path.");
                LOGGER.log(Level.SEVERE, "BookingStep3.fxml not found.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            // Pass the BookingSession to the next controller
            BookingStep3Controller nextController = fxmlLoader.getController();
            nextController.setBookingSession(bookingSession);

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Booking Step 3: Choose Rooms");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the room selection page.", e);
            showAlert("Navigation Error", "Could not load the room selection page: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep1.fxml");
            if (fxmlLocation == null) {
                showAlert("Navigation Error", "BookingStep1.fxml not found! Cannot go back.");
                LOGGER.log(Level.SEVERE, "BookingStep1.fxml not found for back navigation.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            BookingStep1Controller previousController = fxmlLoader.getController();
            previousController.setBookingSession(bookingSession);

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Booking Step 1");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the previous booking page.", e);
            showAlert("Navigation Error", "Could not load the previous booking page: " + e.getMessage());
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

