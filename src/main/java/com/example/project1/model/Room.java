package com.example.project1.model;

public class Room {
    private int roomId; // Matches your 'roomId' column
    private String roomNumber;
    private String roomType;
    private double pricePerNight; // Matches your 'pricePerNight' column
    private String status; // Internal status string (e.g., "Available", "Occupied")

    // Constructor for creating a new Room (without ID yet, default status)
    public Room(String roomNumber, String roomType, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.status = "Available"; // Default status
    }

    // Constructor for loading from DB (with all fields)
    public Room(int roomId, String roomNumber, String roomType, double pricePerNight, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.status = status;
    }

    // Getters
    public int getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public double getPricePerNight() { return pricePerNight; }
    public String getStatus() { return status; }

    // Setters
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Room{" +
                "roomId=" + roomId +
                ", roomNumber='" + roomNumber + '\'' +
                ", roomType='" + roomType + '\'' +
                ", pricePerNight=" + pricePerNight +
                ", status='" + status + '\'' +
                '}';
    }
}