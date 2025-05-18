package com.example.vehiclerentwebapp.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.util.logging.Logger;

@WebServlet(name = "SlipServlet", urlPatterns = {"/slip"})
public class SlipServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SlipServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            LOGGER.warning("Unauthorized access to slip");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized");
            return;
        }

        String bookingId = request.getParameter("bookingId");
        if (bookingId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing bookingId");
            return;
        }

        String slipPath = "/WEB-INF/uploads/slip_" + bookingId + ".jpg";
        File file = new File(getServletContext().getRealPath(slipPath));
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Slip not found");
            return;
        }

        response.setContentType("image/jpeg");
        try (InputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}