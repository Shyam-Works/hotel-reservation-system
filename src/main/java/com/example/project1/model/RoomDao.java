//package com.example.project1.dao;
//
//import com.example.project1.model.Room;
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//public class RoomDao {
//    private static final Logger LOGGER = Logger.getLogger(RoomDao.class.getName());
//    private static final String DATABASE_URL = "jdbc:sqlite:hotel_bookings.db";
//
//    public RoomDao() {
//        // We assume 'Rooms' table already exists as per user's info.
//        // We only need to ensure the junction table exists.
//        createBookingRoomAssignmentsTable();
//    }
//
//    private Connection connect() throws SQLException {
//        return DriverManager.getConnection(DATABASE_URL);
//    }
//
//    // This method is now only for the junction table
//    public void createBookingRoomAssignmentsTable() {
//        String sql = "CREATE TABLE IF NOT EXISTS booking_room_assignments (" +
//                "booking_id INTEGER NOT NULL," +
//                "room_id INTEGER NOT NULL," + // Refers to your 'roomId' column in 'Rooms' table
//                "PRIMARY KEY (booking_id, room_id)," +
//                "FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE," +
//                "FOREIGN KEY (room_id) REFERENCES Rooms(roomId) ON DELETE CASCADE" + // Corrected FK
//                ");";
//        try (Connection conn = connect();
//             Statement stmt = conn.createStatement()) {
//            stmt.execute(sql);
//            LOGGER.log(Level.INFO, "Booking room assignments table checked/created successfully.");
//        } catch (SQLException e) {
//            LOGGER.log(Level.SEVERE, "Error creating booking room assignments table: " + e.getMessage(), e);
//        }
//    }
//
//    // Method to add a room (if you need it, make sure column names match your DB)
//    // This assumes you would add a new room to your Rooms table
//    public boolean addRoom(Room room) {
//        String sql = "INSERT INTO Rooms(roomNumber, roomType, pricePerNight, isAvailable) VALUES(?,?,?,?)";
//        try (Connection conn = connect();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, room.getRoomNumber());
//            pstmt.setString(2, room.getRoomType());
//            pstmt.setDouble(3, room.getPricePerNight());
//            pstmt.setInt(4, "Available".equalsIgnoreCase(room.getStatus()) ? 1 : 0); // Convert status to int for isAvailable
//            int rowsAffected = pstmt.executeUpdate();
//            if (rowsAffected > 0) {
//                LOGGER.log(Level.INFO, "Room {0} added successfully.", room.getRoomNumber());
//                return true;
//            }
//        } catch (SQLException e) {
//            LOGGER.log(Level.SEVERE, "Error adding room {0}: {1}", new Object[]{room.getRoomNumber(), e.getMessage()});
//        }
//        return false;
//    }
//
//    public List<Room> getAllRooms() {
//        List<Room> rooms = new ArrayList<>();
//        String sql = "SELECT roomId, roomNumber, roomType, pricePerNight, isAvailable FROM Rooms ORDER BY roomNumber ASC";
//        try (Connection conn = connect();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(sql)) {
//            while (rs.next()) {
//                // Convert isAvailable (int) from DB to status (String) for Room model
//                String status = rs.getInt("isAvailable") == 1 ? "Available" : "Occupied"; // Assuming 0 is occupied/not available
//                rooms.add(new Room(
//                        rs.getInt("roomId"),
//                        rs.getString("roomNumber"),
//                        rs.getString("roomType"),
//                        rs.getDouble("pricePerNight"),
//                        status
//                ));
//            }
//        } catch (SQLException e) {
//            LOGGER.log(Level.SEVERE, "Error getting all rooms: " + e.getMessage(), e);
//        }
//        return rooms;
//    }
//
//    /**
//     * Gets a list of available rooms matching the specified type.
//     * Uses the 'isAvailable' column (1 for true).
//     */
//    public List<Room> getAvailableRoomsByType(String roomType) {
//        List<Room> rooms = new ArrayList<>();
//        String sql = "SELECT roomId, roomNumber, roomType, pricePerNight, isAvailable FROM Rooms WHERE roomType = ? AND isAvailable = 1 ORDER BY roomNumber ASC";
//        try (Connection conn = connect();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, roomType);
//            try (ResultSet rs = pstmt.executeQuery()) {
//                while (rs.next()) {
//                    String status = rs.getInt("isAvailable") == 1 ? "Available" : "Occupied";
//                    rooms.add(new Room(
//                            rs.getInt("roomId"),
//                            rs.getString("roomNumber"),
//                            rs.getString("roomType"),
//                            rs.getDouble("pricePerNight"),
//                            status
//                    ));
//                }
//            }
//        } catch (SQLException e) {
//            LOGGER.log(Level.SEVERE, "Error getting available rooms by type {0}: {1}", new Object[]{roomType, e.getMessage()});
//        }
//        return rooms;
//    }
//
//    /**
//     * Updates the status (isAvailable) of a specific room by its roomId.
//     * Converts the string status (e.g., "Available", "Occupied") to the int value (1 or 0).
//     */
//    public boolean updateRoomStatus(int roomId, String newStatus) {
//        String sql = "UPDATE Rooms SET isAvailable = ? WHERE roomId = ?";
//        int isAvailableValue;
//        if ("Available".equalsIgnoreCase(newStatus)) {
//            isAvailableValue = 1;
//        } else {
//            // For "Occupied", "Maintenance", "Cleaning", or any other non-available status
//            isAvailableValue = 0;
//        }
//
//        try (Connection conn = connect();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, isAvailableValue);
//            pstmt.setInt(2, roomId);
//            int rowsAffected = pstmt.executeUpdate();
//            if (rowsAffected > 0) {
//                LOGGER.log(Level.INFO, "Room ID {0} isAvailable updated to {1} (from status '{2}').", new Object[]{roomId, isAvailableValue, newStatus});
//                return true;
//            }
//        } catch (SQLException e) {
//            LOGGER.log(Level.SEVERE, "Error updating status for room ID {0}: {1}", new Object[]{roomId, e.getMessage()});
//        }
//        return false;
//    }
//
//    /**
//     * Assigns a specific room to a booking in the junction table.
//     */
//    public boolean assignRoomToBooking(long bookingId, int roomId) {
//        String sql = "INSERT OR IGNORE INTO booking_room_assignments(booking_id, room_id) VALUES(?,?)";
//        try (Connection conn = connect();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setLong(1, bookingId);
//            pstmt.setInt(2, roomId);
//            int rowsAffected = pstmt.executeUpdate();
//            if (rowsAffected > 0) {
//                LOGGER.log(Level.INFO, "Room ID {0} assigned to Booking ID {1}.", new Object[]{roomId, bookingId});
//                return true;
//            }
//        } catch (SQLException e) {
//            LOGGER.log(Level.SEVERE, "Error assigning room ID {0} to Booking ID {1}: {2}", new Object[]{roomId, bookingId, e.getMessage()});
//        }
//        return false;
//    }
//
//    /**
//     * Removes a room assignment from a booking in the junction table.
//     */
//    public boolean removeRoomFromBooking(long bookingId, int roomId) {
//        String sql = "DELETE FROM booking_room_assignments WHERE booking_id = ? AND room_id = ?";
//        try (Connection conn = connect();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setLong(1, bookingId);
//            pstmt.setInt(2, roomId);
//            int rowsAffected = pstmt.executeUpdate();
//            if (rowsAffected > 0) {
//                LOGGER.log(Level.INFO, "Room ID {0} unassigned from Booking ID {1}.", new Object[]{roomId, bookingId});
//                return true;
//            }
//        } catch (SQLException e) {
//            LOGGER.log(Level.SEVERE, "Error unassigning room ID {0} from Booking ID {1}: {2}", new Object[]{roomId, bookingId, e.getMessage()});
//        }
//        return false;
//    }
//
//    /**
//     * Gets all rooms currently assigned to a specific booking.
//     * Fetches from your 'Rooms' table using the junction table.
//     */
//    public List<Room> getAssignedRoomsForBooking(long bookingId) {
//        List<Room> assignedRooms = new ArrayList<>();
//        String sql = "SELECT r.roomId, r.roomNumber, r.roomType, r.pricePerNight, r.isAvailable " +
//                "FROM Rooms r " + // Corrected table name
//                "JOIN booking_room_assignments bra ON r.roomId = bra.room_id " + // Corrected column names
//                "WHERE bra.booking_id = ?";
//        try (Connection conn = connect();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setLong(1, bookingId);
//            try (ResultSet rs = pstmt.executeQuery()) {
//                while (rs.next()) {
//                    String status = rs.getInt("isAvailable") == 1 ? "Available" : "Occupied";
//                    assignedRooms.add(new Room(
//                            rs.getInt("roomId"),
//                            rs.getString("roomNumber"),
//                            rs.getString("roomType"),
//                            rs.getDouble("pricePerNight"),
//                            status
//                    ));
//                }
//            }
//        } catch (SQLException e) {
//            LOGGER.log(Level.SEVERE, "Error getting assigned rooms for booking ID {0}: {1}", new Object[]{bookingId, e.getMessage()});
//        }
//        return assignedRooms;
//    }
//}