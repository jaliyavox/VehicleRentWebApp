<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Notification - Vehicle Rental</title>
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
    <div class="max-w-2xl mx-auto">
        <h1 class="text-3xl font-bold text-gray-800 mb-6">Edit Notification</h1>

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

        <div class="bg-white p-6 rounded-lg shadow-md">
            <form action="notifications" method="post" class="space-y-4">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="notificationId" value="${notification.notificationId}">
                
                <div>
                    <label for="title" class="block text-sm font-medium text-gray-700">Title</label>
                    <input type="text" id="title" name="title" value="${notification.title}" required
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500">
                </div>
                
                <div>
                    <label for="message" class="block text-sm font-medium text-gray-700">Message</label>
                    <textarea id="message" name="message" rows="3" required
                              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500">${notification.message}</textarea>
                </div>
                
                <div class="flex items-center">
                    <input type="checkbox" id="active" name="active" ${notification.active ? 'checked' : ''}
                           class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded">
                    <label for="active" class="ml-2 block text-sm text-gray-700">
                        Active
                    </label>
                </div>
                
                <div class="flex space-x-4">
                    <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
                        Update Notification
                    </button>
                    <a href="admin" class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600">
                        Cancel
                    </a>
                </div>
            </form>
        </div>
    </div>
</main>

<footer class="bg-blue-900 text-white text-center py-4 mt-8">
    <p>© 2025 VehicleRentWebApp. All rights reserved.</p>
</footer>
</body>
</html> 