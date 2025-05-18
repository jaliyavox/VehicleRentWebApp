package com.example.vehiclerentwebapp.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Servlet for displaying a list of admins
 */
@WebServlet(name = "AdminListServlet", urlPatterns = {"/admin-list"})
public class AdminListServlet extends HttpServlet {
    private static final String ADMIN_FILE = "/WEB-INF/admin.txt";
    private static final Logger LOGGER = Logger.getLogger(AdminListServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if user is logged in and is admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            LOGGER.warning("Unauthorized attempt to access admin list");
            response.sendRedirect("login.jsp?error=Unauthorized access. Please log in as admin.");
            return;
        }

        List<String[]> adminList = new ArrayList<>();
        String filePath = getServletContext().getRealPath(ADMIN_FILE);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] adminData = line.split(":");
                // Store only username, email, fullName (omit password)
                if (adminData.length >= 4) {
                    String[] safeAdminData = new String[3];
                    safeAdminData[0] = adminData[0]; // username
                    safeAdminData[1] = adminData[1]; // email
                    safeAdminData[2] = adminData[2]; // fullName
                    adminList.add(safeAdminData);
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error reading admin.txt at " + filePath + ": " + e.getMessage());
            request.setAttribute("error", "Error accessing admin data.");
        }

        request.setAttribute("adminList", adminList);
        request.getRequestDispatcher("/admin-management.jsp").forward(request, response);
    }
}