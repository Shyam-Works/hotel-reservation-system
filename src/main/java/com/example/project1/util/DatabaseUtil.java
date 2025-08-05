package com.example.project1.util;

import java.sql.Connection;
import com.example.project1.config.DatabaseConfig;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUtil {

    private static final Logger LOGGER = Logger.getLogger(DatabaseUtil.class.getName());
    private static final String DATABASE_URL = "jdbc:sqlite:hotel_reservation.db";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DatabaseConfig.DB_URL);
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

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             var stmt = conn.createStatement()) {

            // Create Guest table (if you still use this for separate guest data)
            String createGuestTable = "CREATE TABLE IF NOT EXISTS Guests (" +
                    "guestId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "firstName TEXT NOT NULL," +
                    "lastName TEXT NOT NULL," +
                    "email TEXT UNIQUE," +
                    "phone TEXT NOT NULL" +
                    ");";
            stmt.execute(createGuestTable);
            LOGGER.log(Level.INFO, "Guests table ensured.");

            // Create Rooms table (if you still use this for separate room data)
            String createRoomTable = "CREATE TABLE IF NOT EXISTS Rooms (" +
                    "roomId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "roomNumber TEXT UNIQUE NOT NULL," +
                    "roomType TEXT NOT NULL," + // e.g., SINGLE, DOUBLE, SUITE
                    "pricePerNight REAL NOT NULL," +
                    "isAvailable INTEGER NOT NULL DEFAULT 1" + // 1 for true, 0 for false
                    ");";
            stmt.execute(createRoomTable);
            LOGGER.log(Level.INFO, "Rooms table ensured.");

            // --- IMPORTANT CHANGE HERE: Create 'bookings' table ---
            String createBookingsTable = "CREATE TABLE IF NOT EXISTS bookings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "reservation_id TEXT UNIQUE NOT NULL," +
                    "number_of_guests INTEGER NOT NULL," +
                    "check_in_date TEXT NOT NULL," +
                    "check_out_date TEXT NOT NULL," +
                    "total_price REAL NOT NULL," +
                    "selected_rooms_summary TEXT," + // e.g., "Standard x 1; Deluxe x 1"
                    "guest_first_name TEXT NOT NULL," +
                    "guest_last_name TEXT NOT NULL," +
                    "guest_gender TEXT," +
                    "guest_phone TEXT NOT NULL," +
                    "guest_email TEXT NOT NULL," +
                    "guest_age INTEGER," +
                    "guest_street TEXT," +
                    "guest_apt_suite TEXT," +
                    "guest_city TEXT," +
                    "guest_province_state TEXT," +
                    "guest_country TEXT," +
                    "status TEXT DEFAULT 'Confirmed' " + // Added status column
                    ");";
            stmt.execute(createBookingsTable);
            LOGGER.log(Level.INFO, "bookings table ensured."); // Log for 'bookings'

            // Create Admin table (for login)
            String createAdminTable = "CREATE TABLE IF NOT EXISTS Admins (" +
                    "adminId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "passwordHash TEXT NOT NULL" + // Store hashed passwords!
                    ");";
            stmt.execute(createAdminTable);
            LOGGER.log(Level.INFO, "Admins table ensured.");

            // Add a default admin if the table is empty
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