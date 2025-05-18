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
    <title>Booking Management - VehicleRent</title>
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
            background-color: rgba(0, 0, 0, 0.4);
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
            <li><a href="vehicle-management" class="hover:text-blue-300">Manage Vehicles</a></li>
            <li><a href="booking-management" class="hover:text-blue-300">Manage Bookings</a></li>
            <li><a href="logout" class="hover:text-blue-300">Logout</a></li>
        </ul>
    </nav>
</header>

<main class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold text-gray-800 mb-6">Booking Management</h1>

    <%
        String success = request.getParameter("success");
        String error = request.getParameter("error");
        List<Booking> bookings = (List<Booking>) request.getAttribute("bookings");
        VehicleLinkedList vehicleList = (VehicleLinkedList) request.getAttribute("vehicleList");
    %>

    <% if (success != null) { %>
    <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-6">
        <%= success %>
    </div>
    <% } else if (error != null) { %>
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
        <%= error %>
    </div>
    <% } %>

    <div class="bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-xl font-semibold mb-4">All Bookings</h2>
        <% if (bookings == null || bookings.isEmpty()) { %>
        <p class="text-gray-600">No bookings found.</p>
        <% } else { %>
        <div class="overflow-x-auto">
            <table class="w-full border-collapse">
                <thead>
                <tr class="bg-gray-200">
                    <th class="border p-2">Booking ID</th>
                    <th class="border p-2">Username</th>
                    <th class="border p-2">Vehicle</th>
                    <th class="border p-2">Start Date</th>
                    <th class="border p-2">End Date</th>
                    <th class="border p-2">Total Price</th>
                    <th class="border p-2">Status</th>
                    <th class="border p-2">Actions</th>
                </tr>
                </thead>
                <tbody>
                <% for (Booking booking : bookings) {
                    Vehicle vehicle = vehicleList.find(booking.getVehicleId());
                    String vehicleName = vehicle != null ? vehicle.getBrand() + " " + vehicle.getModel() : "Unknown";
                %>
                <tr class="border-b">
                    <td class="border p-2"><%= booking.getBookingId() %>
                    </td>
                    <td class="border p-2"><%= booking.getUsername() %>
                    </td>
                    <td class="border p-2"><%= vehicleName %>
                    </td>
                    <td class="border p-2"><%= booking.getStartDate() %>
                    </td>
                    <td class="border p-2"><%= booking.getEndDate() %>
                    </td>
                    <td class="border p-2">$<%= String.format("%.2f", booking.getTotalPrice()) %>
                    </td>
                    <td class="border p-2">
                        <span class="inline-block px-2 py-1 rounded text-sm <%=
                            booking.getStatus().equals("pending") ? "bg-yellow-100 text-yellow-800" :
                            booking.getStatus().equals("confirmed") ? "bg-green-100 text-green-800" :
                            "bg-red-100 text-red-800" %>">
                            <%= booking.getStatus().substring(0, 1).toUpperCase() + booking.getStatus().substring(1) %>
                        </span>
                    </td>
                    <td class="border p-2">
                        <a href="${pageContext.request.contextPath}/img?target=<%=booking.getSlipPath()%>"
                           target="_blank">
                            <button class="bg-blue-600 text-white px-2 py-1 rounded hover:bg-blue-700 mr-2">Slip
                            </button>
                        </a>
                        <% if (booking.getStatus().equals("pending")) { %>
                        <button onclick="openActionModal('approve', '<%= booking.getBookingId() %>')"
                                class="bg-blue-600 text-white px-2 py-1 rounded hover:bg-blue-700 mr-2">Approve
                        </button>
                        <button onclick="openActionModal('reject', '<%= booking.getBookingId() %>')"
                                class="bg-red-600 text-white px-2 py-1 rounded hover:bg-red-700">Reject
                        </button>
                        <% } %>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
        <% } %>
    </div>

    <!-- Action Modal -->
    <div id="actionModal" class="modal">
        <div class="modal-content">
            <h2 class="text-xl font-semibold mb-4" id="modalTitle"></h2>
            <p class="mb-4" id="modalMessage"></p>
            <form id="actionForm" action="booking-management" method="post">
                <input type="hidden" name="action" id="actionType">
                <input type="hidden" name="bookingId" id="actionBookingId">
                <div class="flex justify-end space-x-2">
                    <button type="button" onclick="closeActionModal()"
                            class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600">Cancel
                    </button>
                    <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                            id="actionSubmitButton">Confirm
                    </button>
                </div>
            </form>
        </div>
    </div>
</main>

<footer class="bg-blue-900 text-white text-center py-4">
    <p>© 2025 VehicleRent. All rights reserved.</p>
</footer>

<script>
    function openActionModal(action, bookingId) {
        const modal = document.getElementById('actionModal');
        const modalTitle = document.getElementById('modalTitle');
        const modalMessage = document.getElementById('modalMessage');
        const actionType = document.getElementById('actionType');
        const actionBookingId = document.getElementById('actionBookingId');
        const actionSubmitButton = document.getElementById('actionSubmitButton');

        actionType.value = action;
        actionBookingId.value = bookingId;
        modalTitle.textContent = action === 'approve' ? 'Approve Booking' : 'Reject Booking';
        modalMessage.textContent = `Are you sure you want to ${action} booking ID ${bookingId}?`;
        actionSubmitButton.className = action === 'approve' ?
            'bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700' :
            'bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700';

        modal.style.display = 'block';
    }

    function closeActionModal() {
        document.getElementById('actionModal').style.display = 'none';
    }

    window.onclick = function (event) {
        const modal = document.getElementById('actionModal');
        if (event.target === modal) {
            closeActionModal();
        }
    }
</script>
</body>
</html>