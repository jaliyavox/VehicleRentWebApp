package com.example.vehiclerentwebapp.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet(name = "AddAdminServlet", urlPatterns = {"/add-admin"})
public class AddAdminServlet extends HttpServlet {
    private static final String ADMIN_FILE = "/WEB-INF/admin.txt";
    private static final Object fileLock = new Object();
    private static final Logger LOGGER = Logger.getLogger(AddAdminServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if user is logged in and is admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            LOGGER.warning("Unauthorized attempt to add admin");
            response.sendRedirect("login.jsp?error=Unauthorized access. Please log in as admin.");
            return;
        }

        // Get form data
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        LOGGER.info("Admin creation attempt for username: " + username);

        // Validate input
        String error = null;
        if (username == null || username.trim().isEmpty()) {
            error = "Username is required.";
        } else if (email == null || !email.contains("@")) {
            error = "Valid email is required.";
        } else if (fullName == null || fullName.trim().isEmpty()) {
            error = "Full name is required.";
        } else if (password == null || password.trim().isEmpty()) {
            error = "Password is required.";
        } else if (!password.equals(confirmPassword)) {
            error = "Passwords do not match.";
        }

        // Check for duplicate username or email
        if (error == null) {
            String filePath = getServletContext().getRealPath(ADMIN_FILE);
            LOGGER.info("Checking admin.txt at: " + filePath);

            synchronized (fileLock) {
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length >= 2) {
                            if (parts[0].equals(username)) {
                                error = "Admin username already exists.";
                                break;
                            }
                            if (parts[1].equals(email)) {
                                error = "Admin email already exists.";
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.severe("Error reading admin.txt at " + filePath + ": " + e.getMessage());
                    error = "Error accessing admin data.";
                }
            }
        }

        // If there's an error, redirect back with the error
        if (error != null) {
            LOGGER.warning("Admin creation failed: " + error);
            response.sendRedirect("admin-management.jsp?error=" + error);
            return;
        }

        // Save admin to file
        String filePath = getServletContext().getRealPath(ADMIN_FILE);
        synchronized (fileLock) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write(username + ":" + email + ":" + fullName + ":" + password);
                writer.newLine();
                LOGGER.info("Admin created successfully: " + username);
            } catch (IOException e) {
                LOGGER.severe("Error writing to admin.txt at " + filePath + ": " + e.getMessage());
                response.sendRedirect("admin-management.jsp?error=Error saving admin data.");
                return;
            }
        }

        // Redirect with success message
        response.sendRedirect("admin-management.jsp?success=Admin " + username + " created successfully.");
    }
}