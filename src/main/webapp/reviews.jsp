<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.vehiclerentwebapp.ReviewServlet.Review" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reviews - Vehicle Rental</title>
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
    <h1 class="text-3xl font-bold text-gray-800 mb-6">Service Reviews</h1>

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

    <!-- Add Review Form -->
    <div class="bg-white p-6 rounded-lg shadow-md mb-6">
        <h2 class="text-xl font-semibold mb-4">Add a Review</h2>
        <form action="reviews" method="post">
            <input type="hidden" name="action" value="add">
            <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700">Rating (1-5)</label>
                <select name="rating" class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500" required>
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                    <option value="4">4</option>
                    <option value="5">5</option>
                </select>
            </div>
            <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700">Comment</label>
                <textarea name="comment" class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500" rows="4" maxlength="500" required></textarea>
            </div>
            <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Submit Review</button>
        </form>
    </div>

    <!-- Display Reviews -->
    <div class="bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-xl font-semibold mb-4">Existing Reviews</h2>
        <%
            List<Review> reviews = (List<Review>) request.getAttribute("reviews");
            if (reviews == null || reviews.isEmpty()) {
        %>
        <p class="text-gray-600">No reviews yet.</p>
        <% } else { %>
        <% for (Review review : reviews) { %>
        <div class="border-b py-4">
            <p><strong><%= review.getUsername() %></strong> rated: <%= review.getRating() %>/5</p>
            <p class="text-gray-600"><%= review.getComment() %></p>
            <p class="text-sm text-gray-500">Posted on: <%= review.getTimestamp() %></p>
            <% if (review.getUsername().equals(session.getAttribute("username"))) { %>
            <div class="mt-2 flex space-x-2">
                <button onclick="openUpdateModal('<%= review.getRating() %>', '<%= review.getComment().replace("'", "\\'") %>', '<%= review.getTimestamp() %>')"
                        class="bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-600">Edit</button>
                <form action="reviews" method="post" class="inline">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="timestamp" value="<%= review.getTimestamp() %>">
                    <button type="submit" class="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600">Delete</button>
                </form>
            </div>
            <% } %>
        </div>
        <% } %>
        <% } %>
    </div>

    <!-- Update Review Modal -->
    <div id="updateModal" class="modal">
        <div class="modal-content">
            <h2 class="text-xl font-semibold mb-4">Update Review</h2>
            <form action="reviews" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="timestamp" id="updateTimestamp">
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Rating (1-5)</label>
                    <select name="rating" id="updateRating" class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500" required>
                        <option value="1">1</option>
                        <option value="2">2</option>
                        <option value="3">3</option>
                        <option value="4">4</option>
                        <option value="5">5</option>
                    </select>
                </div>
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Comment</label>
                    <textarea name="comment" id="updateComment" class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500" rows="4" maxlength="500" required></textarea>
                </div>
                <div class="flex justify-end space-x-2">
                    <button type="button" onclick="closeUpdateModal()" class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600">Cancel</button>
                    <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Save</button>
                </div>
            </form>
        </div>
    </div>
</main>

<footer class="bg-blue-900 text-white text-center py-4">
    <p>© 2025 VehicleRentWebApp. All rights reserved.</p>
</footer>

<script>
    function openUpdateModal(rating, comment, timestamp) {
        document.getElementById('updateRating').value = rating;
        document.getElementById('updateComment').value = comment;
        document.getElementById('updateTimestamp').value = timestamp;
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