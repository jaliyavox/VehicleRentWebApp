package com.example.vehiclerentwebapp.admin;

import com.example.vehiclerentwebapp.Notification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@WebServlet(name = "NotificationServlet", urlPatterns = {"/notifications"})
public class NotificationServlet extends HttpServlet {
    private static final String NOTIFICATIONS_FILE = "/WEB-INF/notifications.txt";
    private static final Object fileLock = new Object();
    private static final Logger LOGGER = Logger.getLogger(NotificationServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.jsp?error=Please log in.");
            return;
        }

        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("isAdmin"));
        String action = request.getParameter("action");

        if ("edit".equals(action) && isAdmin) {
            String notificationId = request.getParameter("id");
            if (notificationId != null) {
                Notification notification = findNotification(notificationId);
                if (notification != null) {
                    request.setAttribute("notification", notification);
                    request.getRequestDispatcher("/admin/edit-notification.jsp").forward(request, response);
                    return;
                }
            }
            response.sendRedirect("admin?error=Notification not found.");
            return;
        }

        // Get all active notifications for users, or all notifications for admin
        List<Notification> notifications = getAllNotifications(isAdmin);
        request.setAttribute("notifications", notifications);
        
        if (isAdmin) {
            request.getRequestDispatcher("/admin/notifications.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/notifications.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            response.sendRedirect("login.jsp?error=Unauthorized access.");
            return;
        }

        String action = request.getParameter("action");
        String error = null;

        if ("create".equals(action)) {
            error = createNotification(request);
        } else if ("update".equals(action)) {
            error = updateNotification(request);
        } else if ("delete".equals(action)) {
            error = deleteNotification(request);
        }

        if (error != null) {
            response.sendRedirect("admin?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
        } else {
            response.sendRedirect("admin?success=Notification " + action + " successful.");
        }
    }

    private String createNotification(HttpServletRequest request) {
        String title = request.getParameter("title");
        String message = request.getParameter("message");

        if (title == null || title.trim().isEmpty()) {
            return "Title is required.";
        }
        if (message == null || message.trim().isEmpty()) {
            return "Message is required.";
        }

        String notificationId = UUID.randomUUID().toString();
        String createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Notification notification = new Notification(notificationId, title, message, createdAt, true);

        String filePath = getServletContext().getRealPath(NOTIFICATIONS_FILE);
        synchronized (fileLock) {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                    writer.write(notification.toString());
                    writer.newLine();
                }
                LOGGER.info("Notification created: " + notificationId);
                return null;
            } catch (IOException e) {
                LOGGER.severe("Error creating notification: " + e.getMessage());
                return "Error creating notification.";
            }
        }
    }

    private String updateNotification(HttpServletRequest request) {
        String notificationId = request.getParameter("notificationId");
        String title = request.getParameter("title");
        String message = request.getParameter("message");
        boolean isActive = "true".equals(request.getParameter("isActive"));

        if (notificationId == null || title == null || message == null) {
            return "Missing required fields.";
        }

        String filePath = getServletContext().getRealPath(NOTIFICATIONS_FILE);
        synchronized (fileLock) {
            try {
                List<String> lines = new ArrayList<>();
                boolean found = false;

                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith(notificationId + ":")) {
                            Notification notification = Notification.fromString(line);
                            if (notification != null) {
                                notification.setTitle(title);
                                notification.setMessage(message);
                                notification.setActive(isActive);
                                lines.add(notification.toString());
                                found = true;
                            }
                        } else {
                            lines.add(line);
                        }
                    }
                }

                if (!found) {
                    return "Notification not found.";
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    for (String line : lines) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
                LOGGER.info("Notification updated: " + notificationId);
                return null;
            } catch (IOException e) {
                LOGGER.severe("Error updating notification: " + e.getMessage());
                return "Error updating notification.";
            }
        }
    }

    private String deleteNotification(HttpServletRequest request) {
        String notificationId = request.getParameter("notificationId");
        if (notificationId == null) {
            return "Notification ID is required.";
        }

        String filePath = getServletContext().getRealPath(NOTIFICATIONS_FILE);
        synchronized (fileLock) {
            try {
                List<String> lines = new ArrayList<>();
                boolean found = false;

                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith(notificationId + ":")) {
                            lines.add(line);
                        } else {
                            found = true;
                        }
                    }
                }

                if (!found) {
                    return "Notification not found.";
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    for (String line : lines) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
                LOGGER.info("Notification deleted: " + notificationId);
                return null;
            } catch (IOException e) {
                LOGGER.severe("Error deleting notification: " + e.getMessage());
                return "Error deleting notification.";
            }
        }
    }

    private Notification findNotification(String notificationId) {
        String filePath = getServletContext().getRealPath(NOTIFICATIONS_FILE);
        synchronized (fileLock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(notificationId + ":")) {
                        return Notification.fromString(line);
                    }
                }
            } catch (IOException e) {
                LOGGER.severe("Error finding notification: " + e.getMessage());
            }
        }
        return null;
    }

    private List<Notification> getAllNotifications(boolean isAdmin) {
        List<Notification> notifications = new ArrayList<>();
        String filePath = getServletContext().getRealPath(NOTIFICATIONS_FILE);
        synchronized (fileLock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Notification notification = Notification.fromString(line);
                    if (notification != null && (isAdmin || notification.isActive())) {
                        notifications.add(notification);
                    }
                }
            } catch (IOException e) {
                LOGGER.severe("Error reading notifications: " + e.getMessage());
            }
        }
        return notifications;
    }
} 