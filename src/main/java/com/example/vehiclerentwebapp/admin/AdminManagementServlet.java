package com.example.vehiclerentwebapp.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(name = "AdminManagementServlet", urlPatterns = {"/admin-management"})
public class AdminManagementServlet extends HttpServlet {
    private static final String ADMIN_FILE = "/WEB-INF/admin.txt";
    private static final Object fileLock = new Object();
    private static final Logger LOGGER = Logger.getLogger(AdminManagementServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            LOGGER.warning("Unauthorized access to admin management");
            response.sendRedirect("login.jsp?error=Unauthorized access.");
            return;
        }

        String action = request.getParameter("action");
        String username = request.getParameter("username");
        String filePath = getServletContext().getRealPath(ADMIN_FILE);
        LOGGER.info("getRealPath returned: " + filePath);

        File file = new File(filePath);
        String error = null;

        synchronized (fileLock) {
            List<String> lines = new ArrayList<>();
            boolean adminExists = false;

            // Read existing admin entries
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 4 && parts[0].equals(username)) {
                        adminExists = true;
                        if ("update".equals(action)) {
                            String email = request.getParameter("email");
                            String fullName = request.getParameter("fullName");
                            if (email == null || !email.contains("@") || fullName == null || fullName.trim().isEmpty()) {
                                error = "Valid email and full name are required.";
                                LOGGER.warning("Update failed for admin " + username + ": " + error);
                                lines.add(line); // Keep original line on error
                            } else {
                                lines.add(username + ":" + email + ":" + fullName + ":" + parts[3]);
                                LOGGER.info("Updated admin: " + username);
                            }
                        } else if ("delete".equals(action)) {
                            // Skip this line to delete the admin
                            continue;
                        } else {
                            lines.add(line); // Keep original line for other actions
                        }
                    } else {
                        lines.add(line); // Keep non-matching lines
                    }
                }
            } catch (IOException e) {
                LOGGER.severe("Error reading admin.txt at " + filePath + ": " + e.getMessage());
                error = "Error accessing admin data.";
            }

            // Validate admin existence for update/delete
            if (error == null && ("delete".equals(action) || "update".equals(action)) && !adminExists) {
                error = "Admin not found.";
                LOGGER.warning("Action failed: Admin " + username + " not found");
            }

            // Write updated lines back to file for update/delete
            if (error == null && ("update".equals(action) || "delete".equals(action))) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    for (String line : lines) {
                        writer.write(line);
                        writer.newLine();
                    }
                    LOGGER.info("Admin " + action + " successful: " + username);
                } catch (IOException e) {
                    LOGGER.severe("Error writing to admin.txt at " + filePath + ": " + e.getMessage());
                    error = "Error saving admin data.";
                }
            }

            // Handle add action
            if ("add".equals(action)) {
                String email = request.getParameter("email");
                String fullName = request.getParameter("fullName");
                String password = request.getParameter("password");

                if (username == null || username.trim().isEmpty()) {
                    error = "Username is required.";
                } else if (email == null || !email.contains("@")) {
                    error = "Valid email is required.";
                } else if (fullName == null || fullName.trim().isEmpty()) {
                    error = "Full name is required.";
                } else if (password == null || password.trim().isEmpty()) {
                    error = "Password is required.";
                } else {
                    // Check for duplicates
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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

                    if (error == null) {
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                            writer.write(username + ":" + email + ":" + fullName + ":" + password);
                            writer.newLine();
                            LOGGER.info("Admin created successfully: " + username);
                        } catch (IOException e) {
                            LOGGER.severe("Error writing to admin.txt at " + filePath + ": " + e.getMessage());
                            error = "Error saving admin data.";
                        }
                    }
                }
            }
        }

        // Redirect with success or error message
        if (error != null) {
            response.sendRedirect("admin?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
        } else {
            String successMessage = "Admin " + action + " successful: " + username;
            response.sendRedirect("admin?success=" + java.net.URLEncoder.encode(successMessage, "UTF-8"));
        }
    }
}