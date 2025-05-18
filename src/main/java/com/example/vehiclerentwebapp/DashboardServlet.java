package com.example.vehiclerentwebapp;

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

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
    private static final String BOOKINGS_FILE = "/WEB-INF/bookings.txt";
    private static final String NOTIFICATIONS_FILE = "/WEB-INF/notifications.txt";
    private static final Logger LOGGER = Logger.getLogger(DashboardServlet.class.getName());
    private static final Object fileLock = new Object();
    private VehicleLinkedList vehicleList;

    @Override
    public void init() throws ServletException {
        vehicleList = VehicleLinkedList.getInstance(getServletContext());
        LOGGER.info("DashboardServlet initialized with shared VehicleLinkedList");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            LOGGER.warning("Unauthorized access to dashboard");
            response.sendRedirect("login.jsp?error=Please log in.");
            return;
        }

        String username = (String) session.getAttribute("username");
        LOGGER.info("Loading dashboard for username: " + username);

        // Load notifications for the user dashboard
        List<Notification> notifications = getAllActiveNotifications();
        request.setAttribute("notifications", notifications);

        List<Booking> bookings = new ArrayList<>();
        String filePath = getServletContext().getRealPath(BOOKINGS_FILE);
        LOGGER.info("Reading bookings from: " + filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            LOGGER.warning("Bookings file does not exist: " + filePath);
            request.setAttribute("bookings", bookings);
            request.setAttribute("vehicleList", vehicleList);
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(":", 8);
                if (parts.length == 8) {
                    try {
                        Booking booking = new Booking(
                                parts[0], parts[1], parts[2], parts[3], parts[4],
                                Double.parseDouble(parts[5]), parts[6], parts[7]
                        );
                        bookings.add(booking);
                        LOGGER.info("Loaded booking: " + booking.getBookingId() + " for user: " + booking.getUsername());
                    } catch (NumberFormatException e) {
                        LOGGER.severe("Invalid totalPrice format at line " + lineNumber + ": " + line);
                    }
                } else {
                    LOGGER.severe("Invalid booking format at line " + lineNumber + ": " + line);
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error reading bookings: " + e.getMessage());
        }

        LOGGER.info("Total bookings loaded: " + bookings.size());
        request.setAttribute("bookings", bookings);
        request.setAttribute("vehicleList", vehicleList);
        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }

    private List<Notification> getAllActiveNotifications() {
        List<Notification> notifications = new ArrayList<>();
        String filePath = getServletContext().getRealPath(NOTIFICATIONS_FILE);
        synchronized (fileLock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Notification notification = Notification.fromString(line);
                    if (notification != null && notification.isActive()) {
                        notifications.add(notification);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return notifications;
    }
}