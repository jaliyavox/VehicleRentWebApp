package com.example.vehiclerentwebapp;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@WebServlet(name = "ReviewServlet", urlPatterns = {"/reviews"})
public class ReviewServlet extends HttpServlet {
    private static final String REVIEWS_FILE = "/WEB-INF/reviews.txt";
    private static final String FALLBACK_FILE = "C:/Users/jaliy/Downloads/Compressed/apache-tomcat-11.0.6/webapps/ROOT/WEB-INF/reviews.txt";
    private static final Object fileLock = new Object();
    private static final Logger LOGGER = Logger.getLogger(ReviewServlet.class.getName());
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            LOGGER.warning("Unauthorized access to reviews page");
            response.sendRedirect("login.jsp?error=Please log in.");
            return;
        }

        // Read reviews from file
        List<Review> reviews = new ArrayList<>();
        String filePath = getServletContext().getRealPath(REVIEWS_FILE);
        if (filePath == null) {
            LOGGER.severe("getRealPath returned null, using fallback: " + FALLBACK_FILE);
            filePath = FALLBACK_FILE;
        }

        File file = new File(filePath);
        if (file.exists()) {
            synchronized (fileLock) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split(":", 6);
                        if (parts.length == 6) {
                            try {
                                int rating = Integer.parseInt(parts[2]);
                                reviews.add(new Review(parts[0], parts[1], rating, parts[3], parts[4], parts[5]));
                            } catch (NumberFormatException e) {
                                LOGGER.warning("Invalid review format: " + line);
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.severe("Error reading reviews.txt at " + filePath + ": " + e.getMessage());
                }
            }
        }

        request.setAttribute("reviews", reviews);
        request.getRequestDispatcher("/reviews.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            LOGGER.warning("Unauthorized review action attempt");
            response.sendRedirect("login.jsp?error=Please log in.");
            return;
        }

        String action = request.getParameter("action");
        String username = (String) session.getAttribute("username");
        String filePath = getServletContext().getRealPath(REVIEWS_FILE);
        if (filePath == null) {
            LOGGER.severe("getRealPath returned null, using fallback: " + FALLBACK_FILE);
            filePath = FALLBACK_FILE;
        }

        File file = new File(filePath);
        String error = null;

        synchronized (fileLock) {
            List<String> lines = new ArrayList<>();
            boolean reviewFound = false;

            // Read existing reviews
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        lines.add(line);
                        if ("add".equals(action) && line.split(":", 6)[1].equals(username)) {
                            reviewFound = true; // User already has a review
                        }
                    }
                } catch (IOException e) {
                    LOGGER.severe("Error reading reviews.txt at " + filePath + ": " + e.getMessage());
                    error = "Error accessing review data.";
                }
            }

            if (error == null && "add".equals(action)) {
                if (reviewFound) {
                    error = "You have already submitted a review.";
                    LOGGER.warning("User " + username + " attempted to add multiple reviews.");
                } else {
                    String ratingStr = request.getParameter("rating");
                    String comment = request.getParameter("comment");

                    // Validate inputs
                    if (ratingStr == null || comment == null || comment.trim().isEmpty() || comment.length() > 500) {
                        error = "Valid rating and comment (1-500 characters) are required.";
                    } else {
                        try {
                            int rating = Integer.parseInt(ratingStr);
                            if (rating < 1 || rating > 5) {
                                error = "Rating must be between 1 and 5.";
                            }
                        } catch (NumberFormatException e) {
                            error = "Invalid rating format.";
                        }
                    }

                    if (error == null) {
                        String reviewId = UUID.randomUUID().toString();
                        String vehicleId = "none"; // Placeholder, as vehicle ID is not provided
                        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
                        String reviewLine = reviewId + ":" + username + ":" + ratingStr + ":" + comment + ":" + vehicleId + ":" + timestamp;
                        lines.add(reviewLine);
                        LOGGER.info("Added review by: " + username + ", reviewId: " + reviewId);
                    }
                }
            } else if (error == null && "update".equals(action)) {
                String ratingStr = request.getParameter("rating");
                String comment = request.getParameter("comment");
                String timestamp = request.getParameter("timestamp");

                // Validate inputs
                if (ratingStr == null || comment == null || comment.trim().isEmpty() || comment.length() > 500) {
                    error = "Valid rating and comment (1-500 characters) are required.";
                } else {
                    try {
                        int rating = Integer.parseInt(ratingStr);
                        if (rating < 1 || rating > 5) {
                            error = "Rating must be between 1 and 5.";
                        }
                    } catch (NumberFormatException e) {
                        error = "Invalid rating format.";
                    }
                }

                if (error == null) {
                    List<String> newLines = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.trim().isEmpty()) continue;
                            String[] parts = line.split(":", 6);
                            if (parts.length == 6 && parts[1].equals(username) && parts[5].equals(timestamp)) {
                                reviewFound = true;
                                String reviewId = parts[0];
                                String vehicleId = parts[4];
                                String reviewLine = reviewId + ":" + username + ":" + ratingStr + ":" + comment + ":" + vehicleId + ":" + timestamp;
                                newLines.add(reviewLine);
                                LOGGER.info("Updated review by: " + username + " at " + timestamp);
                            } else {
                                newLines.add(line);
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.severe("Error reading reviews.txt at " + filePath + ": " + e.getMessage());
                        error = "Error accessing review data.";
                    }
                    lines = newLines;
                }
            } else if (error == null && "delete".equals(action)) {
                String timestamp = request.getParameter("timestamp");
                List<String> newLines = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split(":", 6);
                        if (parts.length == 6 && parts[1].equals(username) && parts[5].equals(timestamp)) {
                            reviewFound = true;
                            LOGGER.info("Deleted review by: " + username + " at " + timestamp);
                            continue;
                        }
                        newLines.add(line);
                    }
                } catch (IOException e) {
                    LOGGER.severe("Error reading reviews.txt at " + filePath + ": " + e.getMessage());
                    error = "Error accessing review data.";
                }
                lines = newLines;
            } else if (error == null) {
                error = "Invalid action.";
                LOGGER.warning("Invalid action: " + action);
            }

            if (error == null && ("update".equals(action) || "delete".equals(action)) && !reviewFound) {
                error = "Review not found or you don’t have permission to modify it.";
                LOGGER.warning("Review action failed: Review not found for " + username);
            }

            // Write all reviews back to file
            if (error == null && ("add".equals(action) || "update".equals(action) || "delete".equals(action))) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    for (String line : lines) {
                        writer.write(line);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    LOGGER.severe("Error writing to reviews.txt at " + filePath + ": " + e.getMessage());
                    error = "Error saving review data.";
                }
            }
        }

        // Redirect with success or error message
        if (error != null) {
            response.sendRedirect("reviews?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
        } else {
            String successMessage = "Review " + action + " successful.";
            response.sendRedirect("reviews?success=" + java.net.URLEncoder.encode(successMessage, "UTF-8"));
        }
    }

    // Inner class to hold review data
    public static class Review {
        private String reviewId;
        private String username;
        private int rating;
        private String comment;
        private String vehicleId;
        private String timestamp;

        public Review(String reviewId, String username, int rating, String comment, String vehicleId, String timestamp) {
            this.reviewId = reviewId;
            this.username = username;
            this.rating = rating;
            this.comment = comment;
            this.vehicleId = vehicleId;
            this.timestamp = timestamp;
        }

        public String getReviewId() {
            return reviewId;
        }

        public String getUsername() {
            return username;
        }

        public int getRating() {
            return rating;
        }

        public String getComment() {
            return comment;
        }

        public String getVehicleId() {
            return vehicleId;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}