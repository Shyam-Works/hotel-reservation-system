package com.example.project1.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;
public class BookingSession {
    private static final Logger LOGGER = Logger.getLogger(BookingSession.class.getName());
    private long id; // Add this field to store the actual DB ID of the booking
    private int numberOfGuests;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalPrice;
    private double discountAmount;
    private Map<String, Integer> selectedRoomsAndQuantities;
    private String selectedRoomsSummary;
    private double discountPercentage;
    private double finalPrice;
    // --- NEW FIELD: List of assigned rooms ---
    private List<Room> assignedRooms;

    private String guestFirstName;
    private String guestLastName;
    private String guestGender; // Corrected: This is the field that holds the gender string
    private String guestPhone;
    private String guestEmail;
    private int guestAge;
    private String guestStreet;
    private String guestAptSuite;
    private String guestCity;
    private String guestProvinceState;
    private String guestCountry;
    private String reservationId;
    private String status;

    public BookingSession() {
        this.selectedRoomsAndQuantities = new HashMap<>();
        this.totalPrice = 0.0;
        this.discountAmount = 0.0;
        this.assignedRooms = new ArrayList<>(); // Initialize the list
    }

    // --- GETTER/SETTER for id ---
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // --- GETTER/SETTER for assignedRooms ---


    // Existing Getters and Setters:

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Map<String, Integer> getSelectedRoomsAndQuantities() {
        return selectedRoomsAndQuantities;
    }

    public void setSelectedRoomsAndQuantities(Map<String, Integer> selectedRoomsAndQuantities) {
        this.selectedRoomsAndQuantities = selectedRoomsAndQuantities;
    }

    public String getSelectedRoomsSummary() {
        return selectedRoomsSummary;
    }

    public void setSelectedRoomsSummary(String selectedRoomsSummary) {
        this.selectedRoomsSummary = selectedRoomsSummary;
    }

    public String getGuestFirstName() {
        return guestFirstName;
    }

    public void setGuestFirstName(String guestFirstName) {
        this.guestFirstName = guestFirstName;
    }

    public String getGuestLastName() {
        return guestLastName;
    }

    public void setGuestLastName(String guestLastName) {
        this.guestLastName = guestLastName;
    }

    // --- CORRECTION MADE HERE ---
    public String getGuestGender() {
        return guestGender; // This now correctly returns the 'guestGender' field
    }

    public void setGuestGender(String guestGender) {
        this.guestGender = guestGender;
    }
    // --- END CORRECTION ---

    public String getGuestPhone() {
        return guestPhone;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public int getGuestAge() {
        return guestAge;
    }

    public void setGuestAge(int guestAge) {
        this.guestAge = guestAge;
    }

    public String getGuestStreet() {
        return guestStreet;
    }

    public void setGuestStreet(String guestStreet) {
        this.guestStreet = guestStreet;
    }

    public String getGuestAptSuite() {
        return guestAptSuite;
    }

    public void setGuestAptSuite(String guestAptSuite) {
        this.guestAptSuite = guestAptSuite;
    }

    public String getGuestCity() {
        return guestCity;
    }

    public void setGuestCity(String guestCity) {
        this.guestCity = guestCity;
    }

    public String getGuestProvinceState() {
        return guestProvinceState;
    }

    public void setGuestProvinceState(String guestProvinceState) {
        this.guestProvinceState = guestProvinceState;
    }

    public String getGuestCountry() {
        return guestCountry;
    }

    public void setGuestCountry(String guestCountry) {
        this.guestCountry = guestCountry;
    }

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
        this.status = "Confirmed";
    }
    public List<Room> getAssignedRooms() {
        return assignedRooms;
    }
    public double getDiscountPercentage() {
        return discountPercentage;
    }
    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    public void setAssignedRooms(List<Room> assignedRooms) {
        this.assignedRooms = assignedRooms;
        // You can also populate the old map for backward compatibility if needed
        if (assignedRooms != null) {
            Map<String, Integer> quantities = assignedRooms.stream()
                    .collect(Collectors.groupingBy(Room::getRoomType, Collectors.reducing(0, e -> 1, Integer::sum)));
            setSelectedRoomsAndQuantities(quantities);
        }
    }
    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public void parseSummaryToQuantities() {
        this.selectedRoomsAndQuantities = new HashMap<>();
        if (this.selectedRoomsSummary == null || this.selectedRoomsSummary.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Selected rooms summary is null or empty. Not parsing.");
            return;
        }

        LOGGER.log(Level.INFO, "Parsing room summary: " + this.selectedRoomsSummary);

        String[] rooms = this.selectedRoomsSummary.split(", ");
        for (String room : rooms) {
            String[] parts = room.split("x ");
            if (parts.length == 2) {
                try {
                    int quantity = Integer.parseInt(parts[0].trim());
                    String roomType = parts[1].trim();
                    this.selectedRoomsAndQuantities.put(roomType, quantity);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, "Error parsing room summary part: " + room, e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Unexpected format for room summary part: " + room);
            }
        }
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String checkInStr = (checkInDate != null) ? checkInDate.format(formatter) : "null";
        String checkOutStr = (checkOutDate != null) ? checkOutDate.format(formatter) : "null";

        return "BookingSession{" +
                "id=" + id +
                ", numberOfGuests=" + numberOfGuests +
                ", checkInDate='" + checkInStr + '\'' +
                ", checkOutDate='" + checkOutStr + '\'' +
                ", totalPrice=" + totalPrice +
                ", discountAmount=" + discountAmount +
                ", selectedRoomsAndQuantities=" + selectedRoomsAndQuantities +
                ", selectedRoomsSummary='" + selectedRoomsSummary + '\'' +
                ", assignedRooms=" + assignedRooms +
                ", guestFirstName='" + guestFirstName + '\'' +
                ", guestLastName='" + guestLastName + '\'' +
                ", guestGender='" + guestGender + '\'' + // Use guestGender field here
                ", guestPhone='" + guestPhone + '\'' +
                ", guestEmail='" + guestEmail + '\'' +
                ", guestAge=" + guestAge +
                ", guestStreet='" + guestStreet + '\'' +
                ", guestAptSuite='" + guestAptSuite + '\'' +
                ", guestCity='" + guestCity + '\'' +
                ", guestProvinceState='" + guestProvinceState + '\'' +
                ", guestCountry='" + guestCountry + '\'' +
                ", reservationId='" + reservationId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}