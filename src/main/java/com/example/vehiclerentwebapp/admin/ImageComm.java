package com.example.vehiclerentwebapp.admin;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/img")
public class ImageComm extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.jsp?error=Please log in.");
            return;
        }

        String slipPath = getServletContext().getRealPath("/WEB-INF/uploads/" + request.getParameter("target"));
        File imageFile = new File(slipPath);

        if (imageFile.exists()) {
            response.setContentType("image/jpg"); // Set content type
            OutputStream outStream = response.getOutputStream();
            FileInputStream inStream = new FileInputStream(imageFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            inStream.close();
            outStream.close();
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
