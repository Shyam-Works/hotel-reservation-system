package com.example.project1.controller;

import com.example.project1.dao.BookingDao;
import com.example.project1.model.BookingSession;
import com.example.project1.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminReportsController {

    private static final Logger LOGGER = Logger.getLogger(AdminReportsController.class.getName());
    private BookingDao bookingDao;
    private BookingSession currentBookingSession;

    // FXML Injections from AdminReport.fxml
    @FXML private TextField searchField;
    @FXML private Label guestNameLabel;
    @FXML private Label reservationIdLabel;
    @FXML private Label billIdLabel;
    @FXML private Label checkInLabel;
    @FXML private Label checkOutLabel;
    @FXML private Label billDateLabel;
    @FXML private Label roomChargesLabel;
    @FXML private Label taxesLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalAmountLabel;

    private static final double TAX_RATE = 0.13; // Example tax rate (13%)
    private static final DecimalFormat CURRENCY_FORMATTER = new DecimalFormat("$#,##0.00");

    @FXML
    public void initialize() {
        bookingDao = new BookingDao();
    }

    // This method is for when the controller is loaded from the dashboard.
    public void setBookingSession(BookingSession session) {
        if (session != null) {
            this.currentBookingSession = session;
            populateReport();
        } else {
            clearReport();
            AlertUtil.showErrorAlert("No Booking Found", "No booking data was passed to the reports page.");
        }
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        String phoneNumber = searchField.getText().trim();
        if (phoneNumber.isEmpty()) {
            AlertUtil.showWarningAlert("Search Error", "Please enter a mobile number to generate a report.");
            return;
        }

        currentBookingSession = bookingDao.getBookingByPhone(phoneNumber);

        if (currentBookingSession != null) {
            populateReport();
        } else {
            clearReport();
            AlertUtil.showErrorAlert("Search Failed", "No booking found for the mobile number: " + phoneNumber);
        }
    }

    private void populateReport() {
        if (currentBookingSession == null) {
            clearReport();
            return;
        }

        // --- Populating Basic Information ---
        guestNameLabel.setText(currentBookingSession.getGuestFirstName() + " " + currentBookingSession.getGuestLastName());
        reservationIdLabel.setText(currentBookingSession.getReservationId());
        billIdLabel.setText(UUID.randomUUID().toString().substring(0, 8));
        checkInLabel.setText(currentBookingSession.getCheckInDate().toString());
        checkOutLabel.setText(currentBookingSession.getCheckOutDate().toString());
        billDateLabel.setText(LocalDate.now().toString());

        // --- Corrected Financials Calculation ---
        // Assuming totalPrice in the database is the final amount after discount and tax.
        // Assuming discountAmount is the value of the discount.

        double finalTotal = currentBookingSession.getTotalPrice();
        double discount = currentBookingSession.getDiscountAmount();

        // Calculate the amount before discount, but after taxes
        double preDiscountTotal = finalTotal + discount;

        // Calculate the base room charges (pre-tax)
        double roomCharges = preDiscountTotal;

        // Calculate the taxes based on the room charges
        double taxes = roomCharges * TAX_RATE;

        // The subtotal is the total of all charges before discounts
        double subtotal = roomCharges + taxes;

        // The final total is the subtotal minus the discount
        double totalAmount = subtotal - discount;

        roomChargesLabel.setText(CURRENCY_FORMATTER.format(roomCharges));
        taxesLabel.setText(CURRENCY_FORMATTER.format(taxes));
        subtotalLabel.setText(CURRENCY_FORMATTER.format(subtotal));
        discountLabel.setText("-" + CURRENCY_FORMATTER.format(discount));
        totalAmountLabel.setText(CURRENCY_FORMATTER.format(totalAmount));
    }

    private void clearReport() {
        guestNameLabel.setText("N/A");
        reservationIdLabel.setText("N/A");
        billIdLabel.setText("N/A");
        checkInLabel.setText("N/A");
        checkOutLabel.setText("N/A");
        billDateLabel.setText("N/A");
        roomChargesLabel.setText("$0.00");
        taxesLabel.setText("$0.00");
        subtotalLabel.setText("$0.00");
        discountLabel.setText("$0.00");
        totalAmountLabel.setText("$0.00");
        currentBookingSession = null;
    }

    @FXML
    private void handlePrintBill(ActionEvent event) {
        AlertUtil.showInformationAlert("Print Bill", "Printing functionality is not implemented in this version.");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/project1/AdminDashboard.fxml"));
            Parent adminDashboardParent = loader.load();
            Scene adminDashboardScene = new Scene(adminDashboardParent);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(adminDashboardScene);
            window.setTitle("Admin Dashboard");
            window.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error navigating back to Admin Dashboard.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load the previous screen.");
        }
    }
}