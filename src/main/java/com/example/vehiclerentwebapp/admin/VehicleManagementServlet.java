package com.example.vehiclerentwebapp.admin;

import com.example.vehiclerentwebapp.Vehicle;
import com.example.vehiclerentwebapp.VehicleLinkedList;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

@WebServlet(name = "VehicleManagementServlet", urlPatterns = {"/vehicle-management"})
public class VehicleManagementServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(VehicleManagementServlet.class.getName());
    private VehicleLinkedList vehicleList;

    @Override
    public void init() throws ServletException {
        vehicleList = VehicleLinkedList.getInstance(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            LOGGER.warning("Unauthorized access to vehicle management");
            response.sendRedirect("login.jsp?error=Unauthorized access.");
            return;
        }

        String sort = request.getParameter("sort");
        if ("priceAsc".equals(sort)) {
            vehicleList.sortByPricePerDay(true);
        } else if ("priceDesc".equals(sort)) {
            vehicleList.sortByPricePerDay(false);
        } else {
            vehicleList.sortByAvailability();
        }

        request.setAttribute("vehicles", vehicleList.toArray());
        request.getRequestDispatcher("/vehicle-management.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            LOGGER.warning("Unauthorized access to vehicle management");
            response.sendRedirect("login.jsp?error=Unauthorized access.");
            return;
        }

        String action = request.getParameter("action");
        String error = null;

        if ("add".equals(action)) {
            try {
                String type = request.getParameter("type");
                String brand = request.getParameter("brand");
                String model = request.getParameter("model");
                int year = Integer.parseInt(request.getParameter("year"));
                double pricePerDay = Double.parseDouble(request.getParameter("pricePerDay"));
                boolean available = "true".equalsIgnoreCase(request.getParameter("available"));

                if (type == null || type.isEmpty() || brand == null || brand.isEmpty() ||
                        model == null || model.isEmpty() || year < 1900 || year > 2025 || pricePerDay <= 0) {
                    error = "Invalid vehicle details.";
                } else {
                    Vehicle vehicle = new Vehicle(UUID.randomUUID().toString(), type, brand, model, year, pricePerDay, available);
                    vehicleList.add(vehicle);
                    LOGGER.info("Added vehicle: " + vehicle.getVehicleId() + ", available: " + available);
                }
            } catch (NumberFormatException e) {
                error = "Invalid number format for year or price.";
            }
        } else if ("update".equals(action)) {
            try {
                String vehicleId = request.getParameter("vehicleId");
                String type = request.getParameter("type");
                String brand = request.getParameter("brand");
                String model = request.getParameter("model");
                int year = Integer.parseInt(request.getParameter("year"));
                double pricePerDay = Double.parseDouble(request.getParameter("pricePerDay"));
                String availableParam = request.getParameter("available");
                boolean available = "true".equalsIgnoreCase(availableParam);

                if (type == null || type.isEmpty() || brand == null || brand.isEmpty() ||
                        model == null || model.isEmpty() || year < 1900 || year > 2025 || pricePerDay <= 0) {
                    error = "Invalid vehicle details.";
                } else {
                    Vehicle vehicle = new Vehicle(vehicleId, type, brand, model, year, pricePerDay, available);
                    vehicleList.update(vehicle);
                    LOGGER.info("Updated vehicle: " + vehicleId + ", available: " + available);
                }
            } catch (NumberFormatException e) {
                error = "Invalid number format for year or price.";
            }
        } else if ("delete".equals(action)) {
            String vehicleId = request.getParameter("vehicleId");
            vehicleList.remove(vehicleId);
            LOGGER.info("Deleted vehicle: " + vehicleId);
        } else {
            error = "Invalid action.";
        }

        if (error != null) {
            response.sendRedirect("vehicle-management?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
        } else {
            response.sendRedirect("vehicle-management?success=" + java.net.URLEncoder.encode("Vehicle " + action + " successful.", "UTF-8"));
        }
    }
}