package com.example.project1.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType; // Import AlertType for different alert types
import javafx.scene.control.ButtonType; // <--- NEW IMPORT for ButtonType
import java.util.Optional; // <--- NEW IMPORT for Optional

public class AlertUtil {

    /**
     * Displays an alert dialog with the specified title, header, and content.
     *
     * @param alertType The type of alert (e.g., AlertType.INFORMATION, AlertType.ERROR, AlertType.WARNING)
     * @param title     The title of the alert window.
     * @param header    The header text of the alert (can be null).
     * @param content   The main content message of the alert.
     */
    public static void showAlert(AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header); // Header can be null for simpler alerts
        alert.setContentText(content);
        alert.showAndWait(); // Show the alert and wait for user to close it
    }

    /**
     * Convenience method to show an ERROR alert.
     * @param title The title of the alert window.
     * @param content The main content message of the alert.
     */
    public static void showErrorAlert(String title, String content) {
        showAlert(AlertType.ERROR, title, null, content);
    }

    /**
     * Convenience method to show an INFORMATION alert.
     * @param title The title of the alert window.
     * @param content The main content message of the alert.
     */
    public static void showInformationAlert(String title, String content) {
        showAlert(AlertType.INFORMATION, title, null, content);
    }

    /**
     * Convenience method to show a WARNING alert.
     * @param title The title of the alert window.
     * @param content The main content message of the alert.
     */
    public static void showWarningAlert(String title, String content) {
        showAlert(AlertType.WARNING, title, null, content);
    }

    /**
     * Displays a confirmation alert dialog with "OK" and "Cancel" buttons.
     *
     * @param title   The title of the alert window.
     * @param content The main content message of the alert.
     * @return true if the user clicks "OK", false if the user clicks "Cancel" or closes the dialog.
     */
    public static boolean showConfirmationAlert(String title, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION); // Use CONFIRMATION type
        alert.setTitle(title);
        alert.setHeaderText(null); // No header for simple confirmation
        alert.setContentText(content);

        // Show the alert and wait for a response
        Optional<ButtonType> result = alert.showAndWait();

        // Check which button was clicked
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}