package com.example.vehiclerentwebapp;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@WebServlet(name = "BookingServlet", urlPatterns = {"/bookings"})
@MultipartConfig(maxFileSize=5242880) // 5MB
public class BookingServlet extends HttpServlet {
    private static final String VEHICLES_FILE = "/WEB-INF/vehicles.txt";
    private static final String BOOKINGS_FILE = "/WEB-INF/bookings.txt";
    private static final Logger LOGGER = Logger.getLogger(BookingServlet.class.getName());
    private VehicleLinkedList vehicleList;
    private static final Object fileLock = new Object();

    @Override
    public void init() throws ServletException {
        vehicleList = VehicleLinkedList.getInstance(getServletContext());
        LOGGER.info("BookingServlet initialized with shared VehicleLinkedList");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.jsp?error=Please log in.");
            return;
        }

        String action = request.getParameter("action");
        String vehicleId = request.getParameter("vehicleId");
        String bookingId = request.getParameter("bookingId");
        String username = (String) session.getAttribute("username");

        if ("edit".equals(action) && bookingId != null) {
            // Load booking for editing
            String filePath = getServletContext().getRealPath(BOOKINGS_FILE);
            Booking booking = null;
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(":", 8);
                    if (parts.length == 8 && parts[0].equals(bookingId) && parts[1].equals(username) && parts[6].equals("pending")) {
                        booking = new Booking(
                                parts[0], parts[1], parts[2], parts[3], parts[4],
                                Double.parseDouble(parts[5]), parts[6], parts[7]
                        );
                        break;
                    }
                }
            } catch (IOException e) {
                LOGGER.severe("Error reading bookings: " + e.getMessage());
                response.sendRedirect("dashboard?error=" + java.net.URLEncoder.encode("Error loading booking.", "UTF-8"));
                return;
            }

            if (booking == null) {
                response.sendRedirect("dashboard?error=" + java.net.URLEncoder.encode("Booking not found or not editable.", "UTF-8"));
                return;
            }

            Vehicle vehicle = vehicleList.find(booking.getVehicleId());
            if (vehicle == null) {
                response.sendRedirect("dashboard?error=" + java.net.URLEncoder.encode("Vehicle not found.", "UTF-8"));
                return;
            }

            request.setAttribute("booking", booking);
            request.setAttribute("vehicle", vehicle);
            request.getRequestDispatcher("/edit-booking.jsp").forward(request, response);
        } else if (vehicleId != null) {
            Vehicle vehicle = vehicleList.find(vehicleId);
            if (vehicle != null && vehicle.isAvailable()) {
                request.setAttribute("vehicle", vehicle);
                request.getRequestDispatcher("/booking.jsp").forward(request, response);
            } else {
                response.sendRedirect("vehicles?error=Vehicle not available.");
            }
        } else {
            request.getRequestDispatcher("/booking.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.jsp?error=Please log in.");
            return;
        }

        String action = request.getParameter("action");
        String username = (String) session.getAttribute("username");
        LOGGER.info("BookingServlet: Processing action: " + action + " for username: " + username);
        String error = null;

        if ("book".equals(action)) {
            String vehicleId = request.getParameter("vehicleId");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            Part filePart = request.getPart("paymentSlip");

            Vehicle vehicle = vehicleList.find(vehicleId);
            if (vehicle == null || !vehicle.isAvailable()) {
                error = "Vehicle not available.";
            } else {
                try {
                    LocalDate start = LocalDate.parse(startDate);
                    LocalDate end = LocalDate.parse(endDate);
                    LocalDate today = LocalDate.now();

                    if (start.isBefore(today) || end.isBefore(start)) {
                        error = "Invalid date range.";
                    } else if (filePart == null || !filePart.getContentType().equals("image/jpeg") || filePart.getSize() > 5242880) {
                        error = "Invalid payment slip. Must be JPEG, max 5MB.";
                    } else {
                        String filePath = getServletContext().getRealPath(BOOKINGS_FILE);
                        synchronized (fileLock) {
                            // Check for overlapping bookings
                            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (line.trim().isEmpty()) continue;
                                    String[] parts = line.split(":", 8);
                                    if (parts.length == 8 && parts[2].equals(vehicleId) &&
                                            (parts[6].equals("pending") || parts[6].equals("confirmed"))) {
                                        LocalDate bookedStart = LocalDate.parse(parts[3]);
                                        LocalDate bookedEnd = LocalDate.parse(parts[4]);
                                        if (!(end.isBefore(bookedStart) || start.isAfter(bookedEnd))) {
                                            error = "Vehicle booked for selected dates.";
                                            break;
                                        }
                                    }
                                }
                            }

                            if (error == null) {
                                String bookingId = UUID.randomUUID().toString();
                                long days = ChronoUnit.DAYS.between(start, end);
                                days = days < 1 ? 1 : days;
                                double totalPrice = days * vehicle.getPricePerDay();
                                String uploadPath = getServletContext().getRealPath("/WEB-INF/uploads");
                                File uploadDir = new File(uploadPath);
                                if (!uploadDir.exists()) uploadDir.mkdir();
                                String fileName = "slip_" + bookingId + ".jpg";
                                String slipPath = uploadPath + "/" + fileName;

                                // Save the uploaded file
                                filePart.write(slipPath);

                                Booking booking = new Booking(bookingId, username, vehicleId, startDate, endDate,
                                        totalPrice, "pending", fileName);
                                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                                    writer.write(booking.toString());
                                    writer.newLine();
                                }
                                LOGGER.info("Booking created: " + bookingId + " for username: " + username);
                            }
                        }
                    }
                } catch (Exception e) {
                    error = "Error processing booking: " + e.getMessage();
                    LOGGER.severe("Booking error: " + e.getMessage());
                }
            }
            if (error != null) {
                response.sendRedirect("bookings?vehicleId=" + vehicleId + "&error=" +
                        java.net.URLEncoder.encode(error, "UTF-8"));
            } else {
                response.sendRedirect("bookings?success=" + java.net.URLEncoder.encode("Booking submitted. Awaiting approval.", "UTF-8"));
            }
        } else if ("edit".equals(action)) {
            String bookingId = request.getParameter("bookingId");
            String vehicleId = request.getParameter("vehicleId");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            Part filePart = request.getPart("paymentSlip");

            Vehicle vehicle = vehicleList.find(vehicleId);
            if (vehicle == null || !vehicle.isAvailable()) {
                error = "Vehicle not available.";
            } else {
                try {
                    LocalDate start = LocalDate.parse(startDate);
                    LocalDate end = LocalDate.parse(endDate);
                    LocalDate today = LocalDate.now();

                    if (start.isBefore(today) || end.isBefore(start)) {
                        error = "Invalid date range.";
                    } else if (filePart != null && filePart.getSize() > 0 &&
                            (!filePart.getContentType().equals("image/jpeg") || filePart.getSize() > 5242880)) {
                        error = "Invalid payment slip. Must be JPEG, max 5MB.";
                    } else {
                        String filePath = getServletContext().getRealPath(BOOKINGS_FILE);
                        synchronized (fileLock) {
                            List<String> updatedLines = new ArrayList<>();
                            boolean bookingFound = false;

                            // Check for overlapping bookings, excluding the current booking
                            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (line.trim().isEmpty()) continue;
                                    String[] parts = line.split(":", 8);
                                    if (parts.length == 8 && parts[2].equals(vehicleId) &&
                                            (parts[6].equals("pending") || parts[6].equals("confirmed")) &&
                                            !parts[0].equals(bookingId)) {
                                        LocalDate bookedStart = LocalDate.parse(parts[3]);
                                        LocalDate bookedEnd = LocalDate.parse(parts[4]);
                                        if (!(end.isBefore(bookedStart) || start.isAfter(bookedEnd))) {
                                            error = "Vehicle booked for selected dates.";
                                            break;
                                        }
                                    }
                                    updatedLines.add(line);
                                }
                            }

                            if (error == null) {
                                updatedLines.clear();
                                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        if (line.trim().isEmpty()) continue;
                                        String[] parts = line.split(":", 8);
                                        if (parts.length == 8 && parts[0].equals(bookingId) && parts[1].equals(username) && parts[6].equals("pending")) {
                                            bookingFound = true;
                                            long days = ChronoUnit.DAYS.between(start, end);
                                            days = days < 1 ? 1 : days;
                                            double totalPrice = days * vehicle.getPricePerDay();
                                            String slipPath = parts[7];
                                            if (filePart != null && filePart.getSize() > 0) {
                                                String fileName = "slip_" + bookingId + ".jpg";
                                                slipPath = "/WEB-INF/uploads/" + fileName;
                                                filePart.write(fileName);
                                            }
                                            updatedLines.add(bookingId + ":" + username + ":" + vehicleId + ":" + startDate + ":" +
                                                    endDate + ":" + totalPrice + ":pending:" + slipPath);
                                        } else {
                                            updatedLines.add(line);
                                        }
                                    }
                                }

                                if (bookingFound) {
                                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                                        for (String line : updatedLines) {
                                            writer.write(line);
                                            writer.newLine();
                                        }
                                        LOGGER.info("Booking " + bookingId + " updated for username: " + username);
                                    }
                                } else {
                                    error = "Booking not found or not editable.";
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    error = "Error updating booking: " + e.getMessage();
                    LOGGER.severe("Booking update error: " + e.getMessage());
                }
            }
            if (error != null) {
                response.sendRedirect("bookings?action=edit&bookingId=" + bookingId + "&error=" +
                        java.net.URLEncoder.encode(error, "UTF-8"));
            } else {
                response.sendRedirect("dashboard?success=" + java.net.URLEncoder.encode("Booking updated successfully.", "UTF-8"));
            }
        } else if ("delete".equals(action)) {
            String bookingId = request.getParameter("bookingId");
            String filePath = getServletContext().getRealPath(BOOKINGS_FILE);
            synchronized (fileLock) {
                List<String> updatedLines = new ArrayList<>();
                boolean bookingFound = false;

                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split(":", 8);
                        if (parts.length == 8 && parts[0].equals(bookingId) && parts[1].equals(username) && parts[6].equals("pending")) {
                            bookingFound = true;
                            continue; // Skip this booking to delete it
                        }
                        updatedLines.add(line);
                    }
                } catch (IOException e) {
                    LOGGER.severe("Error reading bookings: " + e.getMessage());
                    error = "Error deleting booking.";
                }

                if (bookingFound && error == null) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                        for (String line : updatedLines) {
                            writer.write(line);
                            writer.newLine();
                        }
                        LOGGER.info("Booking " + bookingId + " deleted for username: " + username);
                    } catch (IOException e) {
                        LOGGER.severe("Error writing bookings: " + e.getMessage());
                        error = "Error deleting booking.";
                    }
                } else if (!bookingFound) {
                    error = "Booking not found or not deletable.";
                }
            }
            if (error != null) {
                response.sendRedirect("dashboard?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
            } else {
                response.sendRedirect("dashboard?success=" + java.net.URLEncoder.encode("Booking deleted successfully.", "UTF-8"));
            }
        } else {
            error = "Invalid action.";
            response.sendRedirect("dashboard?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
        }
    }
}