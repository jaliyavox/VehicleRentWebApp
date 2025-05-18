<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.vehiclerentwebapp.Booking" %>
<%@ page import="com.example.vehiclerentwebapp.Vehicle" %>
<%@ page import="com.example.vehiclerentwebapp.VehicleLinkedList" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Dashboard - Vehicle Rental</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        .modal {
            display: none;
            position: fixed;
            z-index: 50;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            overflow: auto;
            background-color: rgba(0,0,0,0.4);
        }
        .modal-content {
            background-color: #ffffff;
            margin: 15% auto;
            padding: 20px;
            border: 1px solid #888;
            width: 80%;
            max-width: 500px;
            border-radius: 8px;
        }
    </style>
</head>
<body class="bg-gray-100 font-sans">
<header class="bg-blue-900 text-white">
    <nav class="container mx-auto px-4 py-4 flex justify-between items-center">
        <div class="text-2xl font-bold">VehicleRent</div>
        <ul class="flex space-x-6">
            <li><a href="index.jsp" class="hover:text-blue-300">Home</a></li>
            <li><a href="vehicles" class="hover:text-blue-300">Vehicles</a></li>
            <li><a href="reviews" class="hover:text-blue-300">Reviews</a></li>
            <li><a href="dashboard" class="hover:text-blue-300">Dashboard</a></li>
            <li><a href="logout" class="hover:text-blue-300">Logout</a></li>
        </ul>
    </nav>
</header>

