package com.example.vehiclerentwebapp;

public class VehicleNode {
    Vehicle vehicle;
    VehicleNode next;

    public VehicleNode(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.next = null;
    }
}