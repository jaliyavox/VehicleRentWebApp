package com.example.vehiclerentwebapp.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    private static final String USERS_FILE = "users.txt";
    private static final Object fileLock = new Object();
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Serving login.jsp");
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        LOGGER.info("User login attempt: username=" + username);

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            LOGGER.warning("Invalid input: username or password empty");
            response.sendRedirect("login?error=" + java.net.URLEncoder.encode("Username and password are required.", "UTF-8"));
            return;
        }

        // Get the absolute path to the WEB-INF directory
        String webInfPath = getServletContext().getRealPath("/WEB-INF");
        Path usersFilePath = Paths.get(webInfPath, USERS_FILE);
        
        LOGGER.info("Checking users.txt at: " + usersFilePath);
        boolean validUser = false;
        String fullName = null;
        String email = null;

        synchronized (fileLock) {
            try {
                if (!Files.exists(usersFilePath)) {
                    LOGGER.warning("Users file does not exist at: " + usersFilePath);
                    response.sendRedirect("login?error=" + java.net.URLEncoder.encode("No users registered yet.", "UTF-8"));
                    return;
                }

                try (BufferedReader reader = Files.newBufferedReader(usersFilePath)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length == 4 && parts[0].equals(username) && parts[3].equals(password)) {
                            validUser = true;
                            email = parts[1];
                            fullName = parts[2];
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.severe("Error reading users.txt at " + usersFilePath + ": " + e.getMessage());
                response.sendRedirect("login?error=" + java.net.URLEncoder.encode("Error accessing user data.", "UTF-8"));
                return;
            }
        }

        if (validUser) {
            LOGGER.info("User login successful for: " + username);
            HttpSession session = request.getSession();
            session.setAttribute("username", username);
            session.setAttribute("email", email);
            session.setAttribute("fullName", fullName);
            session.setAttribute("isAdmin", false);
            response.sendRedirect("dashboard");
        } else {
            LOGGER.warning("User login failed: Invalid username or password");
            response.sendRedirect("login?error=" + java.net.URLEncoder.encode("Invalid username or password", "UTF-8"));
        }
    }
}