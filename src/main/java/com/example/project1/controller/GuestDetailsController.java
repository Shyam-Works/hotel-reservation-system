package com.example.project1.controller;

import com.example.project1.model.BookingSession; // To receive booking data
import com.example.project1.util.AlertUtil; // For alerts
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuestDetailsController {

    private static final Logger LOGGER = Logger.getLogger(GuestDetailsController.class.getName());

    // Guest Information Labels
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label genderLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label ageLabel;
    @FXML private Label streetNameLabel;
    @FXML private Label aptSuiteLabel;
    @FXML private Label cityLabel;
    @FXML private Label provinceStateLabel;
    @FXML private Label countryLabel;

    // Reservation Information Labels
    @FXML private Label reservationIdLabel;
    @FXML private Label checkInDateLabel;
    @FXML private Label checkOutDateLabel;
    @FXML private Label numGuestsLabel;
    @FXML private Label statusLabel; // Assuming a status for the booking

    // Room Information Labels
    @FXML private Label roomNumberLabel; // Note: Your current booking flow doesn't assign specific room numbers
    @FXML private Label roomTypeLabel;   // This will display the selected_rooms_summary

    private BookingSession currentBookingSession;

    /**
     * Called by the previous controller (AdminGuestSearchController) to pass the booking details.
     * @param bookingSession The BookingSession object containing all details.
     */
    public void setBookingDetails(BookingSession bookingSession) {
        if (bookingSession == null) {
            LOGGER.log(Level.WARNING, "Attempted to set null BookingSession in GuestDetailsController.");
            AlertUtil.showErrorAlert("Error", "No booking details available.");
            return;
        }
        this.currentBookingSession = bookingSession;
        populateLabels();
        LOGGER.log(Level.INFO, "Guest details set for Reservation ID: {0}", bookingSession.getReservationId());
    }

    /**
     * Populates the FXML labels with data from the currentBookingSession.
     */
    private void populateLabels() {
        // Guest Information
        firstNameLabel.setText(currentBookingSession.getGuestFirstName());
        lastNameLabel.setText(currentBookingSession.getGuestLastName());
        genderLabel.setText(currentBookingSession.getGuestGender());
        phoneLabel.setText(currentBookingSession.getGuestPhone());
        emailLabel.setText(currentBookingSession.getGuestEmail());
        ageLabel.setText(String.valueOf(currentBookingSession.getGuestAge()));
        streetNameLabel.setText(currentBookingSession.getGuestStreet());
        aptSuiteLabel.setText(currentBookingSession.getGuestAptSuite());
        cityLabel.setText(currentBookingSession.getGuestCity());
        provinceStateLabel.setText(currentBookingSession.getGuestProvinceState());
        countryLabel.setText(currentBookingSession.getGuestCountry());

        // Reservation Information
        reservationIdLabel.setText(currentBookingSession.getReservationId());
        // Ensure check-in/out dates are formatted nicely if needed, otherwise direct toString()
        checkInDateLabel.setText(currentBookingSession.getCheckInDate() != null ? currentBookingSession.getCheckInDate().toString() : "N/A");
        checkOutDateLabel.setText(currentBookingSession.getCheckOutDate() != null ? currentBookingSession.getCheckOutDate().toString() : "N/A");
        numGuestsLabel.setText(String.valueOf(currentBookingSession.getNumberOfGuests()));
        // Assuming status is "Confirmed" for now, or if you add to DB, retrieve it.
        statusLabel.setText(currentBookingSession.getStatus());

        // Room Information
        // Note: Your current booking system does not assign specific room numbers.
        // This label will remain empty or display a placeholder unless you implement room assignment.
        roomNumberLabel.setText("N/A (Not assigned)"); // Placeholder
        roomTypeLabel.setText(currentBookingSession.getSelectedRoomsSummary()); // Displays e.g., "Deluxe (x1), Standard (x2)"
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/AdminGuestSearch.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "AdminGuestSearch.fxml not found! Cannot go back.");
                LOGGER.log(Level.SEVERE, "AdminGuestSearch.fxml not found for back navigation from Guest Details.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            // No data to pass back to the search page for now, as it reloads its own state
            // AdminGuestSearchController searchController = fxmlLoader.getController();
            // If you want to retain search results, you'd pass them back.

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Guest Search");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load Guest Search page from Guest Details.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load Guest Search page: " + e.getMessage());
        }
    }
}