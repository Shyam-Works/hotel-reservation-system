package com.example.project1.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUtil {

    private static final Logger LOGGER = Logger.getLogger(DatabaseUtil.class.getName());
    private static final String DATABASE_URL = "jdbc:sqlite:hotel_reservation.db"; // This will create/connect to hotel_reservation.db in your project root

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load the SQLite JDBC driver (optional for modern JDKs, but good practice)
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DATABASE_URL);
            LOGGER.log(Level.INFO, "Database connection established.");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "SQLite JDBC driver not found.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database.", e);
        }
        return connection;
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.log(Level.INFO, "Database connection closed.");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to close database connection.", e);
            }
        }
    }

    // You might want a method to initialize your database schema
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             var stmt = conn.createStatement()) {

            // Create Guest table
            String createGuestTable = "CREATE TABLE IF NOT EXISTS Guests (" +
                    "guestId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "firstName TEXT NOT NULL," +
                    "lastName TEXT NOT NULL," +
                    "email TEXT UNIQUE," +
                    "phone TEXT NOT NULL" +
                    ");";
            stmt.execute(createGuestTable);
            LOGGER.log(Level.INFO, "Guests table ensured.");

            // Create Rooms table
            String createRoomTable = "CREATE TABLE IF NOT EXISTS Rooms (" +
                    "roomId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "roomNumber TEXT UNIQUE NOT NULL," +
                    "roomType TEXT NOT NULL," + // e.g., SINGLE, DOUBLE, SUITE
                    "pricePerNight REAL NOT NULL," +
                    "isAvailable INTEGER NOT NULL DEFAULT 1" + // 1 for true, 0 for false
                    ");";
            stmt.execute(createRoomTable);
            LOGGER.log(Level.INFO, "Rooms table ensured.");

            // Create Reservations table
            String createReservationTable = "CREATE TABLE IF NOT EXISTS Reservations (" +
                    "reservationId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "guestId INTEGER NOT NULL," +
                    "roomId INTEGER NOT NULL," +
                    "checkInDate TEXT NOT NULL," + // YYYY-MM-DD format
                    "checkOutDate TEXT NOT NULL," + // YYYY-MM-DD format
                    "totalAmount REAL NOT NULL," +
                    "status TEXT NOT NULL," + // e.g., CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
                    "FOREIGN KEY (guestId) REFERENCES Guests(guestId)," +
                    "FOREIGN KEY (roomId) REFERENCES Rooms(roomId)" +
                    ");";
            stmt.execute(createReservationTable);
            LOGGER.log(Level.INFO, "Reservations table ensured.");

            // Create Admin table (for login)
            String createAdminTable = "CREATE TABLE IF NOT EXISTS Admins (" +
                    "adminId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "passwordHash TEXT NOT NULL" + // Store hashed passwords!
                    ");";
            stmt.execute(createAdminTable);
            LOGGER.log(Level.INFO, "Admins table ensured.");

            // You might want to add a default admin if the table is empty
            var rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM Admins;");
            if (rs.next() && rs.getInt(1) == 0) {
                // For demonstration, using a plain password. In production, ALWAYS hash passwords.
                String insertAdmin = "INSERT INTO Admins (username, passwordHash) VALUES ('admin', 'password');";
                conn.createStatement().executeUpdate(insertAdmin);
                LOGGER.log(Level.INFO, "Default admin created: username='admin', password='password'");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed.", e);
        }
    }
}