package com.example.project1.controller;

import com.example.project1.model.BookingSession;
import com.example.project1.util.AlertUtil;
import com.example.project1.dao.RoomDao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingStep3Controller {

    private static final Logger LOGGER = Logger.getLogger(BookingStep3Controller.class.getName());

    @FXML private Label suggestionLabel;
    @FXML private Label datesDisplayLabel;
    @FXML private CheckBox singleRoomCheckBox;
    @FXML private TextField singleRoomQuantityField;
    @FXML private CheckBox doubleRoomCheckBox;
    @FXML private TextField doubleRoomQuantityField;
    @FXML private CheckBox deluxeRoomCheckBox;
    @FXML private TextField deluxeRoomQuantityField;
    @FXML private CheckBox penthouseCheckBox;
    @FXML private TextField penthouseQuantityField;

    private BookingSession bookingSession;
    private final RoomDao roomDao = new RoomDao();

    // Prices per room type per night (These should be loaded from the DB in a final version)
    private static final double SINGLE_ROOM_PRICE = 100.00;
    private static final double DOUBLE_ROOM_PRICE = 180.00;
    private static final double DELUXE_ROOM_PRICE = 250.00;
    private static final double PENTHOUSE_PRICE = 500.00;

    // Max capacity for each room type based on your rules
    private static final int SINGLE_ROOM_CAPACITY = 2;
    private static final int DOUBLE_ROOM_CAPACITY = 4;
    private static final int DELUXE_ROOM_CAPACITY = 2;
    private static final int PENTHOUSE_CAPACITY = 2;

    @FXML
    public void initialize() {
        // Here we could attach listeners to checkboxes and text fields to update the UI
    }

    public void setBookingSession(BookingSession session) {
        this.bookingSession = session;
        LOGGER.log(Level.INFO, "Booking Session received (Step 3 Updated): {0}", bookingSession);

        if (bookingSession.getCheckInDate() != null && bookingSession.getCheckOutDate() != null) {
            datesDisplayLabel.setText(
                    "Check-in: " + bookingSession.getCheckInDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                            " | Check-out: " + bookingSession.getCheckOutDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            );
        }

        if (bookingSession.getSelectedRoomsAndQuantities() != null && !bookingSession.getSelectedRoomsAndQuantities().isEmpty()) {
            populateRoomSelections(bookingSession.getSelectedRoomsAndQuantities());
        } else {
            suggestRooms(bookingSession.getNumberOfGuests());
        }

        // Update the available room counts for each room type
        updateRoomAvailabilityDisplays();
    }

    private void updateRoomAvailabilityDisplays() {
        // This method will be implemented to show real-time availability
        // It's a good practice to separate this logic.
    }


    /**
     * Updated logic to suggest rooms based on the provided rules.
     */
    private void suggestRooms(int guests) {
        if (guests <= 0) {
            suggestionLabel.setText("Please select your preferred rooms and quantity.");
            return;
        }

        int suggestedDoubleRooms = 0;
        int suggestedSingleRooms = 0;
        int remainingGuests = guests;

        // Rule: For more than 4 adults, use multiple double or double + single
        if (remainingGuests > DOUBLE_ROOM_CAPACITY) {
            suggestedDoubleRooms = remainingGuests / DOUBLE_ROOM_CAPACITY;
            remainingGuests %= DOUBLE_ROOM_CAPACITY;
        }

        // Rule: More than 2 adults but less than 5 can have a Double Room or two Single Rooms.
        // We'll suggest a double room by default here as it's more space-efficient.
        if (remainingGuests > 0) {
            if (remainingGuests <= DOUBLE_ROOM_CAPACITY) {
                suggestedDoubleRooms++;
            } else {
                // Rule: Each single room holds max 2 people
                suggestedSingleRooms = (remainingGuests + SINGLE_ROOM_CAPACITY - 1) / SINGLE_ROOM_CAPACITY;
            }
        }

        String suggestion = "Based on " + guests + " guests, we suggest ";
        boolean hasSuggestion = false;

        if (suggestedDoubleRooms > 0) {
            doubleRoomCheckBox.setSelected(true);
            doubleRoomQuantityField.setText(String.valueOf(suggestedDoubleRooms));
            suggestion += suggestedDoubleRooms + " Double Room(s) ";
            hasSuggestion = true;
        }
        if (suggestedSingleRooms > 0) {
            singleRoomCheckBox.setSelected(true);
            singleRoomQuantityField.setText(String.valueOf(suggestedSingleRooms));
            suggestion += (hasSuggestion ? "and " : "") + suggestedSingleRooms + " Single Room(s)";
            hasSuggestion = true;
        }

        if (!hasSuggestion) {
            suggestionLabel.setText("Based on " + guests + " guests, we suggest a single room.");
            singleRoomCheckBox.setSelected(true);
            singleRoomQuantityField.setText("1");
        } else {
            suggestionLabel.setText(suggestion.trim() + ". Please adjust as needed.");
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
                selectedRooms.put("Single Room", parseQuantity(singleRoomQuantityField.getText(), "Single Room"));
            }
            if (doubleRoomCheckBox.isSelected()) {
                selectedRooms.put("Double Room", parseQuantity(doubleRoomQuantityField.getText(), "Double Room"));
            }
            if (deluxeRoomCheckBox.isSelected()) {
                selectedRooms.put("Deluxe Room", parseQuantity(deluxeRoomQuantityField.getText(), "Deluxe Room"));
            }
            if (penthouseCheckBox.isSelected()) {
                selectedRooms.put("Penthouse", parseQuantity(penthouseQuantityField.getText(), "Penthouse"));
            }

            if (selectedRooms.isEmpty()) {
                AlertUtil.showErrorAlert("Selection Error", "Please select at least one room.");
                return;
            }

            int totalCapacity = calculateTotalCapacity(selectedRooms);
            if (totalCapacity < bookingSession.getNumberOfGuests()) {
                AlertUtil.showErrorAlert("Capacity Error",
                        "The selected rooms can only accommodate " + totalCapacity + " guests, but your party has " + bookingSession.getNumberOfGuests() + " guests. Please add more rooms.");
                return;
            }

            StringBuilder summary = new StringBuilder();
            for (Map.Entry<String, Integer> entry : selectedRooms.entrySet()) {
                if (entry.getValue() > 0) {
                    summary.append(entry.getValue()).append("x ").append(entry.getKey()).append(", ");
                }
            }
            if (summary.length() > 0) {
                summary.setLength(summary.length() - 2);
            }

            bookingSession.setSelectedRoomsAndQuantities(selectedRooms);
            bookingSession.setSelectedRoomsSummary(summary.toString());

            // Calculate total price based on selected rooms and nights
            currentCalculatedPrice += selectedRooms.getOrDefault("Single Room", 0) * SINGLE_ROOM_PRICE * numberOfNights;
            currentCalculatedPrice += selectedRooms.getOrDefault("Double Room", 0) * DOUBLE_ROOM_PRICE * numberOfNights;
            currentCalculatedPrice += selectedRooms.getOrDefault("Deluxe Room", 0) * DELUXE_ROOM_PRICE * numberOfNights;
            currentCalculatedPrice += selectedRooms.getOrDefault("Penthouse", 0) * PENTHOUSE_PRICE * numberOfNights;

            bookingSession.setTotalPrice(currentCalculatedPrice);

            // --- Corrected section starts here ---
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep4.fxml");
            if (fxmlLocation == null) {
                // This is a navigation error, but the `load()` method below will handle IOException.
                // A more specific error message here is still good practice.
                AlertUtil.showErrorAlert("Navigation Error", "BookingStep4.fxml not found! Check the path.");
                LOGGER.log(Level.SEVERE, "BookingStep4.fxml not found.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load()); // This line must be in the try block

            BookingStep4Controller nextController = fxmlLoader.getController();
            nextController.setBookingSession(bookingSession);

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Guest Details");
            stage.show();

        } catch (NumberFormatException e) {
            AlertUtil.showErrorAlert("Input Error", "Please enter valid numbers for room quantities.");
        } catch (IOException e) { // This catch block now correctly handles the IOException
            LOGGER.log(Level.SEVERE, "Could not load the guest details page.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load the guest details page: " + e.getMessage());
        }
    }

    /**
     * New helper method to calculate the total capacity of the selected rooms.
     */
    private int calculateTotalCapacity(Map<String, Integer> selectedRooms) {
        int totalCapacity = 0;
        totalCapacity += selectedRooms.getOrDefault("Single Room", 0) * SINGLE_ROOM_CAPACITY;
        totalCapacity += selectedRooms.getOrDefault("Double Room", 0) * DOUBLE_ROOM_CAPACITY;
        totalCapacity += selectedRooms.getOrDefault("Deluxe Room", 0) * DELUXE_ROOM_CAPACITY;
        totalCapacity += selectedRooms.getOrDefault("Penthouse", 0) * PENTHOUSE_CAPACITY;
        return totalCapacity;
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
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep2.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "BookingStep2.fxml not found! Cannot go back.");
                LOGGER.log(Level.SEVERE, "BookingStep2.fxml not found for back navigation.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            BookingStep2Controller previousController = fxmlLoader.getController();
            previousController.setBookingSession(bookingSession);

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Choose Dates");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the previous booking page.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load the previous booking page: " + e.getMessage());
        }
    }

    @FXML
    private void handleRulesAndRegulations(ActionEvent event) {
        String rulesText = "• Single room: Max two people.\n\n" +
                "• Double room: Max 4 people.\n\n" +
                "• Deluxe and Pent rooms: Max two people but the prices are higher.\n\n" +
                "• More than 2 adults less than 5 can have Double room or two single rooms will be offered.\n\n" +
                "• More than 4 adults will have multiple Double or combination of Double and single rooms.";

        // Create a new Alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rules and Regulations");
        alert.setHeaderText("Hotel Room Booking Rules");

        // Use a TextArea for scrollable content
        TextArea textArea = new TextArea(rulesText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(200); // Set a preferred height to make the pop-up bigger

        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefWidth(500); // Set a preferred width
        alert.setResizable(true);

        alert.showAndWait();
    }
}