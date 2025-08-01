package com.example.project1.dao;

import com.example.project1.config.DatabaseConfig;
import com.example.project1.model.BookingSession;
import com.example.project1.model.ReservationDisplayData;
import com.example.project1.model.GuestSearchResult;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.example.project1.model.BookingSession;
public class BookingDao {

    private static final Logger LOGGER = Logger.getLogger(BookingDao.class.getName());
    // Formatter for storing dates as TEXT in the database
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BookingDao() {
        // When an instance of BookingDao is created, ensure the table exists
        createBookingsTable();
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
     * Creates the 'bookings' table in the database if it does not already exist.
     * This method is called automatically when a BookingDao object is instantiated.
     */
    public void createBookingsTable() {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS bookings (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " reservation_id TEXT NOT NULL UNIQUE,\n"
                + " number_of_guests INTEGER NOT NULL,\n"
                + " check_in_date TEXT NOT NULL,\n"
                + " check_out_date TEXT NOT NULL,\n"
                + " total_price REAL NOT NULL,\n"
                + " selected_rooms_summary TEXT,\n" // Summary of rooms, e.g., "Standard x 1; Deluxe x 2"
                + " guest_first_name TEXT NOT NULL,\n"
                + " guest_last_name TEXT NOT NULL,\n"
                + " guest_gender TEXT,\n"
                + " guest_phone TEXT NOT NULL,\n"
                + " guest_email TEXT NOT NULL,\n"
                + " guest_age INTEGER,\n"
                + " guest_street TEXT NOT NULL,\n"
                + " guest_apt_suite TEXT,\n"
                + " guest_city TEXT NOT NULL,\n"
                + " guest_province_state TEXT NOT NULL,\n"
                + " guest_country TEXT NOT NULL\n"
                + ");";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute(); // Execute the SQL statement
            LOGGER.log(Level.INFO, "Bookings table created or already exists.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating bookings table: " + e.getMessage(), e);
        }
    }



    /**
     * Inserts a new booking record into the 'bookings' table.
     * @param session The BookingSession object containing all the details of the booking.
     * @return true if the booking was successfully inserted, false otherwise.
     */
    public boolean insertBooking(BookingSession session) {
        String sql = "INSERT INTO bookings("
                + "reservation_id, number_of_guests, check_in_date, check_out_date, total_price, "
                + "selected_rooms_summary, guest_first_name, guest_last_name, guest_gender, "
                + "guest_phone, guest_email, guest_age, guest_street, guest_apt_suite, "
                + "guest_city, guest_province_state, guest_country) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Convert the map of selected rooms to a readable string for storage
            String roomsSummary = "";
            if (session.getSelectedRoomsAndQuantities() != null && !session.getSelectedRoomsAndQuantities().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Integer> entry : session.getSelectedRoomsAndQuantities().entrySet()) {
                    if (sb.length() > 0) sb.append("; "); // Use a semicolon as a separator
                    sb.append(entry.getKey()).append(" x ").append(entry.getValue());
                }
                roomsSummary = sb.toString();
            }

            // Set the parameters for the prepared statement
            pstmt.setString(1, session.getReservationId());
            pstmt.setInt(2, session.getNumberOfGuests());
            pstmt.setString(3, session.getCheckInDate().format(DATE_FORMATTER)); // Format date to string
            pstmt.setString(4, session.getCheckOutDate().format(DATE_FORMATTER)); // Format date to string
            pstmt.setDouble(5, session.getTotalPrice());
            pstmt.setString(6, roomsSummary); // Store the rooms summary string
            pstmt.setString(7, session.getGuestFirstName());
            pstmt.setString(8, session.getGuestLastName());
            pstmt.setString(9, session.getGuestGender());
            pstmt.setString(10, session.getGuestPhone());
            pstmt.setString(11, session.getGuestEmail());
            pstmt.setInt(12, session.getGuestAge());
            pstmt.setString(13, session.getGuestStreet());
            pstmt.setString(14, session.getGuestAptSuite());
            pstmt.setString(15, session.getGuestCity());
            pstmt.setString(16, session.getGuestProvinceState()); // Note: using getProvinceState
            pstmt.setString(17, session.getGuestCountry());

            pstmt.executeUpdate(); // Execute the insert statement
            LOGGER.log(Level.INFO, "Booking with ID {0} saved to database successfully.", session.getReservationId());
            return true;
        } catch (SQLException e) {
            // Log specific error for unique constraint violation
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed: bookings.reservation_id")) {
                LOGGER.log(Level.WARNING, "Attempted to save booking with a duplicate reservation ID: {0}. This booking may have already been saved.", session.getReservationId());
            } else {
                LOGGER.log(Level.SEVERE, "Error inserting booking into database: " + e.getMessage(), e);
            }
            return false;
        }
    }



    /**
     * Retrieves a list of booking records, primarily for display on the admin dashboard.
     * Can be extended to filter by date, status, etc.
     * For "Current Reservations (Next 24 Hours)", we'll fetch all and filter in controller or add a WHERE clause.
     * For simplicity, let's fetch recent and upcoming bookings.
     * @return A list of ReservationDisplayData objects.
     */
    public List<ReservationDisplayData> getRecentAndUpcomingBookings() {
        List<ReservationDisplayData> reservations = new ArrayList<>();
        // SQL to select relevant data from the bookings table
        // We'll order by check-in date to show upcoming first
        String sql = "SELECT guest_first_name, guest_last_name, selected_rooms_summary, check_in_date, check_out_date " +
                "FROM bookings " +
                "ORDER BY check_in_date ASC";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            LocalDate today = LocalDate.now();

            while (rs.next()) {
                String guestFirstName = rs.getString("guest_first_name");
                String guestLastName = rs.getString("guest_last_name");
                String fullName = guestFirstName + " " + guestLastName;

                String roomSummary = rs.getString("selected_rooms_summary");
                // Take first room type if multiple, or just the whole summary
                String primaryRoom = "N/A";
                if (roomSummary != null && !roomSummary.isEmpty()) {
                    primaryRoom = roomSummary.split(";")[0].trim(); // Takes the first room type listed
                    // Optional: You might want to parse this better if 'Room' column needs specific data
                }

                String checkInDateStr = rs.getString("check_in_date");
                LocalDate checkInDate = LocalDate.parse(checkInDateStr, DATE_FORMATTER); // Parse string to LocalDate

                String status;
                if (checkInDate.isAfter(today)) {
                    status = "Upcoming";
                } else if (checkInDate.isEqual(today)) {
                    status = "Today";
                } else {
                    status = "Past"; // Or "Checked Out" if you had a status field
                }

                // Filter for "Next 24 Hours" or "Current"
                // For "Next 24 Hours" from current time, you'd need check-in time in DB.
                // For simplicity now, let's just include all recent/upcoming that haven't fully passed.
                // A more precise "Next 24 Hours" would involve checking if check-in_date is today or tomorrow
                // AND checking check-in_time if available.
                // For now, let's include anything checked in today or future.
                if (!checkInDate.isBefore(today)) { // Include today and future bookings
                    reservations.add(new ReservationDisplayData(fullName, primaryRoom, checkInDateStr, status));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching bookings for admin dashboard: " + e.getMessage(), e);
        }
        return reservations;
    }




    /**
     * Searches for booking records by guest name (first or last) or phone number.
     * @param searchTerm The search query (can be part of name or phone number).
     * @return A list of GuestSearchResult objects matching the search term.
     */
    public List<GuestSearchResult> searchBookings(String searchTerm) {
        List<GuestSearchResult> results = new ArrayList<>();
        // Using LIKE for partial matches and || for concatenation in SQLite
        String sql = "SELECT guest_first_name, guest_last_name, guest_phone, reservation_id, check_in_date " +
                "FROM bookings " +
                "WHERE guest_first_name LIKE ? OR guest_last_name LIKE ? OR guest_phone LIKE ? " +
                "ORDER BY check_in_date ASC";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String likeTerm = "%" + searchTerm.trim() + "%"; // Add wildcards for partial search

            pstmt.setString(1, likeTerm);
            pstmt.setString(2, likeTerm);
            pstmt.setString(3, likeTerm);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String guestFirstName = rs.getString("guest_first_name");
                    String guestLastName = rs.getString("guest_last_name");
                    String fullName = guestFirstName + " " + guestLastName;
                    String phone = rs.getString("guest_phone");
                    String reservationId = rs.getString("reservation_id");
                    String checkInDate = rs.getString("check_in_date"); // Stored as TEXT

                    results.add(new GuestSearchResult(fullName, phone, reservationId, checkInDate));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching bookings: " + e.getMessage(), e);
        }
        return results;
    }



    public BookingSession getBookingByReservationId(String reservationId) {
        String sql = "SELECT * FROM bookings WHERE reservation_id = ?";
        BookingSession session = null;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reservationId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    session = new BookingSession();
                    session.setReservationId(rs.getString("reservation_id"));
                    session.setNumberOfGuests(rs.getInt("number_of_guests"));
                    session.setCheckInDate(LocalDate.parse(rs.getString("check_in_date"), DATE_FORMATTER));
                    session.setCheckOutDate(LocalDate.parse(rs.getString("check_out_date"), DATE_FORMATTER));
                    session.setTotalPrice(rs.getDouble("total_price"));
                    session.setSelectedRoomsSummary(rs.getString("selected_rooms_summary"));
                    session.setGuestFirstName(rs.getString("guest_first_name"));
                    session.setGuestLastName(rs.getString("guest_last_name"));
                    session.setGuestGender(rs.getString("guest_gender"));
                    session.setGuestPhone(rs.getString("guest_phone"));
                    session.setGuestEmail(rs.getString("guest_email"));
                    session.setGuestAge(rs.getInt("guest_age"));
                    session.setGuestStreet(rs.getString("guest_street"));
                    session.setGuestAptSuite(rs.getString("guest_apt_suite"));
                    session.setGuestCity(rs.getString("guest_city"));
                    session.setGuestProvinceState(rs.getString("guest_province_state"));
                    session.setGuestCountry(rs.getString("guest_country"));

                    // Assuming 'status' is not yet in your DB table 'bookings'.
                    // If you add a status column, you can retrieve it here.
                    // For now, let's default to "Confirmed" or derive based on date if needed
                    session.setStatus("Confirmed"); // Placeholder status

                    LOGGER.log(Level.INFO, "Fetched BookingSession for Reservation ID: {0}", reservationId);
                } else {
                    LOGGER.log(Level.WARNING, "No booking found for Reservation ID: {0}", reservationId);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching booking by reservation ID: " + e.getMessage(), e);
        }
        return session;
    }

    public boolean deleteBooking(String reservationId) {
        String sql = "DELETE FROM bookings WHERE reservation_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reservationId);
            int rowsAffected = pstmt.executeUpdate(); // Execute the delete statement

            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Booking with Reservation ID {0} deleted successfully.", reservationId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "No booking found with Reservation ID {0} for deletion.", reservationId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting booking with Reservation ID {0}: {1}", new Object[]{reservationId, e.getMessage()});
            return false;
        }
    }



    /**
     * Updates an existing booking record in the database.
     * The booking is identified by its reservation_id.
     * @param session The BookingSession object containing the updated booking details.
     * @return true if the booking was successfully updated, false otherwise.
     */
    public boolean updateBooking(BookingSession session) {
        String sql = "UPDATE bookings SET " +
                "number_of_guests = ?, check_in_date = ?, check_out_date = ?, " +
                "total_price = ?, selected_rooms_summary = ?, " +
                "guest_first_name = ?, guest_last_name = ?, guest_gender = ?, " +
                "guest_phone = ?, guest_email = ?, guest_age = ?, " +
                "guest_street = ?, guest_apt_suite = ?, guest_city = ?, " +
                "guest_province_state = ?, guest_country = ? " +
                "WHERE reservation_id = ?"; // Identify record by ID

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, session.getNumberOfGuests());
            pstmt.setString(2, session.getCheckInDate().format(DATE_FORMATTER));
            pstmt.setString(3, session.getCheckOutDate().format(DATE_FORMATTER));
            pstmt.setDouble(4, session.getTotalPrice());
            pstmt.setString(5, session.getSelectedRoomsSummary());
            pstmt.setString(6, session.getGuestFirstName());
            pstmt.setString(7, session.getGuestLastName());
            pstmt.setString(8, session.getGuestGender());
            pstmt.setString(9, session.getGuestPhone());
            pstmt.setString(10, session.getGuestEmail());
            pstmt.setInt(11, session.getGuestAge());
            pstmt.setString(12, session.getGuestStreet());
            pstmt.setString(13, session.getGuestAptSuite());
            pstmt.setString(14, session.getGuestCity());
            pstmt.setString(15, session.getGuestProvinceState());
            pstmt.setString(16, session.getGuestCountry());
            pstmt.setString(17, session.getReservationId()); // WHERE clause parameter

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Booking with Reservation ID {0} updated successfully.", session.getReservationId());
                return true;
            } else {
                LOGGER.log(Level.WARNING, "No booking found with Reservation ID {0} for update, or no changes made.", session.getReservationId());
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating booking with Reservation ID {0}: {1}", new Object[]{session.getReservationId(), e.getMessage()});
            return false;
        }
    }
}