package com.example.project1.controller;

import com.example.project1.dao.BookingDao;
import com.example.project1.model.BookingSession;
import com.example.project1.model.GuestSearchResult; // Make sure this model exists
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID; // For generating a simple Bill ID
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.DecimalFormat; // For formatting currency

public class AdminReportsController {

    private static final Logger LOGGER = Logger.getLogger(AdminReportsController.class.getName());
    private BookingDao bookingDao;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DecimalFormat CURRENCY_FORMATTER = new DecimalFormat("$#,##0.00");

    // --- FXML Elements Injected from AdminReports.fxml ---
    @FXML private TextField searchField;
    @FXML private Label guestNameLabel;
    @FXML private Label reservationIdLabel;
    @FXML private Label billIdLabel; // Label for generated bill ID
    @FXML private Label checkInLabel;
    @FXML private Label checkOutLabel;
    @FXML private Label billDateLabel; // Label for current bill date
    @FXML private Label roomChargesLabel;
    @FXML private Label taxesLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalAmountLabel;
    @FXML private VBox billView; // The VBox containing all the report details

    // --- Constants for Calculations ---
    private static final double TAX_RATE = 0.13; // Example: 13% tax (e.g., HST in Ontario, Canada)
    private static final double DISCOUNT_RATE = 0.0; // Start with no discount for simplicity, can be updated

    // --- Constructor ---
    public AdminReportsController() {
        this.bookingDao = new BookingDao(); // Initialize BookingDao
    }

    // --- Initialization Method (called when FXML is loaded) ---
    @FXML
    public void initialize() {
        clearReportFields(); // Clear all fields initially
        billView.setVisible(false); // Hide the bill view until a report is generated
    }

    /**
     * Clears all labels in the report section and resets their values.
     */
    private void clearReportFields() {
        guestNameLabel.setText("N/A");
        reservationIdLabel.setText("N/A");
        billIdLabel.setText("N/A");
        checkInLabel.setText("N/A");
        checkOutLabel.setText("N/A");
        billDateLabel.setText("N/A");
        roomChargesLabel.setText(CURRENCY_FORMATTER.format(0.0));
        taxesLabel.setText(CURRENCY_FORMATTER.format(0.0));
        subtotalLabel.setText(CURRENCY_FORMATTER.format(0.0));
        discountLabel.setText(CURRENCY_FORMATTER.format(0.0));
        totalAmountLabel.setText(CURRENCY_FORMATTER.format(0.0));
    }

    // --- Event Handler for Generate Report Button ---
    @FXML
    private void handleGenerateReport(ActionEvent event) {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Search Empty", "Please enter a mobile number to search for.");
            clearReportFields();
            billView.setVisible(false);
            return;
        }

        // Use BookingDao to search for bookings by phone number
        List<GuestSearchResult> results = bookingDao.searchBookings(searchTerm);

        if (results.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Bookings Found", "No bookings found for the provided mobile number.");
            clearReportFields();
            billView.setVisible(false);
            return;
        }

        // --- Important Decision Point ---
        // For simplicity, we'll take the first booking found for the mobile number.
        // In a real application, if multiple bookings exist for a phone number,
        // you might want to display a list for the admin to choose from.
        GuestSearchResult selectedResult = results.get(0);
        String reservationId = selectedResult.getReservationId();

        // Retrieve the full BookingSession details using the reservation ID
        BookingSession bookingSession = bookingDao.getBookingByReservationId(reservationId);

        if (bookingSession == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not retrieve full booking details for Reservation ID: " + reservationId);
            clearReportFields();
            billView.setVisible(false);
            return;
        }

        // Populate the FXML labels with the retrieved data and calculated values
        populateReport(bookingSession);
        billView.setVisible(true); // Make the bill view visible
    }

    /**
     * Populates the report labels with data from the BookingSession.
     * @param session The BookingSession object containing the booking details.
     */
    private void populateReport(BookingSession session) {
        guestNameLabel.setText(session.getGuestFirstName() + " " + session.getGuestLastName());
        reservationIdLabel.setText(session.getReservationId());
        billIdLabel.setText("BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()); // Generate a unique, short bill ID
        checkInLabel.setText(session.getCheckInDate().format(DATE_FORMATTER));
        checkOutLabel.setText(session.getCheckOutDate().format(DATE_FORMATTER));
        billDateLabel.setText(LocalDate.now().format(DATE_FORMATTER)); // Set bill date to current date

        // --- Financial Calculations ---
        // Assumption: session.getTotalPrice() holds the base room charges before tax.
        double roomCharges = session.getTotalPrice();
        double taxes = roomCharges * TAX_RATE;
        double subtotal = roomCharges + taxes;
        double discountAmount = subtotal * DISCOUNT_RATE; // Apply discount rate if DISCOUNT_RATE > 0
        double totalAmountDue = subtotal - discountAmount;

        roomChargesLabel.setText(CURRENCY_FORMATTER.format(roomCharges));
        taxesLabel.setText(CURRENCY_FORMATTER.format(taxes));
        subtotalLabel.setText(CURRENCY_FORMATTER.format(subtotal));
        discountLabel.setText("-" + CURRENCY_FORMATTER.format(discountAmount)); // Display discount as negative
        totalAmountLabel.setText(CURRENCY_FORMATTER.format(totalAmountDue));
    }

    // --- Event Handler for Print Bill Button ---
    @FXML
    private void handlePrintBill(ActionEvent event) {
        // Placeholder for printing functionality. Actual printing to PDF/printer is complex.
        showAlert(Alert.AlertType.INFORMATION, "Print Feature", "Printing functionality is not yet implemented.");
        LOGGER.log(Level.INFO, "Print Bill button clicked.");
    }

    // --- Event Handler for Back to Dashboard Button ---
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/project1/AdminDashboard.fxml"));
            Parent adminDashboardParent = loader.load();
            Scene adminDashboardScene = new Scene(adminDashboardParent);

            // Get the current stage (window) and set the new scene
            Stage window = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            window.setScene(adminDashboardScene);
            window.setTitle("Admin Dashboard"); // Set appropriate title
            window.show();
            LOGGER.log(Level.INFO, "Navigated back to Admin Dashboard.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error navigating back to Admin Dashboard.", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the Admin Dashboard.");
        }
    }

    // --- Helper Method for Showing Alerts ---
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}