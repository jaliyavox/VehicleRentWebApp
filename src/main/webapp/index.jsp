<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vehicle Rental - Home</title>
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
            <% if (session.getAttribute("user") != null) { %>
                <% if (session.getAttribute("isAdmin") != null && (Boolean)session.getAttribute("isAdmin")) { %>
                    <li><a href="admin" class="hover:text-blue-300">Dashboard</a></li>
                <% } else { %>
                    <li><a href="dashboard" class="hover:text-blue-300">Dashboard</a></li>
                <% } %>
                <li><a href="logout" class="hover:text-blue-300">Logout</a></li>
            <% } else { %>
                <li><a href="login.jsp" class="hover:text-blue-300">Login</a></li>
                <li><a href="register.jsp" class="hover:text-blue-300">Register</a></li>
            <% } %>
        </ul>
    </nav>
</header>

<main class="container mx-auto px-4 py-8">
    <section class="text-center mb-12">
        <h1 class="text-4xl font-bold text-gray-800 mb-4">Welcome to VehicleRent</h1>
        <p class="text-lg text-gray-600 mb-6">Rent your dream vehicle with ease and convenience.</p>
        <a href="vehicles" class="inline-block bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700">Browse Vehicles</a>
    </section>

    <section class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div class="bg-white p-6 rounded-lg shadow-md">
            <h2 class="text-xl font-semibold mb-4">Wide Selection</h2>
            <p class="text-gray-600">Choose from a variety of cars, SUVs, and more for your journey.</p>
        </div>
        <div class="bg-white p-6 rounded-lg shadow-md">
            <h2 class="text-xl font-semibold mb-4">Easy Booking</h2>
            <p class="text-gray-600">Book your vehicle in just a few clicks with our simple process.</p>
        </div>
        <div class="bg-white p-6 rounded-lg shadow-md">
            <h2 class="text-xl font-semibold mb-4">Trusted Reviews</h2>
            <p class="text-gray-600">Read reviews from other renters to make informed decisions.</p>
        </div>
    </section>
</main>

<footer class="bg-blue-900 text-white text-center py-4">
    <p>© 2025 VehicleRentWebApp. All rights reserved.</p>
</footer>
</body>
</html>