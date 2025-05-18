package com.example.vehiclerentwebapp.admin;

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

@WebServlet(name = "UserManagementServlet", urlPatterns = {"/user-management"})
public class UserManagementServlet extends HttpServlet {
    private static final String USERS_FILE = "/WEB-INF/users.txt";
    private static final String FALLBACK_FILE = "C:/Users/jaliy/Downloads/Compressed/apache-tomcat-11.0.6/webapps/ROOT/WEB-INF/users.txt";
    private static final Object fileLock = new Object();
    private static final Logger LOGGER = Logger.getLogger(UserManagementServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            LOGGER.warning("Unauthorized access to user management: No active session");
            response.sendRedirect("login.jsp?error=Please log in.");
            return;
        }

        String action = request.getParameter("action");
        String username = request.getParameter("username");
        String sessionUsername = (String) session.getAttribute("username");
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("isAdmin"));

        // Restrict non-admin users to updating their own details
        if (!isAdmin && !"update".equals(action)) {
            LOGGER.warning("Non-admin user " + sessionUsername + " attempted action: " + action);
            response.sendRedirect("http://localhost:8080/dashboard?error=Unauthorized action.");
            return;
        }
        if (!isAdmin && !sessionUsername.equals(username)) {
            LOGGER.warning("Non-admin user " + sessionUsername + " attempted to update user: " + username);
            response.sendRedirect("http://localhost:8080/dashboard?error=You can only update your own details.");
            return;
        }

        // Declare variables early to ensure scope
        String newUsername = isAdmin ? username : request.getParameter("newUsername");
        String newEmail = request.getParameter("email");
        String newFullName = request.getParameter("fullName");
        String newPassword = isAdmin ? null : request.getParameter("password");
        String filePath = getServletContext().getRealPath(USERS_FILE);
        LOGGER.info("getRealPath returned: " + filePath);
        if (filePath == null) {
            LOGGER.severe("getRealPath returned null, using fallback: " + FALLBACK_FILE);
            filePath = FALLBACK_FILE;
        }

        File file = new File(filePath);
        String error = null;

        synchronized (fileLock) {
            List<String> lines = new ArrayList<>();
            boolean userExists = false;
            boolean usernameTaken = false;
            boolean emailTaken = false;

            // Validate inputs for update
            if ("update".equals(action)) {
                if (isAdmin) {
                    // Admin updates only email and fullName
                    if (newEmail == null || !newEmail.contains("@")) {
                        error = "Valid email is required.";
                    } else if (newFullName == null || newFullName.trim().isEmpty()) {
                        error = "Full name is required.";
                    }
                } else {
                    // User updates username, email, fullName, and optionally password
                    if (newUsername == null || newUsername.trim().isEmpty()) {
                        error = "Username is required.";
                    } else if (newEmail == null || !newEmail.contains("@")) {
                        error = "Valid email is required.";
                    } else if (newFullName == null || newFullName.trim().isEmpty()) {
                        error = "Full name is required.";
                    }
                }
            }

            // Read and process users.txt
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 4 && parts[0].equals(username)) {
                        userExists = true;
                        if ("update".equals(action) && error == null) {
                            String updatedPassword = isAdmin ? parts[3] : (newPassword != null && !newPassword.trim().isEmpty() ? newPassword : parts[3]);
                            lines.add((isAdmin ? username : newUsername) + ":" + newEmail + ":" + newFullName + ":" + updatedPassword);
                            LOGGER.info("Updated user: " + username + (isAdmin ? "" : " to new username: " + newUsername));
                        } else if ("delete".equals(action)) {
                            continue; // Skip line for deletion
                        } else {
                            lines.add(line); // Keep original line
                        }
                    } else {
                        lines.add(line);
                        // Check for conflicts, excluding the user's own current username/email
                        if ("update".equals(action) && parts.length == 4 && !parts[0].equals(username)) {
                            if (newUsername != null && parts[0].equals(newUsername)) {
                                usernameTaken = true;
                            }
                            if (newEmail != null && parts[1].equals(newEmail)) {
                                emailTaken = true;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.severe("Error reading users.txt at " + filePath + ": " + e.getMessage());
                error = "Error accessing user data.";
            }

            // Validate user existence and conflicts
            if (error == null && ("update".equals(action) || "delete".equals(action)) && !userExists) {
                error = "User not found.";
                LOGGER.warning("Action failed: User " + username + " not found");
            }
            if (error == null && "update".equals(action) && usernameTaken) {
                error = "Username already in use by another user.";
                LOGGER.warning("Update failed for user " + username + ": Username " + newUsername + " already taken");
            }
            if (error == null && "update".equals(action) && emailTaken) {
                error = "Email already in use by another user.";
                LOGGER.warning("Update failed for user " + username + ": Email " + newEmail + " already taken");
            }

            // Write updated lines for update/delete
            if (error == null && ("update".equals(action) || "delete".equals(action))) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    for (String line : lines) {
                        writer.write(line);
                        writer.newLine();
                    }
                    LOGGER.info("User " + action + " successful: " + username);
                    // Update session attributes for user-initiated updates
                    if ("update".equals(action) && !isAdmin) {
                        session.setAttribute("username", newUsername);
                        session.setAttribute("email", newEmail);
                        session.setAttribute("fullName", newFullName);
                    }
                } catch (IOException e) {
                    LOGGER.severe("Error writing to users.txt at " + filePath + ": " + e.getMessage());
                    error = "Error saving user data.";
                }
            }
        }

        // Redirect with success or error message
        String redirectUrl = isAdmin ? "admin" : "http://localhost:8080/dashboard";
        if (error != null) {
            response.sendRedirect(redirectUrl + "?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
        } else {
            String successMessage = "User " + action + " successful: " + (isAdmin ? username : newUsername);
            response.sendRedirect(redirectUrl + "?success=" + java.net.URLEncoder.encode(successMessage, "UTF-8"));
        }
    }
}