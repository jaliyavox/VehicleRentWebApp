package com.example.vehiclerentwebapp.admin;

import com.example.vehiclerentwebapp.Booking;
import com.example.vehiclerentwebapp.Vehicle;
import com.example.vehiclerentwebapp.VehicleLinkedList;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(name = "BookingManagementServlet", urlPatterns = {"/booking-management"})
public class BookingManagementServlet extends HttpServlet {
    private static final String BOOKINGS_FILE = "/WEB-INF/bookings.txt";
    private static final Logger LOGGER = Logger.getLogger(BookingManagementServlet.class.getName());
    private VehicleLinkedList vehicleList;
    private static final Object fileLock = new Object();

    @Override
    public void init() throws ServletException {
        vehicleList = VehicleLinkedList.getInstance(getServletContext());
        LOGGER.info("BookingManagementServlet initialized with shared VehicleLinkedList");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            LOGGER.warning("Unauthorized access to booking management");
            response.sendRedirect("login.jsp?error=Unauthorized access.");
            return;
        }

        List<Booking> bookings = new ArrayList<>();
        String filePath = getServletContext().getRealPath(BOOKINGS_FILE);
        File file = new File(filePath);
        if (!file.exists()) {
            LOGGER.warning("Bookings file does not exist: " + filePath);
            request.setAttribute("bookings", bookings);
            request.getRequestDispatcher("/booking-management.jsp").forward(request, response);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(":", 8);
                if (parts.length == 8) {
                    Booking booking = new Booking(
                            parts[0], parts[1], parts[2], parts[3], parts[4],
                            Double.parseDouble(parts[5]), parts[6], parts[7]
                    );
                    bookings.add(booking);
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error reading bookings: " + e.getMessage());
        }

        request.setAttribute("bookings", bookings);
        request.setAttribute("vehicleList", vehicleList);
        request.getRequestDispatcher("/booking-management.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            LOGGER.warning("Unauthorized access to booking management");
            response.sendRedirect("login.jsp?error=Unauthorized access.");
            return;
        }

        String action = request.getParameter("action");
        String bookingId = request.getParameter("bookingId");
        String error = null;

        if ("approve".equals(action) || "reject".equals(action)) {
            String newStatus = "approve".equals(action) ? "confirmed" : "rejected";
            String filePath = getServletContext().getRealPath(BOOKINGS_FILE);
            List<String> updatedLines = new ArrayList<>();
            boolean bookingFound = false;

            synchronized (fileLock) {
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split(":", 8);
                        if (parts.length == 8 && parts[0].equals(bookingId)) {
                            bookingFound = true;
                            if (parts[6].equals("pending")) {
                                parts[6] = newStatus;
                                if ("confirmed".equals(newStatus)) {
                                    Vehicle vehicle = vehicleList.find(parts[2]);
                                    if (vehicle != null && vehicle.isAvailable()) {
                                        vehicle.setAvailable(false);
                                        vehicleList.update(vehicle);
                                    } else {
                                        error = "Vehicle not available for booking.";
                                        updatedLines.add(line);
                                        continue;
                                    }
                                }
                            } else {
                                error = "Booking is not pending.";
                            }
                            updatedLines.add(String.join(":", parts));
                        } else {
                            updatedLines.add(line);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.severe("Error reading bookings: " + e.getMessage());
                    error = "Error processing booking.";
                }

                if (error == null && bookingFound) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                        for (String line : updatedLines) {
                            writer.write(line);
                            writer.newLine();
                        }
                        LOGGER.info("Booking " + bookingId + " " + newStatus);
                    } catch (IOException e) {
                        LOGGER.severe("Error writing bookings: " + e.getMessage());
                        error = "Error saving booking status.";
                    }
                } else if (!bookingFound) {
                    error = "Booking not found.";
                }
            }
        } else {
            error = "Invalid action.";
        }

        if (error != null) {
            response.sendRedirect("booking-management?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
        } else {
            response.sendRedirect("booking-management?success=" + java.net.URLEncoder.encode("Booking " + action + "d successfully.", "UTF-8"));
        }
    }
}