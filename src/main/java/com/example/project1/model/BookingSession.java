package com.example.project1.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class BookingSession {
    private int numberOfGuests;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalPrice; // Calculated based on selected rooms and duration
    private Map<String, Integer> selectedRoomsAndQuantities; // Map of roomType -> quantity

    private String selectedRoomsSummary;

    // Guest Details
    private String guestFirstName;
    private String guestLastName;
    private String guestGender;
    private String guestPhone;
    private String guestEmail;
    private int guestAge;
    private String guestStreet;
    private String guestAptSuite;
    private String guestCity;
    private String guestProvinceState; // Ensure this matches usage in DAO
    private String guestCountry;
    private String status;
    // --- NEW FIELD: Reservation ID ---
    private String reservationId; // This will store the unique ID generated at confirmation

    public BookingSession() {
        this.selectedRoomsAndQuantities = new HashMap<>();
        this.totalPrice = 0.0;
    }

    // --- All your existing Getters and Setters go here ---
    // (Ensure you have getters and setters for all the fields above)

    public int getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Map<String, Integer> getSelectedRoomsAndQuantities() { return selectedRoomsAndQuantities; }
    public void setSelectedRoomsAndQuantities(Map<String, Integer> selectedRoomsAndQuantities) { this.selectedRoomsAndQuantities = selectedRoomsAndQuantities; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getGuestFirstName() { return guestFirstName; }
    public void setGuestFirstName(String guestFirstName) { this.guestFirstName = guestFirstName; }

    public String getGuestLastName() { return guestLastName; }
    public void setGuestLastName(String guestLastName) { this.guestLastName = guestLastName; }

    public String getGuestGender() { return guestGender; }
    public void setGuestGender(String guestGender) { this.guestGender = guestGender; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public int getGuestAge() { return guestAge; }
    public void setGuestAge(int guestAge) { this.guestAge = guestAge; }

    public String getGuestStreet() { return guestStreet; }
    public void setGuestStreet(String guestStreet) { this.guestStreet = guestStreet; }

    public String getGuestAptSuite() { return guestAptSuite; }
    public void setGuestAptSuite(String guestAptSuite) { this.guestAptSuite = guestAptSuite; }

    public String getGuestCity() { return guestCity; }
    public void setGuestCity(String guestCity) { this.guestCity = guestCity; }

    public String getGuestProvinceState() { return guestProvinceState; } // Ensure name matches field
    public void setGuestProvinceState(String guestProvinceState) { this.guestProvinceState = guestProvinceState; }

    public String getGuestCountry() { return guestCountry; }
    public void setGuestCountry(String guestCountry) { this.guestCountry = guestCountry; }


    public String getSelectedRoomsSummary() {
        return selectedRoomsSummary;
    }

    public void setSelectedRoomsSummary(String selectedRoomsSummary) { // <--- THIS IS THE MISSING METHOD
        this.selectedRoomsSummary = selectedRoomsSummary;
    }

    // --- NEW GETTER AND SETTER FOR RESERVATION ID ---
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String checkInStr = (checkInDate != null) ? checkInDate.format(formatter) : "null";
        String checkOutStr = (checkOutDate != null) ? checkOutDate.format(formatter) : "null";

        return "BookingSession{" +
                "numberOfGuests=" + numberOfGuests +
                ", checkInDate='" + checkInStr + '\'' +
                ", checkOutDate='" + checkOutStr + '\'' +
                ", selectedRoomsAndQuantities=" + selectedRoomsAndQuantities +
                ", totalPrice=" + totalPrice +
                ", guestFirstName='" + guestFirstName + '\'' +
                ", guestLastName='" + guestLastName + '\'' +
                ", guestGender='" + guestGender + '\'' +
                ", guestPhone='" + guestPhone + '\'' +
                ", guestEmail='" + guestEmail + '\'' +
                ", guestAge=" + guestAge +
                ", guestStreet='" + guestStreet + '\'' +
                ", guestAptSuite='" + guestAptSuite + '\'' +
                ", guestCity='" + guestCity + '\'' +
                ", guestProvinceState='" + guestProvinceState + '\'' +
                ", guestCountry='" + guestCountry + '\'' +
                ", reservationId='" + reservationId + '\'' + // Include reservationId in toString
                '}';
    }
}