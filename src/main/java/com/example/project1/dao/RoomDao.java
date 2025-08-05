package com.example.project1.dao;

import com.example.project1.model.Room;
import com.example.project1.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomDao {

    private static final Logger LOGGER = Logger.getLogger(RoomDao.class.getName());

    /**
     * Checks the number of available rooms of a specific type for a given date range.
     * @param roomType The type of room to check (e.g., "Single Room").
     * @param checkInDate The check-in date.
     * @param checkOutDate The check-out date.
     * @return The count of available rooms.
     */
    public int countAvailableRooms(String roomType, LocalDate checkInDate, LocalDate checkOutDate) {
        String sql = "SELECT COUNT(room_id) FROM rooms " +
                "WHERE type = ? AND room_id NOT IN (" +
                "SELECT room_id FROM booking_details bd " +
                "JOIN bookings b ON bd.booking_id = b.booking_id " +
                "WHERE (b.check_in < ? AND b.check_out > ?) OR (b.check_in < ? AND b.check_out > ?)" +
                ")";

        int availableRooms = 0;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomType);
            pstmt.setString(2, checkOutDate.toString());
            pstmt.setString(3, checkInDate.toString());
            pstmt.setString(4, checkOutDate.toString());
            pstmt.setString(5, checkInDate.toString());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                availableRooms = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting available rooms for " + roomType, e);
        }
        return availableRooms;
    }

    /**
     * Finds a list of available Room objects for a given room type and date range.
     * @return A list of available Room objects.
     */
    public List<Room> findAvailableRooms(String roomType, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> availableRooms = new ArrayList<>();
        String query = "SELECT r.room_id, r.room_number, r.room_type, r.price_per_night " +
                "FROM rooms r " +
                "LEFT JOIN bookings_rooms br ON r.room_id = br.room_id " +
                "LEFT JOIN bookings b ON br.booking_id = b.booking_id AND b.status != 'Cancelled' " +
                "WHERE r.room_type = ? " +
                "AND r.room_id NOT IN (" +
                "   SELECT br2.room_id FROM bookings_rooms br2 " +
                "   JOIN bookings b2 ON br2.booking_id = b2.booking_id " +
                "   WHERE NOT (b2.check_out_date <= ? OR b2.check_in_date >= ?) " +
                "   AND b2.status != 'Cancelled'" +
                ") " +
                "GROUP BY r.room_id, r.room_number, r.room_type, r.price_per_night";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, roomType);
            pstmt.setDate(2, java.sql.Date.valueOf(checkInDate));
            pstmt.setDate(3, java.sql.Date.valueOf(checkOutDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Room room = new Room(
                            rs.getInt("room_id"),
                            rs.getString("room_number"),
                            rs.getString("room_type"),
                            rs.getDouble("price_per_night"),
                            "Available"
                    );
                    availableRooms.add(room);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding available rooms for " + roomType, e);
        }
        return availableRooms;
    }

    /**
     * Assigns rooms to a booking by inserting records into bookings_rooms.
     * @param bookingId The ID of the booking.
     * @param roomIds The list of room IDs to assign.
     */
    public void assignRoomsToBooking(int bookingId, List<Integer> roomIds) {
        String query = "INSERT INTO bookings_rooms (booking_id, room_id) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            conn.setAutoCommit(false);
            for (Integer roomId : roomIds) {
                pstmt.setInt(1, bookingId);
                pstmt.setInt(2, roomId);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
            LOGGER.log(Level.INFO, "Assigned {0} rooms to booking ID: {1}.", new Object[]{roomIds.size(), bookingId});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error assigning rooms to booking ID: " + bookingId, e);
        }
    }
}