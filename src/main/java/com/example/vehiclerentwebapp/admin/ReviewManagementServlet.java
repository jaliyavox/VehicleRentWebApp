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

@WebServlet(name = "ReviewManagementServlet", urlPatterns = {"/review-management"})
public class ReviewManagementServlet extends HttpServlet {
    private static final String REVIEWS_FILE = "/WEB-INF/reviews.txt";
    private static final String FALLBACK_FILE = "C:/Users/jaliy/Downloads/Compressed/apache-tomcat-11.0.6/webapps/ROOT/WEB-INF/reviews.txt";
    private static final Object fileLock = new Object();
    private static final Logger LOGGER = Logger.getLogger(ReviewManagementServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            LOGGER.warning("Unauthorized access to review management");
            response.sendRedirect("login.jsp?error=Unauthorized access.");
            return;
        }

        String action = request.getParameter("action");
        String reviewId = request.getParameter("reviewId");
        String filePath = getServletContext().getRealPath(REVIEWS_FILE);
        LOGGER.info("getRealPath returned: " + filePath + ", reviewId: " + reviewId);
        if (filePath == null) {
            LOGGER.severe("getRealPath returned null, using fallback: " + FALLBACK_FILE);
            filePath = FALLBACK_FILE;
        }

        File file = new File(filePath);
        String error = null;

        synchronized (fileLock) {
            if (!file.exists()) {
                LOGGER.severe("reviews.txt does not exist at: " + filePath);
                error = "Reviews file not found.";
            } else if (!file.canRead() || !file.canWrite()) {
                LOGGER.severe("reviews.txt is not readable/writable at: " + filePath);
                error = "Cannot access reviews file.";
            } else if (!"delete".equals(action)) {
                error = "Invalid action.";
            } else if (reviewId == null || reviewId.trim().isEmpty()) {
                error = "Invalid review ID.";
            } else {
                List<String> lines = new ArrayList<>();
                boolean reviewExists = false;

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split(":", 6); // Limit to 6 parts
                        if (parts.length == 6 && parts[0].equals(reviewId)) {
                            reviewExists = true;
                            LOGGER.info("Deleted review: reviewId=" + reviewId);
                        } else {
                            lines.add(line);
                        }
                    }
                    if (!reviewExists) {
                        LOGGER.warning("Review not found for reviewId: " + reviewId);
                    }
                } catch (IOException e) {
                    LOGGER.severe("Error reading reviews.txt at " + filePath + ": " + e.getMessage());
                    error = "Error accessing reviews data.";
                }

                if (error == null && !reviewExists) {
                    error = "Review not found.";
                }

                if (error == null) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                        for (String line : lines) {
                            writer.write(line);
                            writer.newLine();
                        }
                        writer.flush();
                        LOGGER.info("Review delete successful: reviewId=" + reviewId);
                    } catch (IOException e) {
                        LOGGER.severe("Error writing to reviews.txt at " + filePath + ": " + e.getMessage());
                        error = "Error saving review data.";
                    }
                }
            }
        }

        if (error != null) {
            response.sendRedirect("admin?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
        } else {
            response.sendRedirect("admin?success=" + java.net.URLEncoder.encode("Review deleted successfully.", "UTF-8"));
        }
    }
}