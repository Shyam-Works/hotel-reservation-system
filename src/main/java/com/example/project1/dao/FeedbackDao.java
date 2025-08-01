package com.example.project1.dao;

import com.example.project1.config.DatabaseConfig; // Make sure this path is correct

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeedbackDao {

    private static final Logger LOGGER = Logger.getLogger(FeedbackDao.class.getName());

    public FeedbackDao() {
        // This constructor will automatically create the table if it doesn't exist
        createFeedbackTable();
    }

    /**
     * Establishes a connection to the SQLite database.
     * @return A Connection object.
     * @throws SQLException If a database access error occurs.
     */
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.DB_URL);
    }

    /**
     * Creates the 'feedback' table in the database if it doesn't already exist.
     */
    public void createFeedbackTable() {
        String sql = "CREATE TABLE IF NOT EXISTS feedback (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " phone_number TEXT NOT NULL,\n"
                + " rating INTEGER NOT NULL,\n"
                + " comment TEXT,\n" // Comment can be optional
                + " submitted_at TEXT DEFAULT CURRENT_TIMESTAMP\n" // Records when feedback was submitted
                + ");";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
            LOGGER.log(Level.INFO, "Feedback table created or already exists.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating feedback table: " + e.getMessage(), e);
        }
    }

    /**
     * Inserts a new feedback record into the 'feedback' table.
     * @param phoneNumber The user's phone number.
     * @param rating The rating (1-5).
     * @param comment The user's comment (can be empty string if no comment).
     * @return true if the feedback was successfully inserted, false otherwise.
     */
    public boolean insertFeedback(String phoneNumber, int rating, String comment) {
        String sql = "INSERT INTO feedback(phone_number, rating, comment) VALUES(?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phoneNumber);
            pstmt.setInt(2, rating);
            pstmt.setString(3, comment);

            pstmt.executeUpdate(); // Execute the insert statement
            LOGGER.log(Level.INFO, "Feedback from phone {0} with rating {1} saved successfully.", new Object[]{phoneNumber, rating});
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting feedback into database: " + e.getMessage(), e);
            return false;
        }
    }
}