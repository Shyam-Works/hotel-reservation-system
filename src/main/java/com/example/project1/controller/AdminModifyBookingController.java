package com.example.project1.controller;

import com.example.project1.dao.BookingDao;
import com.example.project1.model.BookingSession;
import com.example.project1.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AdminModifyBookingController {

    private static final Logger LOGGER = Logger.getLogger(AdminModifyBookingController.class.getName());
    private BookingDao bookingDao;
    private BookingSession currentBookingSession;
    private static final DecimalFormat CURRENCY_FORMATTER = new DecimalFormat("$#,##0.00");

    // --- Room Pricing (from your BookingStep3.fxml) ---
    private static final double PRICE_PER_SINGLE_ROOM_PER_NIGHT = 100.00;
    private static final double PRICE_PER_DOUBLE_ROOM_PER_NIGHT = 180.00;
    private static final double PRICE_PER_DELUXE_ROOM_PER_NIGHT = 250.00;
    private static final double PRICE_PER_PENTHOUSE_ROOM_PER_NIGHT = 500.00;

    // --- FXML Injections ---
    @FXML private Label reservationIdLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Spinner<Integer> ageSpinner;
    @FXML private TextField streetNameField;
    @FXML private TextField aptSuiteField;
    @FXML private TextField cityField;
    @FXML private TextField provinceStateField;
    @FXML private TextField countryField;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private Spinner<Integer> numGuestsSpinner;

    // --- Room Spinners ---
    @FXML private Spinner<Integer> singleRoomQuantitySpinner;
    @FXML private Spinner<Integer> doubleRoomQuantitySpinner;
    @FXML private Spinner<Integer> deluxeRoomQuantitySpinner;
    @FXML private Spinner<Integer> penthouseQuantitySpinner;

    @FXML private Spinner<Double> discountSpinner;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label totalPriceLabel;

    // --- Constructor ---
    public AdminModifyBookingController() {
        this.bookingDao = new BookingDao();
    }

    // --- Initialization ---
    @FXML
    public void initialize() {
        // Initialize Spinners
        ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 18));
        numGuestsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        singleRoomQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        doubleRoomQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        deluxeRoomQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        penthouseQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));

        // Set the discount spinner to work with percentages (0 to 100)
        SpinnerValueFactory<Double> discountValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, 0.0, 1.0);
        discountSpinner.setValueFactory(discountValueFactory);
        discountSpinner.setEditable(true);

        // Initialize ComboBoxes
        genderComboBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        statusComboBox.setItems(FXCollections.observableArrayList("Confirmed", "Checked In", "Cancelled"));

        // Add listeners to recalculate price when inputs change
        checkInDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndUpdatePrice());
        checkOutDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndUpdatePrice());
        singleRoomQuantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndUpdatePrice());
        doubleRoomQuantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndUpdatePrice());
        deluxeRoomQuantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndUpdatePrice());
        penthouseQuantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndUpdatePrice());
        discountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndUpdatePrice());
    }

    public void setBookingSession(BookingSession session) {
        if (session == null) {
            LOGGER.log(Level.WARNING, "Attempted to set null BookingSession in AdminModifyBookingController.");
            AlertUtil.showErrorAlert("Error", "Booking data not provided for modification.");
            return;
        }
        this.currentBookingSession = session;
        populateFields();
        calculateAndUpdatePrice();
    }

    private void populateFields() {
        if (currentBookingSession == null) return;

        reservationIdLabel.setText("Reservation ID: " + currentBookingSession.getReservationId());

        currentBookingSession.parseSummaryToQuantities();
        Map<String, Integer> rooms = currentBookingSession.getSelectedRoomsAndQuantities();
        singleRoomQuantitySpinner.getValueFactory().setValue(rooms.getOrDefault("Single Room", 0));
        doubleRoomQuantitySpinner.getValueFactory().setValue(rooms.getOrDefault("Double Room", 0));
        deluxeRoomQuantitySpinner.getValueFactory().setValue(rooms.getOrDefault("Deluxe Room", 0));
        penthouseQuantitySpinner.getValueFactory().setValue(rooms.getOrDefault("Penthouse", 0));

        firstNameField.setText(currentBookingSession.getGuestFirstName());
        lastNameField.setText(currentBookingSession.getGuestLastName());
        genderComboBox.setValue(currentBookingSession.getGuestGender());
        phoneField.setText(currentBookingSession.getGuestPhone());
        emailField.setText(currentBookingSession.getGuestEmail());
        ageSpinner.getValueFactory().setValue(currentBookingSession.getGuestAge());
        streetNameField.setText(currentBookingSession.getGuestStreet());
        aptSuiteField.setText(currentBookingSession.getGuestAptSuite());
        cityField.setText(currentBookingSession.getGuestCity());
        provinceStateField.setText(currentBookingSession.getGuestProvinceState());
        countryField.setText(currentBookingSession.getGuestCountry());

        checkInDatePicker.setValue(currentBookingSession.getCheckInDate());
        checkOutDatePicker.setValue(currentBookingSession.getCheckOutDate());
        numGuestsSpinner.getValueFactory().setValue(currentBookingSession.getNumberOfGuests());
        statusComboBox.setValue(currentBookingSession.getStatus());

        // Populate the discount spinner with the percentage from the booking session
        discountSpinner.getValueFactory().setValue(currentBookingSession.getDiscountPercentage());
    }

    private void calculateAndUpdatePrice() {
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        double baseRoomPrice = 0.0;
        baseRoomPrice += singleRoomQuantitySpinner.getValue() * PRICE_PER_SINGLE_ROOM_PER_NIGHT;
        baseRoomPrice += doubleRoomQuantitySpinner.getValue() * PRICE_PER_DOUBLE_ROOM_PER_NIGHT;
        baseRoomPrice += deluxeRoomQuantitySpinner.getValue() * PRICE_PER_DELUXE_ROOM_PER_NIGHT;
        baseRoomPrice += penthouseQuantitySpinner.getValue() * PRICE_PER_PENTHOUSE_ROOM_PER_NIGHT;

        long numberOfNights = 0;
        if (checkIn != null && checkOut != null && checkIn.isBefore(checkOut)) {
            numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);
        }

        double calculatedTotalPrice = baseRoomPrice * numberOfNights;

        // Calculate discount based on percentage
        double discountPercentage = discountSpinner.getValue();
        if (discountPercentage < 0 || discountPercentage > 100) {
            discountPercentage = 0;
            discountSpinner.getValueFactory().setValue(0.0);
        }
        double discountAmount = calculatedTotalPrice * (discountPercentage / 100.0);
        calculatedTotalPrice -= discountAmount;

        if (calculatedTotalPrice < 0) {
            calculatedTotalPrice = 0;
        }

        totalPriceLabel.setText(CURRENCY_FORMATTER.format(calculatedTotalPrice));
    }

    @FXML
    private void handleSaveChanges(ActionEvent event) {
        if (currentBookingSession == null) {
            AlertUtil.showErrorAlert("Error", "No booking loaded to save changes for.");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        try {
            currentBookingSession.setGuestFirstName(firstNameField.getText().trim());
            currentBookingSession.setGuestLastName(lastNameField.getText().trim());
            currentBookingSession.setGuestGender(genderComboBox.getValue());
            currentBookingSession.setGuestPhone(phoneField.getText().trim());
            currentBookingSession.setGuestEmail(emailField.getText().trim());
            currentBookingSession.setGuestAge(ageSpinner.getValue());
            currentBookingSession.setGuestStreet(streetNameField.getText().trim());
            currentBookingSession.setGuestAptSuite(aptSuiteField.getText().trim());
            currentBookingSession.setGuestCity(cityField.getText().trim());
            currentBookingSession.setGuestProvinceState(provinceStateField.getText().trim());
            currentBookingSession.setGuestCountry(countryField.getText().trim());

            currentBookingSession.setCheckInDate(checkInDatePicker.getValue());
            currentBookingSession.setCheckOutDate(checkOutDatePicker.getValue());
            currentBookingSession.setNumberOfGuests(numGuestsSpinner.getValue());
            currentBookingSession.setStatus(statusComboBox.getValue());

            Map<String, Integer> updatedRooms = new HashMap<>();
            if (singleRoomQuantitySpinner.getValue() > 0) updatedRooms.put("Single Room", singleRoomQuantitySpinner.getValue());
            if (doubleRoomQuantitySpinner.getValue() > 0) updatedRooms.put("Double Room", doubleRoomQuantitySpinner.getValue());
            if (deluxeRoomQuantitySpinner.getValue() > 0) updatedRooms.put("Deluxe Room", deluxeRoomQuantitySpinner.getValue());
            if (penthouseQuantitySpinner.getValue() > 0) updatedRooms.put("Penthouse", penthouseQuantitySpinner.getValue());
            currentBookingSession.setSelectedRoomsAndQuantities(updatedRooms);

            // Generate the summary in the consistent format
            String summary = updatedRooms.entrySet().stream()
                    .map(entry -> entry.getValue() + "x " + entry.getKey())
                    .collect(Collectors.joining(", "));
            currentBookingSession.setSelectedRoomsSummary(summary);

            // Set the new discount percentage
            currentBookingSession.setDiscountPercentage(discountSpinner.getValue());

            calculateAndUpdatePrice();
            double newTotalPrice = Double.parseDouble(totalPriceLabel.getText().replace("$", "").replace(",", ""));
            currentBookingSession.setTotalPrice(newTotalPrice);

            if (AlertUtil.showConfirmationAlert("Confirm Update", "Are you sure you want to save changes for Reservation ID: " + currentBookingSession.getReservationId() + "?")) {
                boolean success = bookingDao.updateBooking(currentBookingSession);

                if (success) {
                    AlertUtil.showInformationAlert("Update Successful", "Booking for Reservation ID: " + currentBookingSession.getReservationId() + " updated successfully!");
                    handleBack(event);
                } else {
                    AlertUtil.showErrorAlert("Update Failed", "Failed to update booking for Reservation ID: " + currentBookingSession.getReservationId() + ".");
                }
            }
        } catch (NumberFormatException e) {
            AlertUtil.showErrorAlert("Invalid Input", "Please ensure all numeric fields (Age, Quantities, Discount) contain valid numbers.");
            LOGGER.log(Level.WARNING, "Number format error during booking update: " + e.getMessage(), e);
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Error", "An unexpected error occurred while saving changes: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Unexpected error during booking update: " + e.getMessage(), e);
        }
    }

    private boolean validateInputs() {
        String errorMessage = "";

        if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty() ||
                streetNameField.getText().trim().isEmpty() || cityField.getText().trim().isEmpty() ||
                provinceStateField.getText().trim().isEmpty() || countryField.getText().trim().isEmpty()) {
            errorMessage += "All required text fields must be filled out.\n";
        }
        if (!emailField.getText().trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            errorMessage += "Please enter a valid email address.\n";
        }
        if (!phoneField.getText().trim().matches("^\\+?[0-9\\s\\-()]{7,20}$")) {
            errorMessage += "Please enter a valid phone number.\n";
        }
        if (genderComboBox.getValue() == null || genderComboBox.getValue().isEmpty()) {
            errorMessage += "Please select a gender.\n";
        }
        if (statusComboBox.getValue() == null || statusComboBox.getValue().isEmpty()) {
            errorMessage += "Please select a booking status.\n";
        }

        if (checkInDatePicker.getValue() == null) {
            errorMessage += "Please select a check-in date.\n";
        }
        if (checkOutDatePicker.getValue() == null) {
            errorMessage += "Please select a check-out date.\n";
        } else if (checkInDatePicker.getValue() != null && checkOutDatePicker.getValue().isBefore(checkInDatePicker.getValue())) {
            errorMessage += "Check-out date cannot be before Check-in date.\n";
        }

        if (ageSpinner.getValue() == null || ageSpinner.getValue() < 1) {
            errorMessage += "Please enter a valid age.\n";
        }
        if (numGuestsSpinner.getValue() == null || numGuestsSpinner.getValue() < 1) {
            errorMessage += "Please enter a valid number of guests.\n";
        }

        int totalRoomsSelected = singleRoomQuantitySpinner.getValue() + doubleRoomQuantitySpinner.getValue() + deluxeRoomQuantitySpinner.getValue() + penthouseQuantitySpinner.getValue();
        if (totalRoomsSelected == 0) {
            errorMessage += "Please select at least one room.\n";
        }

        double discount = discountSpinner.getValue();
        if (discount < 0 || discount > 100) {
            errorMessage += "Discount percentage must be between 0 and 100.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            AlertUtil.showErrorAlert("Validation Error", errorMessage);
            return false;
        }
    }


    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/project1/AdminDashboard.fxml"));
            Parent adminDashboardParent = loader.load();
            Scene adminDashboardScene = new Scene(adminDashboardParent);

            Stage window = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            window.setScene(adminDashboardScene);
            window.setTitle("Admin Dashboard");
            window.show();
            LOGGER.log(Level.INFO, "Navigated back from Modify Booking screen.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error navigating back from Modify Booking.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load the previous screen.");
        }
    }
}