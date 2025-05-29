package com.example.user_restaurant.models;

import com.google.firebase.Timestamp;

import java.util.Date;

public class ModelBooking {
    private String bookingId;
    private String restaurantId;
    private String customerId;
    private String Status;
    private String date;
    private String time;
    private String mealType;
    private int guestCount;
    private Timestamp bookingTimestamp;
    private Timestamp expiryTimestamp;

    // Default constructor (required for Firestore)
    public ModelBooking() {
    }

    // Constructor with all fields
    public ModelBooking(String bookingId, String restaurantId, String customerId, String Status, String date, String time,
                        String mealType, int guestCount, Timestamp bookingTimestamp, Timestamp expiryTimestamp) {
        this.bookingId = bookingId;
        this.restaurantId = restaurantId;
        this.customerId = customerId;
        this.Status = Status;
        this.date = date;
        this.time = time;
        this.mealType = mealType;
        this.guestCount = guestCount;
        this.bookingTimestamp = bookingTimestamp;
        this.expiryTimestamp = expiryTimestamp;
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
    public String getRestaurantId() {
        return restaurantId;
    }
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
    public String getCustomerId() {
        return customerId;
    }
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getMealType() {
        return mealType;
    }
    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
    public int getGuestCount() {
        return guestCount;
    }
    public void setGuestCount(int guestCount) {
        this.guestCount = guestCount;
    }
    public Timestamp getBookingTimestamp() {
        return bookingTimestamp;
    }
    public void setBookingTimestamp(Timestamp bookingTimestamp) {
        this.bookingTimestamp = bookingTimestamp;
    }
    public Timestamp getExpiryTimestamp() {
        return expiryTimestamp;
    }
    public void setExpiryTimestamp(Timestamp expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
    }
    public Date getBookingDate() {
        return expiryTimestamp != null ? expiryTimestamp.toDate() : new Date();
    }
}
