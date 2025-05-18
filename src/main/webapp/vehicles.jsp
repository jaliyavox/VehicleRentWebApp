<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.vehiclerentwebapp.Vehicle" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Vehicles - VehicleRent</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 font-sans">
<header class="bg-blue-900 text-white">
  <nav class="container mx-auto px-4 py-4 flex justify-between items-center">
    <div class="text-2xl font-bold">VehicleRent</div>
    <ul class="flex space-x-6">
      <li><a href="index.jsp" class="hover:text-blue-300">Home</a></li>
      <li><a href="vehicles" class="hover:text-blue-300">Vehicles</a></li>
      <li><a href="reviews" class="hover:text-blue-300">Reviews</a></li>
      <% if (session.getAttribute("isAdmin") != null && (Boolean)session.getAttribute("isAdmin")) { %>
        <li><a href="admin" class="hover:text-blue-300">Dashboard</a></li>
      <% } else { %>
        <li><a href="dashboard" class="hover:text-blue-300">Dashboard</a></li>
      <% } %>
      <li><a href="logout" class="hover:text-blue-300">Logout</a></li>
    </ul>
  </nav>
</header>
<main class="container mx-auto px-4 py-8">
  <h1 class="text-3xl font-bold text-gray-800 mb-6">Available Vehicles</h1>

  <% if (request.getParameter("error") != null) { %>
  <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
    <%= request.getParameter("error") %>
  </div>
  <% } %>

  <form class="mb-6">
    <label class="mr-2">Sort by:</label>
    <select name="sort" onchange="this.form.submit()">
      <option value="availability" <%= "availability".equals(request.getParameter("sort")) ? "selected" : "" %>>Availability</option>
      <option value="priceAsc" <%= "priceAsc".equals(request.getParameter("sort")) ? "selected" : "" %>>Price (Low to High)</option>
      <option value="priceDesc" <%= "priceDesc".equals(request.getParameter("sort")) ? "selected" : "" %>>Price (High to Low)</option>
    </select>
  </form>

  <%
    Vehicle[] vehicles = (Vehicle[]) request.getAttribute("vehicles");
    int vehicleCount = (vehicles != null) ? vehicles.length : 0;
  %>
  <p class="text-gray-600 mb-4">Found <%= vehicleCount %> vehicles</p>

  <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
    <%
      if (vehicles != null && vehicles.length > 0) {
        for (Vehicle vehicle : vehicles) {
    %>
    <div class="bg-white p-6 rounded-lg shadow-md">
      <h2 class="text-xl font-semibold mb-2"><%= vehicle.getBrand() %> <%= vehicle.getModel() %></h2>
      <p class="text-gray-600 mb-2">Type: <%= vehicle.getType() %></p>
      <p class="text-gray-600 mb-2">Year: <%= vehicle.getYear() %></p>
      <p class="text-gray-600 mb-2">Available: <%= vehicle.isAvailable() ? "Yes" : "No" %></p>
      <p class="text-gray-600 mb-4">Price/Day: $<%= String.format("%.2f", vehicle.getPricePerDay()) %></p>
      <% if (vehicle.isAvailable()) { %>
      <a href="bookings?vehicleId=<%= vehicle.getVehicleId() %>"
         class="inline-block bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Book Now</a>
      <% } %>
    </div>
    <%
      }
    } else {
    %>
    <p class="text-gray-600">No vehicles available. Please check back later or contact support.</p>
    <%
      }
    %>
  </div>
</main>
<footer class="bg-blue-900 text-white text-center py-4">
  <p>© 2025 VehicleRentWebApp. All rights reserved.</p>
</footer>
</body>
</html>