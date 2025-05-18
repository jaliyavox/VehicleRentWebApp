package com.example.vehiclerentwebapp;

public class Booking {
    private String bookingId;
    private String username;
    private String vehicleId;
    private String startDate;
    private String endDate;
    private double totalPrice;
    private String status;
    private String slipPath;

    public Booking(String bookingId, String username, String vehicleId, String startDate, String endDate,
                   double totalPrice, String status, String slipPath) {
        this.bookingId = bookingId;
        this.username = username;
        this.vehicleId = vehicleId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.slipPath = slipPath;
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSlipPath() { return slipPath; }
    public void setSlipPath(String slipPath) { this.slipPath = slipPath; }

    @Override
    public String toString() {
        return bookingId + ":" + username + ":" + vehicleId + ":" + startDate + ":" + endDate + ":" +
                totalPrice + ":" + status + ":" + slipPath;
    }
}