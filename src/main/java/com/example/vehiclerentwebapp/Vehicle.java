package com.example.vehiclerentwebapp;

public class Vehicle {
    private String vehicleId;
    private String type;
    private String brand;
    private String model;
    private int year;
    private double pricePerDay;
    private boolean available;

    public Vehicle(String vehicleId, String type, String brand, String model, int year, double pricePerDay, boolean available) {
        this.vehicleId = vehicleId;
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.pricePerDay = pricePerDay;
        this.available = available;
    }

    // Getters and Setters
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return vehicleId + ":" + type + ":" + brand + ":" + model + ":" + year + ":" + pricePerDay + ":" + available;
    }
}