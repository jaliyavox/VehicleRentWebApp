package com.example.vehiclerentwebapp;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {
    private static final String USERS_FILE = "users.txt";
    private static final Object fileLock = new Object();
    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Serving register.jsp");
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        String password = request.getParameter("password");

        LOGGER.info("Registration attempt: username=" + username);

        String error = null;
        if (username == null || username.trim().isEmpty()) {
            error = "Username is required.";
        } else if (email == null || !email.contains("@")) {
            error = "Valid email is required.";
        } else if (fullName == null || fullName.trim().isEmpty()) {
            error = "Full name is required.";
        } else if (password == null || password.trim().isEmpty()) {
            error = "Password is required.";
        }

        if (error != null) {
            LOGGER.warning("Registration validation failed: " + error);
            request.setAttribute("error", error);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // Get the absolute path to the WEB-INF directory
        String webInfPath = getServletContext().getRealPath("/WEB-INF");
        Path usersFilePath = Paths.get(webInfPath, USERS_FILE);
        
        LOGGER.info("Users file path: " + usersFilePath);

        synchronized (fileLock) {
            try {
                // Create WEB-INF directory if it doesn't exist
                Files.createDirectories(usersFilePath.getParent());
                
                // Create the file if it doesn't exist
                if (!Files.exists(usersFilePath)) {
                    Files.createFile(usersFilePath);
                    LOGGER.info("Created users.txt at: " + usersFilePath);
                }

                // Check if username or email already exists
                if (Files.exists(usersFilePath) && Files.size(usersFilePath) > 0) {
                    try (BufferedReader reader = Files.newBufferedReader(usersFilePath)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split(":");
                            if (parts.length >= 2) {
                                if (parts[0].equals(username)) {
                                    error = "Username already exists.";
                                    break;
                                }
                                if (parts[1].equals(email)) {
                                    error = "Email already exists.";
                                    break;
                                }
                            }
                        }
                    }
                }

                if (error != null) {
                    LOGGER.warning("Registration failed: " + error);
                    request.setAttribute("error", error);
                    request.getRequestDispatcher("/register.jsp").forward(request, response);
                    return;
                }

                // Append the new user
                try (BufferedWriter writer = Files.newBufferedWriter(usersFilePath, 
                        java.nio.file.StandardOpenOption.APPEND, 
                        java.nio.file.StandardOpenOption.CREATE)) {
                    writer.write(username + ":" + email + ":" + fullName + ":" + password);
                    writer.newLine();
                    LOGGER.info("User registered successfully: " + username);
                }

                // Redirect to login page with success message
                response.sendRedirect("login.jsp?message=Registration successful. Please log in.");

            } catch (IOException e) {
                LOGGER.severe("Error during registration: " + e.getMessage());
                error = "Error during registration. Please try again.";
                request.setAttribute("error", error);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
            }
        }
    }
}