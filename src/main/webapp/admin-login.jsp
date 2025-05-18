<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Admin Login - Vehicle Rental</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 font-sans">
<header class="bg-blue-900 text-white">
  <nav class="container mx-auto px-4 py-4 flex justify-between items-center">
    <div class="text-2xl font-bold">VehicleRent</div>
    <ul class="flex space-x-6">
      <li><a href="index.jsp" class="hover:text-blue-300">Home</a></li

    </ul>
  </nav>
</header>

<main class="container mx-auto px-4 py-8">
  <div class="max-w-md mx-auto bg-white p-6 rounded-lg shadow-md">
    <h2 class="text-2xl font-bold text-gray-800 mb-6 text-center">Admin Login</h2>
    <% if (request.getAttribute("error") != null) { %>
    <p class="text-red-600 mb-4 text-center"><%= request.getAttribute("error") %></p>
    <% } %>

    <!-- Admin Login Form -->
    <form action="admin-login" method="post">
      <div class="mb-4">
        <label for="username" class="block text-gray-700">Admin Username</label>
        <input type="text" id="username" name="username" class="w-full border p-2 rounded" required>
      </div>
      <div class="mb-4">
        <label for="password" class="block text-gray-700">Admin Password</label>
        <input type="password" id="password" name="password" class="w-full border p-2 rounded" required>
      </div>
      <button type="submit" class="w-full bg-blue-900 text-white p-2 rounded hover:bg-blue-800">Login as Admin</button>
    </form>

    <p class="text-center mt-4">Not an admin? <a href="login.jsp" class="text-blue-600 hover:underline">User Login</a>.</p>
  </div>
</main>

<footer class="bg-blue-900 text-white text-center py-4">
  <p>© 2025 VehicleRentWebApp. All rights reserved.</p>
</footer>
</body>
</html>