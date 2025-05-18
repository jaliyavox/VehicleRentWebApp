<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Vehicle Rental</title>
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
            <li><a href="admin" class="hover:text-blue-300">Dashboard</a></li>
            <li><a href="logout" class="hover:text-blue-300">Logout</a></li>
        </ul>
    </nav>
</header>

<main class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold text-gray-800 mb-6">Admin Dashboard</h1>

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

    <!-- Notification Management Section -->
    <div class="bg-white p-6 rounded-lg shadow-md mb-6">
        <h2 class="text-xl font-semibold mb-4">Notification Management</h2>
        
        <!-- Create Notification Form -->
        <div class="mb-6">
            <h3 class="text-lg font-medium mb-3">Create New Notification</h3>
            <form action="notifications" method="post" class="space-y-4">
                <input type="hidden" name="action" value="create">
                <div>
                    <label for="title" class="block text-sm font-medium text-gray-700">Title</label>
                    <input type="text" id="title" name="title" required
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500">
                </div>
                <div>
                    <label for="message" class="block text-sm font-medium text-gray-700">Message</label>
                    <textarea id="message" name="message" rows="3" required
                              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"></textarea>
                </div>
                <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
                    Create Notification
                </button>
            </form>
        </div>

        <!-- List of Notifications -->
        <div>
            <h3 class="text-lg font-medium mb-3">Existing Notifications</h3>
            <c:if test="${empty notifications}">
                <p class="text-gray-600">No notifications available.</p>
            </c:if>
            <c:forEach items="${notifications}" var="notification">
                <div class="border rounded-lg p-4 mb-4 ${notification.active ? 'bg-white' : 'bg-gray-50'}">
                    <div class="flex justify-between items-start">
                        <div>
                            <h4 class="text-lg font-medium">${notification.title}</h4>
                            <p class="text-gray-600 mt-1">${notification.message}</p>
                            <p class="text-sm text-gray-500 mt-2">Created: ${notification.createdAt}</p>
                            <c:if test="${!notification.active}">
                                <span class="inline-block bg-gray-200 text-gray-700 px-2 py-1 rounded text-sm mt-2">Inactive</span>
                            </c:if>
                        </div>
                        <div class="flex space-x-2">
                            <a href="notifications?action=edit&id=${notification.notificationId}" 
                               class="bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700">Edit</a>
                            <form action="notifications" method="post" class="inline">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="notificationId" value="${notification.notificationId}">
                                <button type="submit" 
                                        onclick="return confirm('Are you sure you want to delete this notification?')"
                                        class="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700">
                                    Delete
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- Other Admin Sections -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-white p-6 rounded-lg shadow-md">
            <h2 class="text-xl font-semibold mb-4">Vehicle Management</h2>
            <a href="vehicles?action=manage" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
                Manage Vehicles
            </a>
        </div>
        
        <div class="bg-white p-6 rounded-lg shadow-md">
            <h2 class="text-xl font-semibold mb-4">Booking Management</h2>
            <a href="bookings?action=manage" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
                Manage Bookings
            </a>
        </div>
    </div>
</main>

<footer class="bg-blue-900 text-white text-center py-4 mt-8">
    <p>© 2025 VehicleRentWebApp. All rights reserved.</p>
</footer>
</body>
</html> 