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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BookingDao {

    private static final Logger LOGGER = Logger.getLogger(BookingDao.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private RoomDao roomDao;

    public BookingDao() {
        this.roomDao = new RoomDao();
        createBookingsTable();
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
                + " discount_amount REAL DEFAULT 0.0"
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
                "total_price, guest_first_name, guest_last_name, " +
                "guest_gender, guest_phone, guest_email, guest_age, guest_street, " +
                "guest_apt_suite, guest_city, guest_province_state, guest_country, status, discount_amount) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        int bookingId = -1;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, session.getReservationId());
            pstmt.setInt(2, session.getNumberOfGuests());
            pstmt.setString(3, session.getCheckInDate().format(DATE_FORMATTER));
            pstmt.setString(4, session.getCheckOutDate().format(DATE_FORMATTER));
            pstmt.setDouble(5, session.getTotalPrice());
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
            pstmt.setString(17, session.getStatus() != null ? session.getStatus() : "Confirmed");
            pstmt.setDouble(18, session.getDiscountAmount());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        bookingId = generatedKeys.getInt(1);
                        // NEW: Assign rooms to the booking using the new DAO
                        if (session.getAssignedRooms() != null && !session.getAssignedRooms().isEmpty()) {
                            List<Integer> roomIds = session.getAssignedRooms().stream()
                                    .map(Room::getRoomId)
                                    .collect(Collectors.toList());
                            roomDao.assignRoomsToBooking(bookingId, roomIds);
                        }
                    }
                }
                LOGGER.log(Level.INFO, "Booking with ID {0} saved to database successfully.", session.getReservationId());
            }
            return bookingId;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting booking into database: " + e.getMessage(), e);
            return -1;
        }
    }

    public boolean updateBookingStatus(String reservationId, String newStatus) {
        String sql = "UPDATE bookings SET status = ? WHERE reservation_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, reservationId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Booking status for Reservation ID {0} updated to {1}.", new Object[]{reservationId, newStatus});
                return true;
            } else {
                LOGGER.log(Level.WARNING, "No booking found with Reservation ID {0} for status update.", reservationId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating booking status for Reservation ID {0}: {1}", new Object[]{reservationId, e.getMessage()});
            return false;
        }
    }

    public List<ReservationDisplayData> getRecentAndUpcomingBookings() {
        List<ReservationDisplayData> bookings = new ArrayList<>();
        String sql = "SELECT b.reservation_id, b.guest_first_name, b.guest_last_name, b.check_in_date, b.status, " +
                "GROUP_CONCAT(r.room_type, '; ') AS rooms_summary " +
                "FROM bookings b " +
                "LEFT JOIN bookings_rooms br ON b.id = br.booking_id " +
                "LEFT JOIN rooms r ON br.room_id = r.room_id " +
                "WHERE b.check_out_date >= CURRENT_DATE AND b.status NOT IN ('Checked Out', 'Cancelled') " +
                "GROUP BY b.id " +
                "ORDER BY b.check_in_date ASC, b.guest_last_name ASC";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String reservationId = rs.getString("reservation_id");
                String guestName = rs.getString("guest_first_name") + " " + rs.getString("guest_last_name");
                String roomsSummary = rs.getString("rooms_summary");
                String checkInDateStr = rs.getString("check_in_date");
                LocalDate checkInDate = null;
                if (checkInDateStr != null) {
                    checkInDate = LocalDate.parse(checkInDateStr, DATE_FORMATTER);
                }
                String status = rs.getString("status");

                String roomDisplay = "Multiple";
                if (roomsSummary != null && !roomsSummary.isEmpty()) {
                    String[] roomTypes = roomsSummary.split(";");
                    roomDisplay = roomTypes[0].trim();
                }

                if (checkInDate != null) {
                    bookings.add(new ReservationDisplayData(
                            reservationId,
                            guestName,
                            roomDisplay,
                            checkInDate,
                            status
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching recent and upcoming bookings: {0}", e.getMessage());
        }
        return bookings;
    }

    public List<GuestSearchResult> searchBookings(String searchTerm) {
        List<GuestSearchResult> results = new ArrayList<>();
        String sql = "SELECT guest_first_name, guest_last_name, guest_phone, reservation_id, check_in_date " +
                "FROM bookings " +
                "WHERE guest_first_name LIKE ? OR guest_last_name LIKE ? OR guest_phone LIKE ? " +
                "ORDER BY check_in_date ASC";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String likeTerm = "%" + searchTerm.trim() + "%";
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
                    String checkInDate = rs.getString("check_in_date");

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

                    // NEW: Fetch assigned rooms from the database
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
                    session.setDiscountAmount(rs.getDouble("discount_amount"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching booking by reservation ID: " + e.getMessage(), e);
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

    public boolean updateBooking(BookingSession session) {
        String sql = "UPDATE bookings SET " +
                "number_of_guests = ?, check_in_date = ?, check_out_date = ?, " +
                "total_price = ?, guest_first_name = ?, guest_last_name = ?, guest_gender = ?, " +
                "guest_phone = ?, guest_email = ?, guest_age = ?, " +
                "guest_street = ?, guest_apt_suite = ?, guest_city = ?, " +
                "guest_province_state = ?, guest_country = ?, status = ?, " +
                "discount_amount = ? " +
                "WHERE reservation_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, session.getNumberOfGuests());
            pstmt.setString(2, session.getCheckInDate().format(DATE_FORMATTER));
            pstmt.setString(3, session.getCheckOutDate().format(DATE_FORMATTER));
            pstmt.setDouble(4, session.getTotalPrice());
            pstmt.setString(5, session.getGuestFirstName());
            pstmt.setString(6, session.getGuestLastName());
            pstmt.setString(7, session.getGuestGender());
            pstmt.setString(8, session.getGuestPhone());
            pstmt.setString(9, session.getGuestEmail());
            pstmt.setInt(10, session.getGuestAge());
            pstmt.setString(11, session.getGuestStreet());
            pstmt.setString(12, session.getGuestAptSuite());
            pstmt.setString(13, session.getGuestCity());
            pstmt.setString(14, session.getGuestProvinceState());
            pstmt.setString(15, session.getGuestCountry());
            pstmt.setString(16, session.getStatus() != null ? session.getStatus() : "Confirmed");
            pstmt.setDouble(17, session.getDiscountAmount());
            pstmt.setString(18, session.getReservationId());

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