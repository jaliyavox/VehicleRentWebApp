package com.example.vehiclerentwebapp;

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
import java.util.stream.Collectors;

@WebServlet(name = "NotificationServlet", urlPatterns = {"/notifications"})
public class NotificationServlet extends HttpServlet {
    private static final String NOTIFICATIONS_FILE = "notifications.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = Logger.getLogger(NotificationServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.jsp?error=Please login to access this page.");
            return;
        }

        String action = request.getParameter("action");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if ("edit".equals(action) && isAdmin != null && isAdmin) {
            String notificationId = request.getParameter("id");
            if (notificationId != null) {
                Notification notification = findNotificationById(notificationId);
                if (notification != null) {
                    request.setAttribute("notification", notification);
                    request.getRequestDispatcher("admin/edit-notification.jsp").forward(request, response);
                } else {
                    response.sendRedirect("admin?error=Notification not found.");
                }
            }
        } else {
            // Load active notifications for regular users
            List<Notification> notifications = loadNotifications().stream()
                    .filter(Notification::isActive)
                    .collect(Collectors.toList());
            request.setAttribute("notifications", notifications);
            request.getRequestDispatcher("notifications.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.jsp?error=Please login to access this page.");
            return;
        }

        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            response.sendRedirect("notifications?error=Admin access required.");
            return;
        }

        String action = request.getParameter("action");
        LOGGER.info("Received notification action: " + action);

        if (action == null) {
            response.sendRedirect("admin?error=Invalid action.");
            return;
        }

        switch (action) {
            case "create":
                createNotification(request, response);
                break;
            case "update":
                updateNotification(request, response);
                break;
            case "delete":
                deleteNotification(request, response);
                break;
            default:
                response.sendRedirect("admin?error=Invalid action.");
        }
    }

    private void createNotification(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("title");
        String message = request.getParameter("message");

        if (title == null || message == null || title.trim().isEmpty() || message.trim().isEmpty()) {
            response.sendRedirect("admin?error=Title and message are required.");
            return;
        }

        Notification notification = new Notification();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setTitle(title.trim());
        notification.setMessage(message.trim());
        notification.setCreatedAt(LocalDateTime.now().format(formatter));
        notification.setActive(true);

        List<Notification> notifications = loadNotifications();
        notifications.add(notification);
        saveNotifications(notifications);

        response.sendRedirect("admin?success=Notification created successfully.");
    }

    private void updateNotification(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String notificationId = request.getParameter("notificationId");
        String title = request.getParameter("title");
        String message = request.getParameter("message");
        boolean active = "on".equals(request.getParameter("active"));

        LOGGER.info("Updating notification - ID: " + notificationId + ", Title: " + title + ", Message: " + message + ", Active: " + active);

        if (notificationId == null || title == null || message == null ||
                title.trim().isEmpty() || message.trim().isEmpty()) {
            response.sendRedirect("admin?error=All fields are required.");
            return;
        }

        List<Notification> notifications = loadNotifications();
        LOGGER.info("Loaded " + notifications.size() + " notifications");

        boolean found = false;
        for (Notification notification : notifications) {
            if (notification.getNotificationId().equals(notificationId)) {
                notification.setTitle(title.trim());
                notification.setMessage(message.trim());
                notification.setActive(active);
                found = true;
                break;
            }
        }

        if (found) {
            saveNotifications(notifications);
            response.sendRedirect("admin?success=Notification updated successfully.");
        } else {
            LOGGER.warning("Notification not found with ID: " + notificationId);
            response.sendRedirect("admin?error=Notification not found.");
        }
    }

    private void deleteNotification(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String notificationId = request.getParameter("notificationId");
        LOGGER.info("Deleting notification with ID: " + notificationId);

        if (notificationId == null) {
            response.sendRedirect("admin?error=Notification ID is required.");
            return;
        }

        List<Notification> notifications = loadNotifications();
        LOGGER.info("Loaded " + notifications.size() + " notifications before deletion");

        boolean removed = notifications.removeIf(n -> n.getNotificationId().equals(notificationId));

        if (removed) {
            saveNotifications(notifications);
            response.sendRedirect("admin?success=Notification deleted successfully.");
        } else {
            LOGGER.warning("Notification not found for deletion with ID: " + notificationId);
            response.sendRedirect("admin?error=Notification not found.");
        }
    }

    private List<Notification> loadNotifications() throws IOException {
        List<Notification> notifications = new ArrayList<>();
        String webInfPath = getServletContext().getRealPath("/WEB-INF");
        File file = new File(webInfPath, NOTIFICATIONS_FILE);

        LOGGER.info("Loading notifications from: " + file.getAbsolutePath());

        if (!file.exists()) {
            LOGGER.info("Notifications file does not exist, creating new file");
            file.getParentFile().mkdirs();
            file.createNewFile();
            return notifications;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Notification notification = Notification.fromString(line);
                if (notification != null) {
                    notifications.add(notification);
                }
            }
        }

        LOGGER.info("Loaded " + notifications.size() + " notifications");
        return notifications;
    }

    private void saveNotifications(List<Notification> notifications) throws IOException {
        String webInfPath = getServletContext().getRealPath("/WEB-INF");
        File file = new File(webInfPath, NOTIFICATIONS_FILE);

        LOGGER.info("Saving " + notifications.size() + " notifications to: " + file.getAbsolutePath());

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (Notification notification : notifications) {
                writer.println(notification.toString());
            }
        }
    }

    private Notification findNotificationById(String notificationId) throws IOException {
        List<Notification> notifications = loadNotifications();
        return notifications.stream()
                .filter(n -> n.getNotificationId().equals(notificationId))
                .findFirst()
                .orElse(null);
    }
}