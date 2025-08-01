package com.example.project1.dao;

import com.example.project1.model.Room;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    // This will act as our "dummy database" for now
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        rooms.add(new Room("Single Room", "Equipped with a Queen Bed. Max 2 Adults.", 100.00, 2));
        rooms.add(new Room("Double Room", "Spacious with two Queen Beds. Max 4 Adults.", 180.00, 4));
        rooms.add(new Room("Deluxe Room", "Premium amenities. King Bed. Max 2 Adults.", 250.00, 2));
        rooms.add(new Room("Penthouse", "Luxurious suite with stunning views. Max 2 Adults.", 500.00, 2));
        return rooms;
    }

    // You can add methods here later to fetch from a real SQLite database
    // public Room getRoomByType(String roomType) { ... }
    // public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) { ... }
}