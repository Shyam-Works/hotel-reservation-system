package com.example.project1.controller;

import com.example.project1.model.BookingSession;
import com.example.project1.util.AlertUtil;
import com.example.project1.dao.BookingDao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingStep5Controller {

    private static final Logger LOGGER = Logger.getLogger(BookingStep5Controller.class.getName());

    @FXML private Label guestNameLabel;
    @FXML private Label datesLabel;
    @FXML private Label roomTypeLabel;
    @FXML private Label numberOfGuestsLabel;
    @FXML private Label priceLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;

    private BookingSession bookingSession;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void setBookingSession(BookingSession session) {
        this.bookingSession = session;
        LOGGER.log(Level.INFO, "Booking Session received (Step 5 - Review): {0}", bookingSession);
        updateLabels();
    }

    private void updateLabels() {
        if (bookingSession == null) {
            LOGGER.log(Level.WARNING, "BookingSession is null in BookingStep5Controller. Cannot update labels.");
            return;
        }

        String fullName = "";
        if (bookingSession.getGuestFirstName() != null) {
            fullName += bookingSession.getGuestFirstName();
        }
        if (bookingSession.getGuestLastName() != null) {
            if (!fullName.isEmpty()) fullName += " ";
            fullName += bookingSession.getGuestLastName();
        }
        guestNameLabel.setText("Guest: " + (fullName.isEmpty() ? "N/A" : fullName));

        String datesText = "Check-in: N/A   Check-out: N/A";
        long nights = 0;
        if (bookingSession.getCheckInDate() != null && bookingSession.getCheckOutDate() != null) {
            datesText = String.format("Check-in: %s   Check-out: %s",
                    bookingSession.getCheckInDate().format(DATE_FORMATTER),
                    bookingSession.getCheckOutDate().format(DATE_FORMATTER));
            nights = ChronoUnit.DAYS.between(bookingSession.getCheckInDate(), bookingSession.getCheckOutDate());
            if (nights > 0) {
                datesText += String.format(" (%d Night%s)", nights, (nights == 1 ? "" : "s"));
            }
        }
        datesLabel.setText(datesText);

        StringBuilder roomsSummary = new StringBuilder();
        if (bookingSession.getSelectedRoomsAndQuantities() != null && !bookingSession.getSelectedRoomsAndQuantities().isEmpty()) {
            for (Map.Entry<String, Integer> entry : bookingSession.getSelectedRoomsAndQuantities().entrySet()) {
                if (roomsSummary.length() > 0) {
                    roomsSummary.append(", ");
                }
                roomsSummary.append(String.format("%s(%d)", entry.getKey(), entry.getValue()));
            }
        }
        roomTypeLabel.setText("Room Types: " + (roomsSummary.length() > 0 ? roomsSummary.toString() : "N/A"));

        numberOfGuestsLabel.setText("Number of Guests: " + bookingSession.getNumberOfGuests());

        double calculatedPrice = bookingSession.getTotalPrice();
        double taxes = calculatedPrice * 0.13;
        double total = calculatedPrice + taxes;

        priceLabel.setText(String.format("Room Price (%d night%s):   $%.2f", nights, (nights == 1 ? "" : "s"), calculatedPrice));
        taxLabel.setText(String.format("Taxes:                             $%.2f", taxes));
        totalLabel.setText(String.format("Estimated Total:         $%.2f", total));
    }


    @FXML
    private void handleConfirmBooking(ActionEvent event) {
        String generatedReservationId = "ABC-" + System.currentTimeMillis() % 100000;

        if (bookingSession != null) {
            bookingSession.setReservationId(generatedReservationId);

            BookingDao bookingDao = new BookingDao();

            // --- FIX: The insertBooking method now returns an int, not a boolean. ---
            int bookingId = bookingDao.insertBooking(bookingSession);

            // We check if the returned ID is not -1, which indicates success.
            if (bookingId != -1) {
                LOGGER.log(Level.INFO, "Booking saved to database successfully with ID: {0}", generatedReservationId);
                // If saving is successful, proceed to the next screen
            } else {
                AlertUtil.showErrorAlert("Database Error", "Failed to save booking to database. Please try again.");
                LOGGER.log(Level.SEVERE, "Failed to save booking with ID: {0} to database.", generatedReservationId);
                return;
            }
        } else {
            AlertUtil.showErrorAlert("Booking Error", "Booking session is null. Cannot confirm booking.");
            LOGGER.log(Level.SEVERE, "Booking session is null during confirmation.");
            return;
        }

        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep6.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "BookingStep6.fxml not found!");
                LOGGER.log(Level.SEVERE, "BookingStep6.fxml not found during confirmation.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            BookingStep6Controller nextController = fxmlLoader.getController();
            nextController.setBookingSession(bookingSession);

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Booking Successful!");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the booking successful page.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load confirmation page: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep4.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "BookingStep4.fxml not found! Cannot go back.");
                LOGGER.log(Level.SEVERE, "BookingStep4.fxml not found for back navigation.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            BookingStep4Controller previousController = fxmlLoader.getController();
            previousController.setBookingSession(bookingSession);

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Enter Your Details");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the previous booking page.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load the previous booking page: " + e.getMessage());
        }
    }
}