<main class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold text-gray-800 mb-6">User Dashboard</h1>

    <!-- Success/Error Messages -->
    <% if (request.getParameter("success") != null) { %>
    <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-6">
        <%= request.getParameter("success") %>
    </div>
    <% } %>
    <% if (request.getParameter("error") != null) { %>
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
        <%= request.getParameter("error") %>
    </div>
    <% } %>

    <div class="bg-white p-6 rounded-lg shadow-md mb-6">
        <h2 class="text-xl font-semibold mb-4">Welcome, <%= session.getAttribute("fullName") %></h2>
        <p><strong>Username:</strong> <%= session.getAttribute("username") %></p>
        <p><strong>Email:</strong> <%= session.getAttribute("email") %></p>
        <button onclick="openUpdateModal('<%= session.getAttribute("username") %>', '<%= session.getAttribute("username") %>', '<%= session.getAttribute("email") %>', '<%= session.getAttribute("fullName") %>')"
                class="bg-blue-600 text-white px-4 py-2 mt-4 rounded hover:bg-blue-700">Update Details</button>
    </div>

    <!-- Update User Modal -->
    <div id="updateModal" class="modal">
        <div class="modal-content">
            <h2 class="text-xl font-semibold mb-4">Update Your Details</h2>
            <form action="user-management" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="username" id="currentUsername">
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Username</label>
                    <input type="text" id="updateUsername" name="newUsername" required
                           class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Email</label>
                    <input type="email" id="updateEmail" name="email" required
                           class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Full Name</label>
                    <input type="text" id="updateFullName" name="fullName" required
                           class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Password (leave blank to keep current)</label>
                    <input type="password" id="updatePassword" name="password"
                           class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div class="flex justify-end space-x-2">
                    <button type="button" onclick="closeUpdateModal()"
                            class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600">Cancel</button>
                    <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Save</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Notifications Section -->
    <div class="bg-white p-6 rounded-lg shadow-md mb-6">
        <div class="flex justify-between items-center">
            <h2 class="text-xl font-semibold">Notifications</h2>
            <a href="notifications" 
               class="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z" />
                </svg>
                View Notifications
            </a>
        </div>
    </div>

    <div class="bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-xl font-semibold mb-4">Your Bookings</h2>
        <%
            List<Booking> bookings = (List<Booking>) request.getAttribute("bookings");
            VehicleLinkedList vehicleList = (VehicleLinkedList) request.getAttribute("vehicleList");
            String username = (String) session.getAttribute("username");
            String normalizedUsername = username != null ? username.trim().toLowerCase() : null;
        %>
        <% if (bookings == null || bookings.isEmpty()) { %>
        <p class="text-gray-600">No bookings found.</p>
        <% } else { %>
        <div class="overflow-x-auto">
            <table class="w-full border-collapse">
                <thead>
                <tr class="bg-gray-200">
                    <th class="border p-2">Booking ID</th>
                    <th class="border p-2">Vehicle</th>
                    <th class="border p-2">Start Date</th>
                    <th class="border p-2">End Date</th>
                    <th class="border p-2">Total Price</th>
                    <th class="border p-2">Status</th>
                    <th class="border p-2">Actions</th>
                </tr>
                </thead>
                <tbody>
                <%
                    boolean hasUserBookings = false;
                    for (Booking booking : bookings) {
                        String bookingUsername = booking.getUsername() != null ? booking.getUsername().trim().toLowerCase() : null;
                        if (bookingUsername != null && normalizedUsername != null && bookingUsername.equals(normalizedUsername)) {
                            hasUserBookings = true;
                            Vehicle vehicle = vehicleList.find(booking.getVehicleId());
                            String vehicleName = vehicle != null ? vehicle.getBrand() + " " + vehicle.getModel() : "Unknown";
                            String statusTooltip = booking.getStatus().equals("confirmed") ? "Approved" : "Not Approved";
                %>
                <tr class="border-b">
                    <td class="border p-2"><%= booking.getBookingId() %></td>
                    <td class="border p-2"><%= vehicleName %></td>
                    <td class="border p-2"><%= booking.getStartDate() %></td>
                    <td class="border p-2"><%= booking.getEndDate() %></td>
                    <td class="border p-2">$<%= String.format("%.2f", booking.getTotalPrice()) %></td>
                    <td class="border p-2">
                        <span class="inline-block px-2 py-1 rounded text-sm <%=
                            booking.getStatus().equals("pending") ? "bg-yellow-100 text-yellow-800" :
                            booking.getStatus().equals("confirmed") ? "bg-green-100 text-green-800" :
                            "bg-red-100 text-red-800" %>"
                              title="<%= statusTooltip %>">
                            <%= booking.getStatus().substring(0, 1).toUpperCase() + booking.getStatus().substring(1) %>
                        </span>
                    </td>
                    <td class="border p-2">
                        <% if (booking.getStatus().equals("pending")) { %>
                        <a href="bookings?action=edit&bookingId=<%= booking.getBookingId() %>"
                           class="bg-yellow-600 text-white px-2 py-1 rounded hover:bg-yellow-700 mr-2">Edit</a>
                        <a href="bookings?action=delete&bookingId=<%= booking.getBookingId() %>"
                           class="bg-red-600 text-white px-2 py-1 rounded hover:bg-red-700"
                           onclick="return confirm('Are you sure you want to delete this booking?')">Delete</a>
                        <% } %>
                    </td>
                </tr>
                <%
                        }
                    }
                    if (!hasUserBookings) {
                %>
                <tr>
                    <td colspan="7" class="border p-2 text-gray-600">No bookings found for this user.</td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
        <% } %>
        <a href="vehicles" class="inline-block bg-blue-600 text-white px-4 py-2 mt-4 rounded-lg hover:bg-blue-700">Book a Vehicle</a>
    </div>
</main>

<footer class="bg-blue-900 text-white text-center py-4">
    <p>© 2025 VehicleRentWebApp. All rights reserved.</p>
</footer>

<script>
    function openUpdateModal(currentUsername, username, email, fullName) {
        document.getElementById('currentUsername').value = currentUsername;
        document.getElementById('updateUsername').value = username;
        document.getElementById('updateEmail').value = email;
        document.getElementById('updateFullName').value = fullName;
        document.getElementById('updatePassword').value = '';
        document.getElementById('updateModal').style.display = 'block';
    }

    function closeUpdateModal() {
        document.getElementById('updateModal').style.display = 'none';
    }

    window.onclick = function(event) {
        let modal = document.getElementById('updateModal');
        if (event.target == modal) {
            closeUpdateModal();
        }
    }
</script>
</body>
</html>
```