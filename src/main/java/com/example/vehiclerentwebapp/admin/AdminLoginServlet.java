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
import java.util.logging.Logger;

@WebServlet(name = "AdminLoginServlet", urlPatterns = {"/admin-login"})
public class AdminLoginServlet extends HttpServlet {
    private static final String ADMIN_FILE = "/WEB-INF/admin.txt";
    private static final Object fileLock = new Object();
    private static final Logger LOGGER = Logger.getLogger(AdminLoginServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Serving admin-login.jsp");
        request.getRequestDispatcher("/admin-login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        LOGGER.info("Admin login attempt: username=" + username);

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            LOGGER.warning("Invalid input: username or password empty");
            request.setAttribute("error", "Username and password are required.");
            request.getRequestDispatcher("/admin-login.jsp").forward(request, response);
            return;
        }

        String filePath = getServletContext().getRealPath(ADMIN_FILE);
        LOGGER.info("Checking admin.txt at: " + filePath);
        boolean validAdmin = false;
        String fullName = null;

        synchronized (fileLock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 4 && parts[0].equals(username) && parts[3].equals(password)) {
                        validAdmin = true;
                        fullName = parts[2];
                        break;
                    }
                }
            } catch (IOException e) {
                LOGGER.severe("Error reading admin.txt at " + filePath + ": " + e.getMessage());
                request.setAttribute("error", "Error accessing admin data.");
                request.getRequestDispatcher("/admin-login.jsp").forward(request, response);
                return;
            }
        }

        if (validAdmin) {
            LOGGER.info("Admin login successful for: " + username);
            HttpSession session = request.getSession();
            session.setAttribute("username", username);
            session.setAttribute("fullName", fullName);
            session.setAttribute("isAdmin", true);
            response.sendRedirect("admin");
        } else {
            LOGGER.warning("Admin login failed: Invalid username or password");
            request.setAttribute("error", "Invalid username or password");
            request.getRequestDispatcher("/admin-login.jsp").forward(request, response);
        }
    }
}