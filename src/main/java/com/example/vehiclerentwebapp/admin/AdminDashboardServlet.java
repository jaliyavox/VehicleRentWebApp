package com.example.vehiclerentwebapp.admin;

import com.example.vehiclerentwebapp.Notification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin-dashboard"})
public class AdminDashboardServlet extends HttpServlet {
    private static final String NOTIFICATIONS_FILE = "/WEB-INF/notifications.txt";
    private static final Object fileLock = new Object();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            response.sendRedirect("login.jsp?error=Unauthorized access.");
            return;
        }

        // Load all notifications for the admin dashboard
        List<Notification> notifications = getAllNotifications();
        request.setAttribute("notifications", notifications);

        request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
    }

    private List<Notification> getAllNotifications() {
        List<Notification> notifications = new ArrayList<>();
        String filePath = getServletContext().getRealPath(NOTIFICATIONS_FILE);
        synchronized (fileLock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Notification notification = Notification.fromString(line);
                    if (notification != null) {
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