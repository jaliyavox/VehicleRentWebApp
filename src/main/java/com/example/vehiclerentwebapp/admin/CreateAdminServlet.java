package com.example.vehiclerentwebapp.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet(name = "CreateAdminServlet", urlPatterns = {"/create-admin"})
public class CreateAdminServlet extends HttpServlet {
    private static final String ADMIN_FILE = "/WEB-INF/admin.txt";
    private static final String FALLBACK_FILE = "C:/Users/jaliy/Downloads/Compressed/apache-tomcat-11.0.6/webapps/ROOT/WEB-INF/admin.txt";
    private static final Object fileLock = new Object();
    private static final Logger LOGGER = Logger.getLogger(CreateAdminServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = "admin";
        String email = "admin@example.com";
        String fullName = "Admin User";
        String password = "admin123";

        LOGGER.info("Attempting to create admin: username=" + username);

        String filePath = getServletContext().getRealPath(ADMIN_FILE);
        LOGGER.info("getRealPath returned: " + filePath);
        if (filePath == null) {
            LOGGER.severe("getRealPath returned null, using fallback: " + FALLBACK_FILE);
            filePath = FALLBACK_FILE;
        }

        File file = new File(filePath);
        String error = null;

        synchronized (fileLock) {
            if (!file.exists()) {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    LOGGER.info("Created admin.txt at: " + filePath);
                } catch (IOException e) {
                    LOGGER.severe("Failed to create admin.txt at " + filePath + ": " + e.getMessage());
                    error = "Cannot create admin data file.";
                }
            } else {
                LOGGER.info("admin.txt exists at: " + filePath);
            }

            if (error == null && file.exists() && !file.canWrite()) {
                LOGGER.severe("admin.txt is not writable at: " + filePath);
                error = "Cannot write to admin data file.";
            }

            if (error == null) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length >= 1 && parts[0].equals(username)) {
                            error = "Admin username already exists.";
                            break;
                        }
                    }
                } catch (IOException e) {
                    LOGGER.severe("Error reading admin.txt at " + filePath + ": " + e.getMessage());
                    error = "Error accessing admin data.";
                }
            }

            if (error == null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                    writer.write(username + ":" + email + ":" + fullName + ":" + password);
                    writer.newLine();
                    LOGGER.info("Admin created successfully: " + username + " at " + filePath);
                } catch (IOException e) {
                    LOGGER.severe("Error writing to admin.txt at " + filePath + ": " + e.getMessage());
                    error = "Error saving admin data.";
                }
            }
        }

        response.setContentType("text/plain");
        if (error != null) {
            response.getWriter().write("Failed to create admin: " + error);
        } else {
            response.getWriter().write("Admin created successfully. Log in at /login.jsp");
        }
    }
}