package com.example.project1.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class GuestSearchResult {

    private final StringProperty reservationId;
    private final StringProperty guestName;
    private final StringProperty phone;
    private final StringProperty checkInDate;

    public GuestSearchResult(String reservationId, String guestName, String phone, String checkInDate) {
        this.reservationId = new SimpleStringProperty(reservationId);
        this.guestName = new SimpleStringProperty(guestName);
        this.phone = new SimpleStringProperty(phone);
        this.checkInDate = new SimpleStringProperty(checkInDate);
    }

    public String getReservationId() {
        return reservationId.get();
    }

    public StringProperty reservationIdProperty() {
        return reservationId;
    }

    public String getGuestName() {
        return guestName.get();
    }

    public StringProperty guestNameProperty() {
        return guestName;
    }

    public String getPhone() {
        return phone.get();
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public String getCheckInDate() {
        return checkInDate.get();
    }

    public StringProperty checkInDateProperty() {
        return checkInDate;
    }
}