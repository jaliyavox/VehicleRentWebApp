package com.example.vehiclerentwebapp;

import jakarta.servlet.ServletContext;

import java.io.*;
import java.util.UUID;
import java.util.logging.Logger;

public class VehicleLinkedList {
    private VehicleNode head;
    private static final Logger LOGGER = Logger.getLogger(VehicleLinkedList.class.getName());
    private final String vehiclesFile;
    private static final Object fileLock = new Object();

    // Singleton-like access
    public static VehicleLinkedList getInstance(ServletContext context) {
        synchronized (fileLock) {
            VehicleLinkedList instance = (VehicleLinkedList) context.getAttribute("vehicleList");
            if (instance == null) {
                String filePath = context.getRealPath("/WEB-INF/vehicles.txt");
                instance = new VehicleLinkedList(filePath);
                context.setAttribute("vehicleList", instance);
                LOGGER.info("Created new VehicleLinkedList instance for: " + filePath);
            }
            return instance;
        }
    }

    private VehicleLinkedList(String vehiclesFile) {
        this.vehiclesFile = vehiclesFile;
        this.head = null;
        loadFromFile();
    }

    public void add(Vehicle vehicle) {
        VehicleNode newNode = new VehicleNode(vehicle);
        if (head == null) {
            head = newNode;
        } else {
            VehicleNode current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        saveToFile();
        LOGGER.info("Added vehicle: " + vehicle.getVehicleId());
    }

    public void remove(String vehicleId) {
        if (head == null) return;
        if (head.vehicle.getVehicleId().equals(vehicleId)) {
            head = head.next;
            saveToFile();
            return;
        }
        VehicleNode current = head;
        while (current.next != null && !current.next.vehicle.getVehicleId().equals(vehicleId)) {
            current = current.next;
        }
        if (current.next != null) {
            current.next = current.next.next;
            saveToFile();
        }
        LOGGER.info("Removed vehicle: " + vehicleId);
    }

    public void update(Vehicle updatedVehicle) {
        VehicleNode current = head;
        while (current != null) {
            if (current.vehicle.getVehicleId().equals(updatedVehicle.getVehicleId())) {
                current.vehicle = updatedVehicle;
                saveToFile();
                LOGGER.info("Updated vehicle: " + updatedVehicle.getVehicleId());
                return;
            }
            current = current.next;
        }
    }

    public Vehicle find(String vehicleId) {
        VehicleNode current = head;
        while (current != null) {
            if (current.vehicle.getVehicleId().equals(vehicleId)) {
                return current.vehicle;
            }
            current = current.next;
        }
        return null;
    }

    public void sortByAvailability() {
        if (head == null || head.next == null) return;
        VehicleNode current;
        VehicleNode index;
        Vehicle temp;
        for (current = head; current.next != null; current = current.next) {
            for (index = current.next; index != null; index = index.next) {
                if (!current.vehicle.isAvailable() && index.vehicle.isAvailable()) {
                    temp = current.vehicle;
                    current.vehicle = index.vehicle;
                    index.vehicle = temp;
                }
            }
        }
        LOGGER.info("Sorted vehicles by availability");
    }

    public void sortByPricePerDay(boolean ascending) {
        if (head == null || head.next == null) return;
        VehicleNode current;
        VehicleNode index;
        Vehicle temp;
        for (current = head; current.next != null; current = current.next) {
            for (index = current.next; index != null; index = index.next) {
                double price1 = current.vehicle.getPricePerDay();
                double price2 = index.vehicle.getPricePerDay();
                if ((ascending && price1 > price2) || (!ascending && price1 < price2)) {
                    temp = current.vehicle;
                    current.vehicle = index.vehicle;
                    index.vehicle = temp;
                }
            }
        }
        LOGGER.info("Sorted vehicles by price, ascending: " + ascending);
    }

    public Vehicle[] toArray() {
        java.util.List<Vehicle> list = new java.util.ArrayList<>();
        VehicleNode current = head;
        while (current != null) {
            list.add(current.vehicle);
            current = current.next;
        }
        LOGGER.info("Returning " + list.size() + " vehicles");
        return list.toArray(new Vehicle[0]);
    }

    private void loadFromFile() {
        File file = new File(vehiclesFile);
        LOGGER.info("Loading vehicles from: " + vehiclesFile);
        if (!file.exists()) {
            LOGGER.warning("Vehicles file does not exist: " + vehiclesFile);
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(":", 7);
                if (parts.length == 7) {
                    Vehicle vehicle = new Vehicle(
                            parts[0], parts[1], parts[2], parts[3],
                            Integer.parseInt(parts[4]), Double.parseDouble(parts[5]), Boolean.parseBoolean(parts[6])
                    );
                    add(vehicle);
                    count++;
                }
            }
            LOGGER.info("Loaded " + count + " vehicles from file");
        } catch (IOException e) {
            LOGGER.severe("Error loading vehicles.txt: " + e.getMessage());
        }
    }

    private void saveToFile() {
        synchronized (fileLock) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(vehiclesFile))) {
                VehicleNode current = head;
                int count = 0;
                while (current != null) {
                    writer.write(current.vehicle.toString());
                    writer.newLine();
                    current = current.next;
                    count++;
                }
                LOGGER.info("Saved " + count + " vehicles to file: " + vehiclesFile);
            } catch (IOException e) {
                LOGGER.severe("Error saving vehicles.txt: " + e.getMessage());
            }
        }
    }
}