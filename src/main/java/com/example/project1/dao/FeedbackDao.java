package com.example.project1.dao;

import com.example.project1.config.DatabaseConfig; // Assuming you have this for DB_URL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeedbackDao {

    private static final Logger LOGGER = Logger.getLogger(FeedbackDao.class.getName());
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FeedbackDao() {
        createFeedbackTable();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.DB_URL);
    }

    /**
     * Creates the 'feedback' table if it does not already exist.
     */
    public void createFeedbackTable() {
        String sql = "CREATE TABLE IF NOT EXISTS feedback (\n"
                + " feedback_id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " phone_number TEXT NOT NULL,\n"
                + " rating INTEGER NOT NULL,\n"
                + " comment TEXT,\n"
                + " submission_date TEXT NOT NULL\n"
                + ");";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "Feedback table created or already exists.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating feedback table: " + e.getMessage(), e);
        }
    }

    /**
     * Inserts a new feedback record into the 'feedback' table.
     * @param phoneNumber The user's mobile number.
     * @param rating The rating given (1-5).
     * @param comment The feedback comment.
     * @return true if the feedback was successfully inserted, false otherwise.
     */
    public boolean insertFeedback(String phoneNumber, int rating, String comment) {
        String sql = "INSERT INTO feedback(phone_number, rating, comment, submission_date) VALUES(?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phoneNumber);
            pstmt.setInt(2, rating);
            pstmt.setString(3, comment);
            pstmt.setString(4, LocalDateTime.now().format(DATETIME_FORMATTER)); // Record submission date/time

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Feedback submitted successfully for phone number: {0}", phoneNumber);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Failed to insert feedback for phone number: {0}", phoneNumber);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting feedback: " + e.getMessage(), e);
            return false;
        }
    }
}