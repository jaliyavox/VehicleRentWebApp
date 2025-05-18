package com.example.vehiclerentwebapp;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

@WebServlet(name = "VehicleServlet", urlPatterns = {"/vehicles"})
public class VehicleServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(VehicleServlet.class.getName());
    private VehicleLinkedList vehicleList;

    @Override
    public void init() throws ServletException {
        vehicleList = VehicleLinkedList.getInstance(getServletContext());
        LOGGER.info("VehicleServlet initialized with VehicleLinkedList");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sort = request.getParameter("sort");
        if ("priceAsc".equals(sort)) {
            vehicleList.sortByPricePerDay(true);
        } else if ("priceDesc".equals(sort)) {
            vehicleList.sortByPricePerDay(false);
        } else {
            vehicleList.sortByAvailability();
        }

        Vehicle[] allVehicles = vehicleList.toArray();
        LOGGER.info("Loaded " + allVehicles.length + " total vehicles");

        // Temporarily show all vehicles for debugging
        // Vehicle[] availableVehicles = Arrays.stream(allVehicles)
        //         .filter(Vehicle::isAvailable)
        //         .toArray(Vehicle[]::new);
        Vehicle[] availableVehicles = allVehicles; // Remove filter to debug
        LOGGER.info("Filtered to " + availableVehicles.length + " vehicles (available=true)");

        request.setAttribute("vehicles", availableVehicles);
        request.getRequestDispatcher("/vehicles.jsp").forward(request, response);
    }
}