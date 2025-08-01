package com.example.project1.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ReservationDisplayData {
    private final StringProperty guestName;
    private final StringProperty room;
    private final StringProperty checkInDate; // Renamed from checkInTime for consistency with DB date
    private final StringProperty status;

    public ReservationDisplayData(String guestName, String room, String checkInDate, String status) {
        this.guestName = new SimpleStringProperty(guestName);
        this.room = new SimpleStringProperty(room);
        this.checkInDate = new SimpleStringProperty(checkInDate);
        this.status = new SimpleStringProperty(status);
    }

    // Getters for properties (important for TableView)
    public StringProperty guestNameProperty() { return guestName; }
    public StringProperty roomProperty() { return room; }
    public StringProperty checkInDateProperty() { return checkInDate; }
    public StringProperty statusProperty() { return status; }

    // Optional: Getters for raw values
    public String getGuestName() { return guestName.get(); }
    public String getRoom() { return room.get(); }
    public String getCheckInDate() { return checkInDate.get(); }
    public String getStatus() { return status.get(); }
}