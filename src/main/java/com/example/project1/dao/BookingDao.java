package com.example.project1.dao;

import com.example.project1.config.DatabaseConfig;
import com.example.project1.model.BookingSession;
import com.example.project1.model.ReservationDisplayData;
import com.example.project1.model.GuestSearchResult;
import com.example.project1.model.Room;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BookingDao {

    private static final Logger LOGGER = Logger.getLogger(BookingDao.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private RoomDao roomDao;

    public BookingDao() {
        this.roomDao = new RoomDao();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.DB_URL);
    }

    public void createBookingsTable() {
        String sqlBookings = "CREATE TABLE IF NOT EXISTS bookings (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " reservation_id TEXT NOT NULL UNIQUE,\n"
                + " number_of_guests INTEGER NOT NULL,\n"
                + " check_in_date TEXT NOT NULL,\n"
                + " check_out_date TEXT NOT NULL,\n"
                + " total_price REAL NOT NULL,\n"
                + " final_price REAL NOT NULL,\n"
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
                + " guest_country TEXT NOT NULL,\n"
                + " status TEXT DEFAULT 'Confirmed',\n"
                + " discount_percentage REAL DEFAULT 0.0,\n" // Corrected column name
                + " selected_rooms_summary TEXT,\n"
                + " assigned_room_numbers TEXT\n" // Added missing column
                + ");";

        String sqlRooms = "CREATE TABLE IF NOT EXISTS rooms (\n"
                + " room_id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " room_number TEXT NOT NULL UNIQUE,\n"
                + " room_type TEXT NOT NULL,\n"
                + " price_per_night REAL NOT NULL\n"
                + ");";

        String sqlBookingsRooms = "CREATE TABLE IF NOT EXISTS bookings_rooms (\n"
                + " booking_room_id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " booking_id INTEGER NOT NULL,\n"
                + " room_id INTEGER NOT NULL,\n"
                + " FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,\n"
                + " FOREIGN KEY (room_id) REFERENCES rooms(room_id)\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlBookings);
            stmt.execute(sqlRooms);
            stmt.execute(sqlBookingsRooms);
            LOGGER.log(Level.INFO, "Tables created or already exist.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating tables: " + e.getMessage(), e);
        }
    }

    public int insertBooking(BookingSession session) {
        String sql = "INSERT INTO bookings(" +
                "reservation_id, number_of_guests, check_in_date, check_out_date, " +
                "total_price, final_price, guest_first_name, guest_last_name, " +
                "guest_gender, guest_phone, guest_email, guest_age, guest_street, " +
                "guest_apt_suite, guest_city, guest_province_state, guest_country, " +
                "status, discount_percentage, selected_rooms_summary, assigned_room_numbers) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String assignedRooms = session.getAssignedRooms().stream()
                    .map(Room::getRoomNumber) // Correctly get room number
                    .collect(Collectors.joining(","));

            pstmt.setString(1, session.getReservationId());
            pstmt.setInt(2, session.getNumberOfGuests());
            pstmt.setString(3, session.getCheckInDate().toString());
            pstmt.setString(4, session.getCheckOutDate().toString());
            pstmt.setDouble(5, session.getTotalPrice());
            pstmt.setDouble(6, session.getFinalPrice());
            pstmt.setString(7, session.getGuestFirstName());
            pstmt.setString(8, session.getGuestLastName());
            pstmt.setString(9, session.getGuestGender());
            pstmt.setString(10, session.getGuestPhone());
            pstmt.setString(11, session.getGuestEmail());
            pstmt.setInt(12, session.getGuestAge());
            pstmt.setString(13, session.getGuestStreet());
            pstmt.setString(14, session.getGuestAptSuite());
            pstmt.setString(15, session.getGuestCity());
            pstmt.setString(16, session.getGuestProvinceState());
            pstmt.setString(17, session.getGuestCountry());
            pstmt.setString(18, session.getStatus());
            pstmt.setDouble(19, session.getDiscountPercentage());
            pstmt.setString(20, session.getSelectedRoomsSummary());
            pstmt.setString(21, assignedRooms);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting booking into database: " + e.getMessage(), e);
        }
        return -1;
    }

    public List<ReservationDisplayData> getRecentAndUpcomingBookings() {
        List<ReservationDisplayData> reservations = new ArrayList<>();
        String sql = "SELECT reservation_id, guest_first_name, guest_last_name, check_in_date, status, selected_rooms_summary " +
                "FROM bookings " +
                "WHERE check_in_date = ? OR check_in_date > ? AND status != 'Checked Out' AND status != 'Cancelled' " +
                "ORDER BY check_in_date ASC";

        LocalDate today = LocalDate.now();
        String todayStr = today.format(DATE_FORMATTER);

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, todayStr);
            pstmt.setString(2, todayStr);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String guestName = rs.getString("guest_first_name") + " " + rs.getString("guest_last_name");
                    String roomsSummary = rs.getString("selected_rooms_summary");
                    String status = rs.getString("status");

                    reservations.add(new ReservationDisplayData(
                            rs.getString("reservation_id"),
                            guestName,
                            roomsSummary != null ? roomsSummary : "N/A",
                            LocalDate.parse(rs.getString("check_in_date"), DATE_FORMATTER),
                            status
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching recent and upcoming bookings: " + e.getMessage(), e);
        }
        return reservations;
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
                    session.setId(rs.getInt("id"));
                    session.setReservationId(rs.getString("reservation_id"));
                    session.setNumberOfGuests(rs.getInt("number_of_guests"));
                    session.setCheckInDate(LocalDate.parse(rs.getString("check_in_date"), DATE_FORMATTER));
                    session.setCheckOutDate(LocalDate.parse(rs.getString("check_out_date"), DATE_FORMATTER));
                    session.setTotalPrice(rs.getDouble("total_price"));
                    session.setFinalPrice(rs.getDouble("final_price"));
                    session.setDiscountPercentage(rs.getDouble("discount_percentage"));
                    session.setSelectedRoomsSummary(rs.getString("selected_rooms_summary"));

                    List<Room> assignedRooms = getAssignedRoomsForBooking(rs.getInt("id"));
                    session.setAssignedRooms(assignedRooms);

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
                    session.setStatus(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching booking by reservation ID: " + e.getMessage(), e);
        }
        return session;
    }

    public BookingSession getBookingByPhone(String phoneNumber) {
        String sql = "SELECT * FROM bookings WHERE guest_phone = ?";
        BookingSession session = null;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phoneNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    session = new BookingSession();
                    session.setId(rs.getInt("id"));
                    session.setReservationId(rs.getString("reservation_id"));
                    session.setNumberOfGuests(rs.getInt("number_of_guests"));
                    session.setCheckInDate(LocalDate.parse(rs.getString("check_in_date"), DATE_FORMATTER));
                    session.setCheckOutDate(LocalDate.parse(rs.getString("check_out_date"), DATE_FORMATTER));
                    session.setTotalPrice(rs.getDouble("total_price"));
                    session.setFinalPrice(rs.getDouble("final_price"));
                    session.setDiscountPercentage(rs.getDouble("discount_percentage"));
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
                    session.setStatus(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching booking by phone number: " + e.getMessage(), e);
        }
        return session;
    }

    private List<Room> getAssignedRoomsForBooking(int bookingId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.room_id, r.room_number, r.room_type, r.price_per_night " +
                "FROM rooms r " +
                "JOIN bookings_rooms br ON r.room_id = br.room_id " +
                "WHERE br.booking_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(new Room(
                            rs.getInt("room_id"),
                            rs.getString("room_number"),
                            rs.getString("room_type"),
                            rs.getDouble("price_per_night"),
                            "Reserved"
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching assigned rooms for booking ID: " + bookingId, e);
        }
        return rooms;
    }

    public boolean updateBooking(BookingSession session) {
        String sql = "UPDATE bookings SET " +
                "number_of_guests = ?, check_in_date = ?, check_out_date = ?, " +
                "total_price = ?, final_price = ?, guest_first_name = ?, guest_last_name = ?, " +
                "guest_gender = ?, guest_phone = ?, guest_email = ?, guest_age = ?, " +
                "guest_street = ?, guest_apt_suite = ?, guest_city = ?, " +
                "guest_province_state = ?, guest_country = ?, status = ?, " +
                "discount_percentage = ?, selected_rooms_summary = ?, assigned_room_numbers = ? " +
                "WHERE reservation_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String assignedRooms = session.getAssignedRooms().stream()
                    .map(Room::getRoomNumber)
                    .collect(Collectors.joining(","));

            int i = 1;
            pstmt.setInt(i++, session.getNumberOfGuests());
            pstmt.setString(i++, session.getCheckInDate().toString());
            pstmt.setString(i++, session.getCheckOutDate().toString());
            pstmt.setDouble(i++, session.getTotalPrice());
            pstmt.setDouble(i++, session.getFinalPrice());
            pstmt.setString(i++, session.getGuestFirstName());
            pstmt.setString(i++, session.getGuestLastName());
            pstmt.setString(i++, session.getGuestGender());
            pstmt.setString(i++, session.getGuestPhone());
            pstmt.setString(i++, session.getGuestEmail());
            pstmt.setInt(i++, session.getGuestAge());
            pstmt.setString(i++, session.getGuestStreet());
            pstmt.setString(i++, session.getGuestAptSuite());
            pstmt.setString(i++, session.getGuestCity());
            pstmt.setString(i++, session.getGuestProvinceState());
            pstmt.setString(i++, session.getGuestCountry());
            pstmt.setString(i++, session.getStatus());
            pstmt.setDouble(i++, session.getDiscountPercentage());
            pstmt.setString(i++, session.getSelectedRoomsSummary());
            pstmt.setString(i++, assignedRooms);
            pstmt.setString(i++, session.getReservationId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating booking: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateBookingStatus(String reservationId, String newStatus) {
        String sql = "UPDATE bookings SET status = ? WHERE reservation_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, reservationId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating booking status for Reservation ID " + reservationId, e);
            return false;
        }
    }

    public List<GuestSearchResult> searchGuestsByLastName(String lastName) {
        List<GuestSearchResult> results = new ArrayList<>();
        String sql = "SELECT reservation_id, guest_first_name, guest_last_name, guest_phone, check_in_date FROM bookings WHERE guest_last_name LIKE ? OR guest_first_name LIKE ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + lastName + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new GuestSearchResult(
                            rs.getString("reservation_id"),
                            rs.getString("guest_first_name") + " " + rs.getString("guest_last_name"),
                            rs.getString("guest_phone"),
                            rs.getString("check_in_date")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching for guests by last name: " + e.getMessage(), e);
        }
        return results;
    }

    public boolean deleteBooking(String reservationId) {
        String sql = "DELETE FROM bookings WHERE reservation_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reservationId);

            int rowsAffected = pstmt.executeUpdate();
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
}