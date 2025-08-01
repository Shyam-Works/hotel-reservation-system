package com.example.project1.controller;

import com.example.project1.dao.BookingDao; // To fetch booking data
import com.example.project1.model.ReservationDisplayData; // To populate the table
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

    // --- Button Action Handlers (Stubs for now) ---

    @FXML
    private void handleNewReservation(ActionEvent event) {
        AlertUtil.showInformationAlert("Functionality", "New Reservation functionality coming soon!");
        LOGGER.log(Level.INFO, "New Reservation button clicked.");
        // Implement navigation to a new booking flow for admin
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
        AlertUtil.showInformationAlert("Functionality", "Check-Out functionality coming soon!");
        LOGGER.log(Level.INFO, "Check-Out button clicked.");
        // Implement logic for checking out guests
    }

    @FXML
    private void handleReports(ActionEvent event) {
        AlertUtil.showInformationAlert("Functionality", "Reports functionality coming soon!");
        LOGGER.log(Level.INFO, "Reports button clicked.");
        // Implement navigation to a reports page
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