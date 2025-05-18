<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.vehiclerentwebapp.Vehicle" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vehicle Management - VehicleRent</title>
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
            <li><a href="vehicle-management" class="hover:text-blue-300">Manage Vehicles</a></li>
            <li><a href="booking-management" class="hover:text-blue-300">Manage Bookings</a></li>
            <li><a href="logout" class="hover:text-blue-300">Logout</a></li>
        </ul>
    </nav>
</header>

<main class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold text-gray-800 mb-6">Vehicle Management</h1>

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

    <!-- Add Vehicle Form -->
    <div class="bg-white p-6 rounded-lg shadow-md mb-8">
        <h2 class="text-xl font-semibold mb-4">Add New Vehicle</h2>
        <form action="vehicle-management" method="post" class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <input type="hidden" name="action" value="add">
            <input type="text" name="type" placeholder="Type (e.g., Sedan)" class="border p-2 rounded" required>
            <input type="text" name="brand" placeholder="Brand" class="border p-2 rounded" required>
            <input type="text" name="model" placeholder="Model" class="border p-2 rounded" required>
            <input type="number" name="year" placeholder="Year" class="border p-2 rounded" required min="1900" max="2025">
            <input type="number" name="pricePerDay" placeholder="Price/Day" class="border p-2 rounded" required step="0.01" min="0">
            <select name="available" class="border p-2 rounded" required>
                <option value="true">Available</option>
                <option value="false">Not Available</option>
            </select>
            <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 col-span-3">Add Vehicle</button>
        </form>
    </div>

    <!-- Vehicle List -->
    <div class="bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-xl font-semibold mb-4">Manage Vehicles</h2>
        <form class="mb-6">
            <label class="mr-2">Sort by:</label>
            <select name="sort" onchange="this.form.submit()">
                <option value="availability" <%= "availability".equals(request.getParameter("sort")) ? "selected" : "" %>>Availability</option>
                <option value="priceAsc" <%= "priceAsc".equals(request.getParameter("sort")) ? "selected" : "" %>>Price (Low to High)</option>
                <option value="priceDesc" <%= "priceDesc".equals(request.getParameter("sort")) ? "selected" : "" %>>Price (High to Low)</option>
            </select>
        </form>
        <table class="w-full border-collapse">
            <thead>
            <tr class="bg-gray-200">
                <th class="border p-2">Type</th>
                <th class="border p-2">Brand</th>
                <th class="border p-2">Model</th>
                <th class="border p-2">Year</th>
                <th class="border p-2">Price/Day</th>
                <th class="border p-2">Available</th>
                <th class="border p-2">Actions</th>
            </tr>
            </thead>
            <tbody>
            <%
                Vehicle[] vehicles = (Vehicle[]) request.getAttribute("vehicles");
                if (vehicles != null) {
                    for (Vehicle vehicle : vehicles) {
            %>
            <tr>
                <td class="border p-2"><%= vehicle.getType() %></td>
                <td class="border p-2"><%= vehicle.getBrand() %></td>
                <td class="border p-2"><%= vehicle.getModel() %></td>
                <td class="border p-2"><%= vehicle.getYear() %></td>
                <td class="border p-2">$<%= String.format("%.2f", vehicle.getPricePerDay()) %></td>
                <td class="border p-2"><%= vehicle.isAvailable() ? "Yes" : "No" %></td>
                <td class="border p-2">
                    <button onclick="openUpdateModal('<%= vehicle.getVehicleId() %>', '<%= vehicle.getType() %>', '<%= vehicle.getBrand() %>', '<%= vehicle.getModel() %>', <%= vehicle.getYear() %>, <%= vehicle.getPricePerDay() %>, <%= vehicle.isAvailable() %>)"
                            class="bg-blue-600 text-white px-2 py-1 rounded hover:bg-blue-700">Update</button>
                    <form action="vehicle-management" method="post" class="inline">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="vehicleId" value="<%= vehicle.getVehicleId() %>">
                        <button type="submit" class="bg-red-600 text-white px-2 py-1 rounded hover:bg-red-700"
                                onclick="return confirm('Are you sure you want to delete this vehicle?')">Delete</button>
                    </form>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </div>
</main>

<!-- Update Vehicle Modal -->
<div id="updateVehicleModal" class="modal">
    <div class="modal-content">
        <h2 class="text-xl font-semibold mb-4">Update Vehicle</h2>
        <form action="vehicle-management" method="post">
            <input type="hidden" name="action" value="update">
            <input type="hidden" id="updateVehicleId" name="vehicleId">
            <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700">Type</label>
                <input type="text" id="updateVehicleType" name="type" required
                       class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
            </div>
            <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700">Brand</label>
                <input type="text" id="updateVehicleBrand" name="brand" required
                       class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
            </div>
            <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700">Model</label>
                <input type="text" id="updateVehicleModel" name="model" required
                       class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
            </div>
            <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700">Year</label>
                <input type="number" id="updateVehicleYear" name="year" required min="1900" max="2025"
                       class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
            </div>
            <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700">Price/Day</label>
                <input type="number" id="updateVehiclePrice" name="pricePerDay" required step="0.01" min="0"
                       class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
            </div>
            <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700">Available</label>
                <select id="updateVehicleAvailable" name="available" class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                </select>
            </div>
            <div class="flex justify-end space-x-2">
                <button type="button" onclick="closeUpdateModal()"
                        class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600">Cancel</button>
                <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Save</button>
            </div>
        </form>
    </div>
</div>

<footer class="bg-blue-900 text-white text-center py-4">
    <p>© 2025 VehicleRentWebApp. All rights reserved.</p>
</footer>

<script>
    function openUpdateModal(vehicleId, type, brand, model, year, price, available) {
        document.getElementById('updateVehicleId').value = vehicleId;
        document.getElementById('updateVehicleType').value = type;
        document.getElementById('updateVehicleBrand').value = brand;
        document.getElementById('updateVehicleModel').value = model;
        document.getElementById('updateVehicleYear').value = year;
        document.getElementById('updateVehiclePrice').value = price;
        document.getElementById('updateVehicleAvailable').value = available.toString();
        document.getElementById('updateVehicleModal').style.display = 'block';
    }

    function closeUpdateModal() {
        document.getElementById('updateVehicleModal').style.display = 'none';
    }

    window.onclick = function(event) {
        let modal = document.getElementById('updateVehicleModal');
        if (event.target == modal) {
            closeUpdateModal();
        }
    }
</script>
</body>
</html>