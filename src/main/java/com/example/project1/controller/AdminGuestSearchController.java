package com.example.project1.controller;

import com.example.project1.dao.BookingDao; // To perform searches
import com.example.project1.model.GuestSearchResult; // Model for TableView
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.example.project1.model.BookingSession;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminGuestSearchController {

    private static final Logger LOGGER = Logger.getLogger(AdminGuestSearchController.class.getName());

    @FXML private TextField searchField;
    @FXML private TableView<GuestSearchResult> resultsTable;
    // Note: selectCol will be a dummy column for now if not implementing checkboxes
    @FXML private TableColumn<GuestSearchResult, String> selectCol;
    @FXML private TableColumn<GuestSearchResult, String> guestNameCol;
    @FXML private TableColumn<GuestSearchResult, String> phoneCol;
    @FXML private TableColumn<GuestSearchResult, String> reservationIdCol;
    @FXML private TableColumn<GuestSearchResult, String> checkInDateCol;

    private BookingDao bookingDao;

    @FXML
    public void initialize() {
        bookingDao = new BookingDao();

        // Set up cell value factories for table columns
        // selectCol is just a header for now, no actual data binding needed unless checkboxes are implemented.
        guestNameCol.setCellValueFactory(cellData -> cellData.getValue().guestNameProperty());
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        reservationIdCol.setCellValueFactory(cellData -> cellData.getValue().reservationIdProperty());
        checkInDateCol.setCellValueFactory(cellData -> cellData.getValue().checkInDateProperty());

        // Set placeholder for empty table
        resultsTable.setPlaceholder(new Label("No guests found. Please enter a search term."));

        // Optional: Perform an initial empty search to show all guests, or keep it empty
        // loadSearchResults("");
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            AlertUtil.showWarningAlert("Search", "Please enter a guest name or phone number to search.");
            resultsTable.setItems(FXCollections.observableArrayList()); // Clear previous results
            return;
        }

        loadSearchResults(searchTerm);
    }

    private void loadSearchResults(String searchTerm) {
        List<GuestSearchResult> results = bookingDao.searchBookings(searchTerm);
        ObservableList<GuestSearchResult> observableResults = FXCollections.observableArrayList(results);
        resultsTable.setItems(observableResults);

        if (results.isEmpty()) {
            resultsTable.setPlaceholder(new Label("No results found for '" + searchTerm + "'."));
        } else {
            resultsTable.setPlaceholder(new Label("")); // Clear placeholder if results exist
        }
        LOGGER.log(Level.INFO, "Search for '{0}' completed. Found {1} results.", new Object[]{searchTerm, results.size()});
    }

    @FXML
    private void handleViewDetails(ActionEvent event) {
        GuestSearchResult selectedGuest = resultsTable.getSelectionModel().getSelectedItem();
        if (selectedGuest == null) {
            AlertUtil.showWarningAlert("No Selection", "Please select a guest from the table to view details.");
            return;
        }

        // Fetch the full BookingSession object from the database using the reservation ID
        BookingSession fullBookingDetails = bookingDao.getBookingByReservationId(selectedGuest.getReservationId());

        if (fullBookingDetails == null) {
            AlertUtil.showErrorAlert("Error", "Could not retrieve full details for the selected booking.");
            LOGGER.log(Level.WARNING, "Failed to retrieve full booking details for Reservation ID: {0}", selectedGuest.getReservationId());
            return;
        }

        try {
            // Get the current stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Load the FXML for the guest details page
            URL fxmlLocation = getClass().getResource("/com/example/project1/ViewGuestDetail.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "ViewGuestDetail.fxml not found! Check the path.");
                LOGGER.log(Level.SEVERE, "ViewGuestDetail.fxml not found for navigation from Guest Search.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            // Get the controller for the new page
            GuestDetailsController guestDetailsController = fxmlLoader.getController();
            // Pass the fetched BookingSession object to the details controller
            guestDetailsController.setBookingDetails(fullBookingDetails);

            // Set the new scene on the existing stage
            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Guest Details");
            stage.show(); // Show the stage with the new scene

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load Guest Details page.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load Guest Details page: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelBooking(ActionEvent event) {
        GuestSearchResult selectedGuest = resultsTable.getSelectionModel().getSelectedItem();
        if (selectedGuest == null) {
            AlertUtil.showWarningAlert("No Selection", "Please select a guest's booking to cancel.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmationAlert("Confirm Cancellation",
                "Are you sure you want to permanently cancel the booking for " + selectedGuest.getGuestName() +
                        " (Reservation ID: " + selectedGuest.getReservationId() + ")?\nThis action cannot be undone.");

        if (confirm) {
            // Attempt to delete the booking from the database
            boolean deleted = bookingDao.deleteBooking(selectedGuest.getReservationId());

            if (deleted) {
                AlertUtil.showInformationAlert("Booking Cancelled",
                        "Booking for " + selectedGuest.getGuestName() + " (Reservation ID: " + selectedGuest.getReservationId() + ") has been successfully cancelled and removed.");
                LOGGER.log(Level.INFO, "Booking successfully deleted for Reservation ID: {0}", selectedGuest.getReservationId());
            } else {
                AlertUtil.showErrorAlert("Cancellation Failed",
                        "Failed to cancel booking for " + selectedGuest.getGuestName() + " (Reservation ID: " + selectedGuest.getReservationId() + "). Please try again.");
                LOGGER.log(Level.WARNING, "Failed to delete booking for Reservation ID: {0}", selectedGuest.getReservationId());
            }

            // Refresh the table to show the updated list (booking should now be gone)
            loadSearchResults(searchField.getText().trim());
        }
        }

    @FXML
    private void handleModifyBooking(ActionEvent event) {
        GuestSearchResult selectedGuest = resultsTable.getSelectionModel().getSelectedItem();
        if (selectedGuest == null) {
            AlertUtil.showWarningAlert("No Selection", "Please select a guest's booking to modify.");
            return;
        }
        AlertUtil.showInformationAlert("Functionality",
                "Modify Booking functionality for Reservation ID: " + selectedGuest.getReservationId() + " coming soon!");
        LOGGER.log(Level.INFO, "Modify booking button clicked for Reservation ID: {0}", selectedGuest.getReservationId());
        // In a real app, you'd navigate to a booking modification screen, passing the reservation ID.
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/com/example/project1/AdminDashboard.fxml");
            if (fxmlLocation == null) {
                AlertUtil.showErrorAlert("Navigation Error", "AdminDashboard.fxml not found! Cannot go back.");
                LOGGER.log(Level.SEVERE, "AdminDashboard.fxml not found for back navigation from Guest Search.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            stage.setScene(scene);
            stage.setTitle("Hotel ABC - Admin Dashboard");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load Admin Dashboard page.", e);
            AlertUtil.showErrorAlert("Navigation Error", "Could not load Admin Dashboard page: " + e.getMessage());
        }
    }
}