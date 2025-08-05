package com.example.project1.controller;

import com.example.project1.dao.BookingDao; // To fetch booking data
import com.example.project1.model.ReservationDisplayData;
import javafx.scene.Parent;
import com.example.project1.util.AlertUtil; // For alerts
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminDashboardController {

    private static final Logger LOGGER = Logger.getLogger(AdminDashboardController.class.getName());

    @FXML private TableView<ReservationDisplayData> reservationsTable;
    @FXML private TableColumn<ReservationDisplayData, String> guestNameCol;
    @FXML private TableColumn<ReservationDisplayData, String> roomCol;
    @FXML private TableColumn<ReservationDisplayData, String> checkInTimeCol; // Will display check-in date
    @FXML private TableColumn<ReservationDisplayData, String> statusCol;

    private BookingDao bookingDao; // To interact with the database

    @FXML
    public void initialize() {
        bookingDao = new BookingDao(); // Initialize the DAO

        // Set up the table columns to map to the properties in ReservationDisplayData
        guestNameCol.setCellValueFactory(cellData -> cellData.getValue().guestNameProperty());
        roomCol.setCellValueFactory(cellData -> cellData.getValue().roomProperty());
        checkInTimeCol.setCellValueFactory(cellData -> cellData.getValue().checkInDateProperty()); // Display date here
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        loadCurrentReservations(); // Load data when the dashboard initializes
    }

    /**
     * Loads current and upcoming reservations into the table.
     */
    private void loadCurrentReservations() {
        List<ReservationDisplayData> data = bookingDao.getRecentAndUpcomingBookings();
        ObservableList<ReservationDisplayData> observableData = FXCollections.observableArrayList(data);
        reservationsTable.setItems(observableData);

        if (data.isEmpty()) {
            reservationsTable.setPlaceholder(new Label("No current or upcoming reservations found."));
        }
    }

    @FXML
    private void handleNewReservation(ActionEvent event) {
        LOGGER.log(Level.INFO, "New Reservation button clicked. Navigating to BookingStep1.fxml");
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/BookingStep1.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "BookingStep1.fxml not found! Check the path.");
                LOGGER.log(Level.SEVERE, "BookingStep1.fxml not found for new reservation navigation.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - New Reservation (Step 1)");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load BookingStep1 page for new reservation.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load new reservation page: " + e.getMessage());
        }
    }

    @FXML
    private void handleGuestSearch(ActionEvent event) {
        LOGGER.log(Level.INFO, "Guest Search button clicked. Navigating to AdminGuestSearch.fxml");
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/AdminGuestSearch.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "AdminGuestSearch.fxml not found! Check the path.");
                LOGGER.log(Level.SEVERE, "AdminGuestSearch.fxml not found for navigation from Dashboard.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            // You might want to pass data to the search controller, but for now, it's independent
            // AdminGuestSearchController searchController = fxmlLoader.getController();
            // searchController.setSomeData(...);

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Guest Search");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load Guest Search page.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load Guest Search page: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckOut(ActionEvent event) {
        ReservationDisplayData selectedBooking = reservationsTable.getSelectionModel().getSelectedItem();

        if (selectedBooking == null) {
            AlertUtil.showWarningAlert("No Selection", "Please select a reservation from the table to check out.");
            return;
        }

        // We assume ReservationDisplayData has a getReservationId() method (which it should)
        String reservationId = selectedBooking.getReservationId();
        String guestName = selectedBooking.getGuestName();

        // Confirmation dialog
        boolean confirm = AlertUtil.showConfirmationAlert("Confirm Check-Out",
                "Are you sure you want to check out " + guestName +
                        " (Reservation ID: " + reservationId + ")?\nThis will mark the booking as 'Checked Out' in the system.");

        if (confirm) {
            // Call the DAO to update the booking status to "Checked Out"
            boolean success = bookingDao.updateBookingStatus(reservationId, "Checked Out");

            if (success) {
                AlertUtil.showInformationAlert("Check-Out Successful",
                        "Booking for " + guestName + " (Reservation ID: " + reservationId + ") has been successfully checked out.");
                LOGGER.log(Level.INFO, "Booking for Reservation ID: {0} successfully checked out.", reservationId);
                loadCurrentReservations(); // Refresh the table to reflect the updated status
            } else {
                AlertUtil.showErrorAlert("Check-Out Failed",
                        "Failed to check out booking for " + guestName + " (Reservation ID: " + reservationId + "). Please try again.");
                LOGGER.log(Level.SEVERE, "Failed to check out booking for Reservation ID: {0}.", reservationId);
            }
        }
    }

    @FXML
    private void handleReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/project1/AdminReports.fxml"));
            Parent reportsParent = loader.load();
            Scene reportsScene = new Scene(reportsParent);

            // Get the current stage (window) and set the new scene
            Stage window = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            window.setScene(reportsScene);
            window.setTitle("Billing & Reports"); // Set the title for the new window
            window.show();
            LOGGER.log(Level.INFO, "Navigated to Billing & Reports screen.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error navigating to Billing & Reports screen.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load the Billing & Reports screen.");
        }
    }

    // --- Admin Logout ---
    // You might want to add a "Logout" button to your AdminDashboard.fxml
    @FXML
    private void handleLogout(ActionEvent event) {
        LOGGER.log(Level.INFO, "Admin logging out.");
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/KioskWelcome.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "KioskWelcome.fxml not found!");
                LOGGER.log(Level.SEVERE, "KioskWelcome.fxml not found for logout navigation.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Welcome");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load welcome page after logout.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load welcome page: " + e.getMessage());
        }
    }



}