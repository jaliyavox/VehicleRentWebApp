package com.example.vehiclerentwebapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class Notification {
    private String notificationId;
    private String title;
    private String message;
    private String createdAt;
    private boolean active;

    // Default constructor
    public Notification() {
    }

    public Notification(String notificationId, String title, String message, String createdAt, boolean isActive) {
        this.notificationId = notificationId;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.active = isActive;
    }

    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    private static String encode(String str) {
        if (str == null) return "";
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String str) {
        if (str == null || str.isEmpty()) return "";
        try {
            return new String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return str; // Return original string if decoding fails
        }
    }

    @Override
    public String toString() {
        return String.format("%s|%s|%s|%s|%s",
                notificationId,
                encode(title),
                encode(message),
                createdAt,
                active);
    }

    public static Notification fromString(String str) {
        try {
            String[] parts = str.split("\\|");
            if (parts.length != 5) {
                return null;
            }

            Notification notification = new Notification();
            notification.setNotificationId(parts[0]);
            notification.setTitle(decode(parts[1]));
            notification.setMessage(decode(parts[2]));
            notification.setCreatedAt(parts[3]);
            notification.setActive(Boolean.parseBoolean(parts[4]));
            return notification;
        } catch (Exception e) {
            return null;
        }
    }
}