package com.example.project1.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class GuestSearchResult {
    private final StringProperty guestName;
    private final StringProperty phone;
    private final StringProperty reservationId;
    private final StringProperty checkInDate;

    public GuestSearchResult(String guestName, String phone, String reservationId, String checkInDate) {
        this.guestName = new SimpleStringProperty(guestName);
        this.phone = new SimpleStringProperty(phone);
        this.reservationId = new SimpleStringProperty(reservationId);
        this.checkInDate = new SimpleStringProperty(checkInDate);
    }

    // Getters for properties (essential for TableView cell value factory)
    public StringProperty guestNameProperty() { return guestName; }
    public StringProperty phoneProperty() { return phone; }
    public StringProperty reservationIdProperty() { return reservationId; }
    public StringProperty checkInDateProperty() { return checkInDate; }

    // Optional: Getters for raw values
    public String getGuestName() { return guestName.get(); }
    public String getPhone() { return phone.get(); }
    public String getReservationId() { return reservationId.get(); }
    public String getCheckInDate() { return checkInDate.get(); }
}