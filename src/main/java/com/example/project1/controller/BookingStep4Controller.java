package com.example.project1.controller;

import com.example.project1.model.BookingSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingStep4Controller {

    private static final Logger LOGGER = Logger.getLogger(BookingStep4Controller.class.getName());

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField ageField;
    @FXML private TextField streetNameField;
    @FXML private TextField aptSuiteField;
    @FXML private TextField cityField;
    @FXML private TextField provinceStateField;
    @FXML private TextField countryField;

    private BookingSession bookingSession;

    @FXML
    public void initialize() {
        genderComboBox.getItems().addAll("Male", "Female", "Other", "Prefer not to say");
    }

    public void setBookingSession(BookingSession session) {
        this.bookingSession = session;
        LOGGER.log(Level.INFO, "Booking Session received (Step 4): {0}", bookingSession);

        // Populate fields if navigating back to this page
        if (bookingSession != null) {
            // Check each field individually to avoid null pointer exceptions
            if (bookingSession.getGuestFirstName() != null && !bookingSession.getGuestFirstName().trim().isEmpty()) {
                firstNameField.setText(bookingSession.getGuestFirstName());
            }
            if (bookingSession.getGuestLastName() != null && !bookingSession.getGuestLastName().trim().isEmpty()) {
                lastNameField.setText(bookingSession.getGuestLastName());
            }
            if (bookingSession.getGuestGender() != null && !bookingSession.getGuestGender().trim().isEmpty()) {
                genderComboBox.setValue(bookingSession.getGuestGender());
            }
            if (bookingSession.getGuestPhone() != null && !bookingSession.getGuestPhone().trim().isEmpty()) {
                phoneField.setText(bookingSession.getGuestPhone());
            }
            if (bookingSession.getGuestEmail() != null && !bookingSession.getGuestEmail().trim().isEmpty()) {
                emailField.setText(bookingSession.getGuestEmail());
            }
            if (bookingSession.getGuestAge() > 0) {
                ageField.setText(String.valueOf(bookingSession.getGuestAge()));
            }
            if (bookingSession.getGuestStreet() != null && !bookingSession.getGuestStreet().trim().isEmpty()) {
                streetNameField.setText(bookingSession.getGuestStreet());
            }
            if (bookingSession.getGuestAptSuite() != null && !bookingSession.getGuestAptSuite().trim().isEmpty()) {
                aptSuiteField.setText(bookingSession.getGuestAptSuite());
            }
            if (bookingSession.getGuestCity() != null && !bookingSession.getGuestCity().trim().isEmpty()) {
                cityField.setText(bookingSession.getGuestCity());
            }
            if (bookingSession.getGuestProvinceState() != null && !bookingSession.getGuestProvinceState().trim().isEmpty()) {
                provinceStateField.setText(bookingSession.getGuestProvinceState());
            }
            if (bookingSession.getGuestCountry() != null && !bookingSession.getGuestCountry().trim().isEmpty()) {
                countryField.setText(bookingSession.getGuestCountry());
            }
        }
    }

    @FXML
    private void handleReviewBooking(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        // Ensure booking session exists
        if (this.bookingSession == null) {
            this.bookingSession = new BookingSession();
            LOGGER.log(Level.WARNING, "Booking Session was null in Step 4, initializing new one.");
        }

        // Save data to session
        bookingSession.setGuestFirstName(firstNameField.getText().trim());
        bookingSession.setGuestLastName(lastNameField.getText().trim());
        bookingSession.setGuestGender(genderComboBox.getValue()); // Can be null if not selected
        bookingSession.setGuestPhone(phoneField.getText().trim());
        bookingSession.setGuestEmail(emailField.getText().trim());

        // Handle age field
        String ageText = ageField.getText().trim();
        if (!ageText.isEmpty()) {
            try {
                bookingSession.setGuestAge(Integer.parseInt(ageText));
            } catch (NumberFormatException e) {
                bookingSession.setGuestAge(0); // Set to default if invalid
                LOGGER.log(Level.WARNING, "Invalid age format: {0}", ageText);
            }
        } else {
            bookingSession.setGuestAge(0); // Default for empty age
        }

        bookingSession.setGuestStreet(streetNameField.getText().trim());
        bookingSession.setGuestAptSuite(aptSuiteField.getText().trim());
        bookingSession.setGuestCity(cityField.getText().trim());
        bookingSession.setGuestProvinceState(provinceStateField.getText().trim());
        bookingSession.setGuestCountry(countryField.getText().trim());

        LOGGER.log(Level.INFO, "Guest details saved in session: {0}", bookingSession);

        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep5.fxml");
            if (fxmlLocation == null) {
                showAlert("Navigation Error", "BookingStep5.fxml not found! Check the path.");
                LOGGER.log(Level.SEVERE, "BookingStep5.fxml not found.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            BookingStep5Controller nextController = fxmlLoader.getController();
            nextController.setBookingSession(bookingSession);

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Review Booking");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the review booking page.", e);
            showAlert("Navigation Error", "Could not load the review booking page: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep3.fxml"); // Go back to room selection
            if (fxmlLocation == null) {
                showAlert("Navigation Error", "BookingStep3.fxml not found! Cannot go back.");
                LOGGER.log(Level.SEVERE, "BookingStep3.fxml not found for back navigation.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            BookingStep3Controller previousController = fxmlLoader.getController();
            previousController.setBookingSession(bookingSession); // Pass session back

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Choose Rooms");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the previous booking page.", e);
            showAlert("Navigation Error", "Could not load the previous booking page: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        // Check required fields
        if (firstNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "First Name is required.");
            return false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Last Name is required.");
            return false;
        }
        if (phoneField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Phone Number is required.");
            return false;
        }
        if (emailField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Email Address is required.");
            return false;
        }
        if (streetNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Street Name is required.");
            return false;
        }
        if (cityField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "City is required.");
            return false;
        }
        if (provinceStateField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Province/State is required.");
            return false;
        }
        if (countryField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Country is required.");
            return false;
        }

        // Basic email validation
        String email = emailField.getText().trim();
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showAlert("Validation Error", "Please enter a valid email address.");
            return false;
        }

        // Phone number validation - more flexible pattern
        String phone = phoneField.getText().trim();
        if (!phone.matches("^[0-9\\s\\-\\(\\)\\+\\.]+$")) {
            showAlert("Validation Error", "Please enter a valid phone number (numbers, spaces, hyphens, parentheses, and + allowed).");
            return false;
        }

        // Age validation (only if provided)
        String ageText = ageField.getText().trim();
        if (!ageText.isEmpty()) {
            try {
                int age = Integer.parseInt(ageText);
                if (age <= 0 || age > 120) {
                    showAlert("Validation Error", "Please enter a realistic age (1-120).");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Validation Error", "Please enter a valid number for age.");
                return false;
            }
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}