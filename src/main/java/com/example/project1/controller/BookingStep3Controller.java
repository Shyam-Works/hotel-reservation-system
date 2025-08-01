package com.example.project1.controller;

import com.example.project1.model.BookingSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingStep3Controller {

    private static final Logger LOGGER = Logger.getLogger(BookingStep3Controller.class.getName());

    @FXML private Label suggestionLabel;
    @FXML private Label datesDisplayLabel; // Added in the previous step
    @FXML private CheckBox singleRoomCheckBox;
    @FXML private TextField singleRoomQuantityField;
    @FXML private CheckBox doubleRoomCheckBox;
    @FXML private TextField doubleRoomQuantityField;
    @FXML private CheckBox deluxeRoomCheckBox;
    @FXML private TextField deluxeRoomQuantityField;
    @FXML private CheckBox penthouseCheckBox;
    @FXML private TextField penthouseQuantityField;

    private BookingSession bookingSession;

    // Prices per room type per night
    private static final double SINGLE_ROOM_PRICE = 100.00;
    private static final double DOUBLE_ROOM_PRICE = 180.00;
    private static final double DELUXE_ROOM_PRICE = 250.00;
    private static final double PENTHOUSE_PRICE = 500.00;


    @FXML
    public void initialize() {
        // You might set initial suggestions based on guest count here if bookingSession is already available.
        // Or leave it to setBookingSession to populate.
    }

    public void setBookingSession(BookingSession session) {
        this.bookingSession = session;
        LOGGER.log(Level.INFO, "Booking Session received (Step 3 Updated): {0}", bookingSession);

        // Display check-in and check-out dates
        if (bookingSession.getCheckInDate() != null && bookingSession.getCheckOutDate() != null) {
            datesDisplayLabel.setText(
                    "Check-in: " + bookingSession.getCheckInDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                            " | Check-out: " + bookingSession.getCheckOutDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            );
        }

        // Pre-populate selections if navigating back
        if (bookingSession.getSelectedRoomsAndQuantities() != null && !bookingSession.getSelectedRoomsAndQuantities().isEmpty()) {
            populateRoomSelections(bookingSession.getSelectedRoomsAndQuantities());
        } else {
            // Suggest initial rooms based on number of guests, if no rooms selected yet
            suggestRooms(bookingSession.getNumberOfGuests());
        }
    }

    private void suggestRooms(int guests) {
        // Simple suggestion logic:
        // Prioritize double rooms for groups, single for individuals.
        // This is a basic example and can be expanded.
        if (guests > 0) {
            int suggestedDoubleRooms = guests / 4; // Each double room can accommodate 4
            int remainingGuests = guests % 4;

            int suggestedSingleRooms = 0;
            if (remainingGuests > 0) {
                // If remaining guests fit in single rooms (max 2 per single), use singles
                suggestedSingleRooms = (remainingGuests + 1) / 2; // +1 to round up if odd number (e.g., 3 guests -> 2 singles)
            }

            if (suggestedDoubleRooms > 0) {
                doubleRoomCheckBox.setSelected(true);
                doubleRoomQuantityField.setText(String.valueOf(suggestedDoubleRooms));
            }
            if (suggestedSingleRooms > 0) {
                singleRoomCheckBox.setSelected(true);
                singleRoomQuantityField.setText(String.valueOf(suggestedSingleRooms));
            }

            suggestionLabel.setText("Based on " + guests + " guests, we suggest " +
                    (suggestedDoubleRooms > 0 ? suggestedDoubleRooms + " Double Room(s) " : "") +
                    (suggestedSingleRooms > 0 ? suggestedSingleRooms + " Single Room(s) " : "") +
                    ". Please adjust as needed.");
        } else {
            suggestionLabel.setText("Please select your preferred rooms and quantity.");
        }
    }

    private void populateRoomSelections(Map<String, Integer> selections) {
        if (selections.containsKey("Single Room")) {
            singleRoomCheckBox.setSelected(true);
            singleRoomQuantityField.setText(String.valueOf(selections.get("Single Room")));
        }
        if (selections.containsKey("Double Room")) {
            doubleRoomCheckBox.setSelected(true);
            doubleRoomQuantityField.setText(String.valueOf(selections.get("Double Room")));
        }
        if (selections.containsKey("Deluxe Room")) {
            deluxeRoomCheckBox.setSelected(true);
            deluxeRoomQuantityField.setText(String.valueOf(selections.get("Deluxe Room")));
        }
        if (selections.containsKey("Penthouse")) {
            penthouseCheckBox.setSelected(true);
            penthouseQuantityField.setText(String.valueOf(selections.get("Penthouse")));
        }
    }

    @FXML
    private void handleNext(ActionEvent event) {
        Map<String, Integer> selectedRooms = new HashMap<>();
        double currentCalculatedPrice = 0.0;
        long numberOfNights = 0;

        if (bookingSession.getCheckInDate() != null && bookingSession.getCheckOutDate() != null) {
            numberOfNights = ChronoUnit.DAYS.between(bookingSession.getCheckInDate(), bookingSession.getCheckOutDate());
        }

        try {
            if (singleRoomCheckBox.isSelected()) {
                int qty = parseQuantity(singleRoomQuantityField.getText(), "Single Room");
                selectedRooms.put("Single Room", qty);
                currentCalculatedPrice += qty * SINGLE_ROOM_PRICE * numberOfNights;
            }
            if (doubleRoomCheckBox.isSelected()) {
                int qty = parseQuantity(doubleRoomQuantityField.getText(), "Double Room");
                selectedRooms.put("Double Room", qty);
                currentCalculatedPrice += qty * DOUBLE_ROOM_PRICE * numberOfNights;
            }
            if (deluxeRoomCheckBox.isSelected()) {
                int qty = parseQuantity(deluxeRoomQuantityField.getText(), "Deluxe Room");
                selectedRooms.put("Deluxe Room", qty);
                currentCalculatedPrice += qty * DELUXE_ROOM_PRICE * numberOfNights;
            }
            if (penthouseCheckBox.isSelected()) {
                int qty = parseQuantity(penthouseQuantityField.getText(), "Penthouse");
                selectedRooms.put("Penthouse", qty);
                currentCalculatedPrice += qty * PENTHOUSE_PRICE * numberOfNights;
            }

            if (selectedRooms.isEmpty()) {
                showAlert("Selection Error", "Please select at least one room.");
                return;
            }

            // You might want to add logic here to ensure total capacity matches numberOfGuests
            // For now, just save selected rooms and quantity.

            bookingSession.setSelectedRoomsAndQuantities(selectedRooms);
            bookingSession.setTotalPrice(currentCalculatedPrice);

            LOGGER.log(Level.INFO, "Rooms selected and price calculated: {0}, Total Price: {1}",
                    new Object[]{bookingSession.getSelectedRoomsAndQuantities(), bookingSession.getTotalPrice()});

            // Load BookingStep4.fxml (Guest Details)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep4.fxml"); // Correctly pointing to Step4
            if (fxmlLocation == null) {
                showAlert("Navigation Error", "BookingStep4.fxml not found! Check the path.");
                LOGGER.log(Level.SEVERE, "BookingStep4.fxml not found.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            // Pass the BookingSession to the next controller (BookingStep4Controller)
            BookingStep4Controller nextController = fxmlLoader.getController();
            nextController.setBookingSession(bookingSession);

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Guest Details");
            stage.show();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for room quantities.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the guest details page.", e);
            showAlert("Navigation Error", "Could not load the guest details page: " + e.getMessage());
        }
    }

    private int parseQuantity(String text, String roomType) throws NumberFormatException {
        if (text.trim().isEmpty()) {
            throw new NumberFormatException("Quantity for " + roomType + " cannot be empty.");
        }
        int qty = Integer.parseInt(text.trim());
        if (qty <= 0) {
            throw new NumberFormatException("Quantity for " + roomType + " must be a positive number.");
        }
        return qty;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep2.fxml"); // Go back to date selection
            if (fxmlLocation == null) {
                showAlert("Navigation Error", "BookingStep2.fxml not found! Cannot go back.");
                LOGGER.log(Level.SEVERE, "BookingStep2.fxml not found for back navigation.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            BookingStep2Controller previousController = fxmlLoader.getController();
            previousController.setBookingSession(bookingSession); // Pass session back

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Choose Dates");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the previous booking page.", e);
            showAlert("Navigation Error", "Could not load the previous booking page: " + e.getMessage());
        }
    }

    @FXML
    private void handleRulesAndRegulations(ActionEvent event) {
        System.out.println("Displaying Rules & Regulations from Step 3...");
        // TODO: Implement logic to open a new window or scene for Rules & Regulations
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

