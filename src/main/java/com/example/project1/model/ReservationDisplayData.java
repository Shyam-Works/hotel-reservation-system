package com.example.project1.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReservationDisplayData {

    private final StringProperty reservationId;
    private final StringProperty guestName;
    private final StringProperty room;
    private final StringProperty checkInDate; // This will now be derived from LocalDate
    private final StringProperty status; // New property for status

    // DateTimeFormatter for consistent date display
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /**
     * Constructor for ReservationDisplayData.
     * @param reservationId The reservation ID.
     * @param guestName The full name of the guest.
     * @param room The room type or number.
     * @param checkInLocalDate The check-in date as LocalDate.
     * @param status The current status of the booking.
     */
    public ReservationDisplayData(String reservationId, String guestName, String room, LocalDate checkInLocalDate, String status) {
        this.reservationId = new SimpleStringProperty(reservationId);
        this.guestName = new SimpleStringProperty(guestName);
        this.room = new SimpleStringProperty(room);
        // Format the LocalDate to a String for the StringProperty
        this.checkInDate = new SimpleStringProperty(checkInLocalDate != null ? checkInLocalDate.format(DISPLAY_DATE_FORMATTER) : "");
        this.status = new SimpleStringProperty(status);
    }

    // --- Property Getters ---
    public StringProperty reservationIdProperty() {
        return reservationId;
    }

    public StringProperty guestNameProperty() {
        return guestName;
    }

    public StringProperty roomProperty() {
        return room;
    }

    public StringProperty checkInDateProperty() {
        return checkInDate;
    }

    public StringProperty statusProperty() {
        return status;
    }

    // --- Standard Getters (for direct access, if needed) ---
    public String getReservationId() {
        return reservationId.get();
    }

    public String getGuestName() {
        return guestName.get();
    }

    public String getRoom() {
        return room.get();
    }

    public String getCheckInDate() {
        return checkInDate.get();
    }

    public String getStatus() {
        return status.get();
    }
}