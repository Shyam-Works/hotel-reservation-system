package com.example.project1.model;

public class Room {
    private String roomType;
    private String description;
    private double pricePerNight;
    private int maxAdults;
    // Add other properties like roomNumber, available dates if needed for advanced logic

    public Room(String roomType, String description, double pricePerNight, int maxAdults) {
        this.roomType = roomType;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.maxAdults = maxAdults;
    }

    // Getters
    public String getRoomType() {
        return roomType;
    }

    public String getDescription() {
        return description;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public int getMaxAdults() {
        return maxAdults;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomType='" + roomType + '\'' +
                ", description='" + description + '\'' +
                ", pricePerNight=" + pricePerNight +
                ", maxAdults=" + maxAdults +
                '}';
    }
}