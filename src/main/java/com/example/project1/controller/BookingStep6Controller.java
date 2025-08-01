package com.example.project1.controller;

import com.example.project1.model.BookingSession;
import com.example.project1.util.AlertUtil; // Assuming you created AlertUtil
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingStep6Controller {

    private static final Logger LOGGER = Logger.getLogger(BookingStep6Controller.class.getName());

    @FXML
    private Label reservationIdLabel;
    @FXML
    private Button finishButton;

    private BookingSession bookingSession; // We'll pass the session to get the reservation ID

    /**
     * Called when the controller is loaded.
     * This method is automatically called by the FXMLLoader after all @FXML annotated members have been injected.
     */
    @FXML
    public void initialize() {
        // Any setup that doesn't depend on the BookingSession goes here.
        // The reservation ID is set in setBookingSession as it depends on passed data.
    }

    /**
     * Sets the BookingSession for this controller and updates the UI based on it.
     * This method is called by the previous controller (BookingStep5Controller).
     * @param session The BookingSession object containing reservation details.
     */
    public void setBookingSession(BookingSession session) {
        this.bookingSession = session;
        LOGGER.log(Level.INFO, "Booking Session received (Step 6 - Confirmation): {0}", bookingSession);

        // Update the reservation ID label
        if (bookingSession != null && bookingSession.getReservationId() != null && !bookingSession.getReservationId().isEmpty()) {
            reservationIdLabel.setText("Reservation ID: " + bookingSession.getReservationId());
        } else {
            reservationIdLabel.setText("Reservation ID: N/A (Error)");
            LOGGER.log(Level.WARNING, "Reservation ID not found in booking session for BookingStep6.");
        }
    }

    /**
     * Handles the action when the "Finish" button is clicked.
     * Navigates back to the welcome screen.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleFinish(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Load the KioskWelcome.fxml
            URL fxmlLocation = getClass().getResource("/com/example/project1/KioskWelcome.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "KioskWelcome.fxml not found! Cannot finish.");
                LOGGER.log(Level.SEVERE, "KioskWelcome.fxml not found for finish action.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Welcome");
            stage.show();

            // Optionally, you might want to clear the current booking session
            // if it's no longer needed after booking is complete.
            // this.bookingSession = null;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the welcome page after booking completion.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load welcome page: " + e.getMessage());
        }
    }
}