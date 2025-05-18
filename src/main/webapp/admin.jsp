<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.io.*,java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Vehicle Rental</title>
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
    <h1 class="text-3xl font-bold text-gray-800 mb-6">Admin Dashboard</h1>
    <p class="text-lg text-gray-600 mb-6">Welcome, <%= session.getAttribute("fullName") != null ? session.getAttribute("fullName") : "Admin" %>!</p>

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

    <!-- User Management -->
    <div class="bg-white p-6 rounded-lg shadow-md mb-8">
        <h2 class="text-xl font-semibold mb-4">Manage Users</h2>
        <%
            String usersFile = application.getRealPath("/WEB-INF/users.txt");
            File usersFileObj = new File(usersFile);
            if (!usersFileObj.exists() || !usersFileObj.canRead()) {
        %>
        <p class="text-red-600">Error: Cannot access users.txt at <%= usersFile %></p>
        <%
        } else {
        %>
        <table class="w-full border-collapse">
            <thead>
            <tr class="bg-gray-200">
                <th class="border p-2">Username</th>
                <th class="border p-2">Email</th>
                <th class="border p-2">Full Name</th>
                <th class="border p-2">Actions</th>
            </tr>
            </thead>
            <tbody>
            <%
                try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length == 4) {
            %>
            <tr>
                <td class="border p-2"><%= parts[0] %></td>
                <td class="border p-2"><%= parts[1] %></td>
                <td class="border p-2"><%= parts[2] %></td>
                <td class="border p-2">
                    <button onclick="openUpdateUserModal('<%= parts[0] %>', '<%= parts[1] %>', '<%= parts[2] %>')"
                            class="bg-blue-600 text-white px-2 py-1 rounded hover:bg-blue-700">Update</button>
                    <form action="user-management" method="post" class="inline">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="username" value="<%= parts[0] %>">
                        <button type="submit" class="bg-red-600 text-white px-2 py-1 rounded hover:bg-red-700"
                                onclick="return confirm('Are you sure you want to delete <%= parts[0] %>?')">Delete</button>
                    </form>
                </td>
            </tr>
            <%
                    }
                }
            } catch (IOException e) {
            %>
            <tr><td colspan="4" class="text-red-600">Error reading users: <%= e.getMessage() %></td></tr>
            <%
                }
            %>
            </tbody>
        </table>
        <%
            }
        %>
    </div>

    <!-- Update User Modal -->
    <div id="updateUserModal" class="modal">
        <div class="modal-content">
            <h2 class="text-xl font-semibold mb-4">Update User</h2>
            <form action="user-management" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" id="updateUserUsername" name="username">
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Email</label>
                    <input type="email" id="updateUserEmail" name="email" required
                           class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Full Name</label>
                    <input type="text" id="updateUserFullName" name="fullName" required
                           class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div class="flex justify-end space-x-2">
                    <button type="button" onclick="closeUpdateUserModal()"
                            class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600">Cancel</button>
                    <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Save</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Admin Management -->
    <div class="bg-white p-6 rounded-lg shadow-md mb-8">
        <h2 class="text-xl font-semibold mb-4">Manage Admins</h2>
        <!-- Add Admin Form -->
        <form action="admin-management" method="post" class="mb-6">
            <input type="hidden" name="action" value="add">
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                <input type="text" name="username" placeholder="Username" class="border p-2 rounded" required>
                <input type="email" name="email" placeholder="Email" class="border p-2 rounded" required>
                <input type="text" name="fullName" placeholder="Full Name" class="border p-2 rounded" required>
                <input type="password" name="password" placeholder="Password" class="border p-2 rounded" required>
            </div>
            <button type="submit" class="bg-blue-600 text-white px-4 py-2 mt-4 rounded hover:bg-blue-700">Add Admin</button>
        </form>

        <!-- Admin List -->
        <%
            String adminFile = application.getRealPath("/WEB-INF/admin.txt");
            File adminFileObj = new File(adminFile);
            if (!adminFileObj.exists() || !adminFileObj.canRead()) {
        %>
        <p class="text-red-600">Error: Cannot access admin.txt at <%= adminFile %></p>
        <%
        } else {
        %>
        <table class="w-full border-collapse">
            <thead>
            <tr class="bg-gray-200">
                <th class="border p-2">Username</th>
                <th class="border p-2">Email</th>
                <th class="border p-2">Full Name</th>
                <th class="border p-2">Actions</th>
            </tr>
            </thead>
            <tbody>
            <%
                try (BufferedReader reader = new BufferedReader(new FileReader(adminFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length == 4) {
            %>
            <tr>
                <td class="border p-2"><%= parts[0] %></td>
                <td class="border p-2"><%= parts[1] %></td>
                <td class="border p-2"><%= parts[2] %></td>
                <td class="border p-2">
                    <button onclick="openUpdateAdminModal('<%= parts[0] %>', '<%= parts[1] %>', '<%= parts[2] %>')"
                            class="bg-blue-600 text-white px-2 py-1 rounded hover:bg-blue-700">Update</button>
                    <form action="admin-management" method="post" class="inline">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="username" value="<%= parts[0] %>">
                        <button type="submit" class="bg-red-600 text-white px-2 py-1 rounded hover:bg-red-700"
                                onclick="return confirm('Are you sure you want to delete <%= parts[0] %>?')">Delete</button>
                    </form>
                </td>
            </tr>
            <%
                    }
                }
            } catch (IOException e) {
            %>
            <tr><td colspan="4" class="text-red-600">Error reading admins: <%= e.getMessage() %></td></tr>
            <%
                }
            %>
            </tbody>
        </table>
        <%
            }
        %>
    </div>

    <!-- Update Admin Modal -->
    <div id="updateAdminModal" class="modal">
        <div class="modal-content">
            <h2 class="text-xl font-semibold mb-4">Update Admin</h2>
            <form action="admin-management" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" id="updateAdminUsername" name="username">
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Email</label>
                    <input type="email" id="updateAdminEmail" name="email" required
                           class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Full Name</label>
                    <input type="text" id="updateAdminFullName" name="fullName" required
                           class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div class="flex justify-end space-x-2">
                    <button type="button" onclick="closeUpdateAdminModal()"
                            class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600">Cancel</button>
                    <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Save</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Review Management -->
    <div class="bg-white p-6 rounded-lg shadow-md mb-8">
        <h2 class="text-xl font-semibold mb-4">Manage Reviews</h2>
        <%
            String reviewsFile = application.getRealPath("/WEB-INF/reviews.txt");
            File reviewsFileObj = new File(reviewsFile);
            if (!reviewsFileObj.exists() || !reviewsFileObj.canRead()) {
        %>
        <p class="text-red-600">Error: Cannot access reviews.txt at <%= reviewsFile %></p>
        <%
        } else {
        %>
        <table class="w-full border-collapse">
            <thead>
            <tr class="bg-gray-200">
                <th class="border p-2">Username</th>
                <th class="border p-2">Rating</th>
                <th class="border p-2">Comment</th>
                <th class="border p-2">Timestamp</th>
                <th class="border p-2">Actions</th>
            </tr>
            </thead>
            <tbody>
            <%
                try (BufferedReader reader = new BufferedReader(new FileReader(reviewsFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split(":", 6);
                        if (parts.length == 6) {
            %>
            <tr>
                <td class="border p-2"><%= parts[1] %></td>
                <td class="border p-2"><%= parts[2] %></td>
                <td class="border p-2"><%= parts[3] %></td>
                <td class="border p-2"><%= parts[5] %></td>
                <td class="border p-2">
                    <form action="review-management" method="post" class="inline">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="reviewId" value="<%= parts[0] %>">
                        <button type="submit" class="bg-red-600 text-white px-2 py-1 rounded hover:bg-red-700"
                                onclick="return confirm('Are you sure you want to delete this review?')">Delete</button>
                    </form>
                </td>
            </tr>
            <%
                    }
                }
            } catch (IOException e) {
            %>
            <tr><td colspan="5" class="text-red-600">Error reading reviews: <%= e.getMessage() %></td></tr>
            <%
                }
            %>
            </tbody>
        </table>
        <%
            }
        %>
    </div>

    <!-- Notification Management -->
    <div class="bg-white p-6 rounded-lg shadow-md mb-8">
        <h2 class="text-xl font-semibold mb-4">Manage Notifications</h2>

        <!-- Add Notification Form -->
        <form action="notifications" method="post" class="mb-6">
            <input type="hidden" name="action" value="create">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Title</label>
                    <input type="text" name="title" placeholder="Notification Title" required
                           class="w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Message</label>
                    <input type="text" name="message" placeholder="Notification Message" required
                           class="w-full border p-2 rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
            </div>
            <div class="mt-4">
                <label class="inline-flex items-center">
                    <input type="checkbox" name="active" value="true" checked
                           class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50">
                    <span class="ml-2 text-sm text-gray-700">Active</span>
                </label>
            </div>
            <button type="submit" class="bg-blue-600 text-white px-4 py-2 mt-4 rounded hover:bg-blue-700">
                Add Notification
            </button>
        </form>

        <!-- Notifications List -->
        <%
            String notificationsFile = application.getRealPath("/WEB-INF/notifications.txt");
            File notificationsFileObj = new File(notificationsFile);
            if (!notificationsFileObj.exists() || !notificationsFileObj.canRead()) {
        %>
        <p class="text-red-600">Error: Cannot access notifications.txt at <%= notificationsFile %></p>
        <%
        } else {
        %>
        <div class="overflow-x-auto">
            <table class="w-full border-collapse">
                <thead>
                <tr class="bg-gray-200">
                    <th class="border p-2">Title</th>
                    <th class="border p-2">Message</th>
                    <th class="border p-2">Created At</th>
                    <th class="border p-2">Status</th>
                    <th class="border p-2">Actions</th>
                </tr>
                </thead>
                <tbody>
                <%
                    try (BufferedReader reader = new BufferedReader(new FileReader(notificationsFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            try {
                                String[] parts = line.split("\\|");
                                if (parts.length >= 4) {
                                    String notificationId = parts[0];
                                    String title = parts[1];
                                    String message = parts[2];
                                    String createdAt = parts[3];
                                    boolean isActive = parts.length > 4 && parts[4].equals("true");
                %>
                <tr class="<%= isActive ? "" : "bg-gray-50" %>">
                    <td class="border p-2"><%= title %></td>
                    <td class="border p-2"><%= message %></td>
                    <td class="border p-2"><%= createdAt %></td>
                    <td class="border p-2">
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium <%= isActive ? "bg-green-100 text-green-800" : "bg-gray-100 text-gray-800" %>">
                            <%= isActive ? "Active" : "Inactive" %>
                        </span>
                    </td>
                    <td class="border p-2">
                        <button class="edit-notification-btn bg-blue-600 text-white px-2 py-1 rounded hover:bg-blue-700"
                                data-id="<%= notificationId %>"
                                data-title="<%= title %>"
                                data-message="<%= message %>"
                                data-active="<%= isActive %>">Edit</button>
                        <form action="notifications" method="post" class="inline">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="notificationId" value="<%= notificationId %>">
                            <button type="submit" class="bg-red-600 text-white px-2 py-1 rounded hover:bg-red-700"
                                    onclick="return confirm('Are you sure you want to delete this notification?')">Delete</button>
                        </form>
                    </td>
                </tr>
                <%
                                }
                            } catch (Exception e) {
                %>
                <tr><td colspan="5" class="text-red-600">Error processing notification: <%= e.getMessage() %></td></tr>
                <%
                            }
                        }
                    } catch (IOException e) {
                %>
                <tr><td colspan="5" class="text-red-600">Error reading notifications: <%= e.getMessage() %></td></tr>
                <%
                    }
                %>
                </tbody>
            </table>
        </div>
        <%
            }
        %>
    </div>

    <!-- Update Notification Modal -->
    <div id="updateNotificationModal" class="modal">
        <div class="modal-content">
            <h2 class="text-xl font-semibold mb-4">Update Notification</h2>
            <form action="notifications" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" id="updateNotificationId" name="notificationId">
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Title</label>
                    <input type="text" id="updateNotificationTitle" name="title" required
                           class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700">Message</label>
                    <input type="text" id="updateNotificationMessage" name="message" required
                           class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div class="mb-4">
                    <label class="inline-flex items-center">
                        <input type="checkbox" id="updateNotificationActive" name="active" value="true"
                               class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50">
                        <span class="ml-2 text-sm text-gray-700">Active</span>
                    </label>
                </div>
                <div class="flex justify-end space-x-2">
                    <button type="button" onclick="closeUpdateNotificationModal()"
                            class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600">Cancel</button>
                    <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Save</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Vehicle and Booking Management Links -->
    <div class="bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-xl font-semibold mb-4">Other Actions</h2>
        <p class="text-gray-600 mb-4">Manage vehicles and bookings.</p>
        <a href="vehicle-management" class="inline-block bg-blue-600 text-white px-4 py-2 mr-2 rounded hover:bg-blue-700">Manage Vehicles</a>
        <a href="booking-management" class="inline-block bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Manage Bookings</a>
    </div>
</main>

<footer class="bg-blue-900 text-white text-center py-4">
    <p>© 2025 VehicleRentWebApp. All rights reserved.</p>
</footer>

<script>
    // User Modal Functions
    function openUpdateUserModal(username, email, fullName) {
        document.getElementById('updateUserUsername').value = username;
        document.getElementById('updateUserEmail').value = email;
        document.getElementById('updateUserFullName').value = fullName;
        document.getElementById('updateUserModal').style.display = 'block';
    }

    function closeUpdateUserModal() {
        document.getElementById('updateUserModal').style.display = 'none';
    }

    // Admin Modal Functions
    function openUpdateAdminModal(username, email, fullName) {
        document.getElementById('updateAdminUsername').value = username;
        document.getElementById('updateAdminEmail').value = email;
        document.getElementById('updateAdminFullName').value = fullName;
        document.getElementById('updateAdminModal').style.display = 'block';
    }

    function closeUpdateAdminModal() {
        document.getElementById('updateAdminModal').style.display = 'none';
    }

    // Notification Modal Functions
    document.addEventListener('DOMContentLoaded', function() {
        document.querySelectorAll('.edit-notification-btn').forEach(button => {
            button.addEventListener('click', function() {
                const id = this.getAttribute('data-id');
                const title = this.getAttribute('data-title');
                const message = this.getAttribute('data-message');
                const active = this.getAttribute('data-active') === 'true';

                document.getElementById('updateNotificationId').value = id;
                document.getElementById('updateNotificationTitle').value = title;
                document.getElementById('updateNotificationMessage').value = message;
                document.getElementById('updateNotificationActive').checked = active;
                document.getElementById('updateNotificationModal').style.display = 'block';
            });
        });
    });

    function closeUpdateNotificationModal() {
        document.getElementById('updateNotificationModal').style.display = 'none';
    }

    // Close modals if clicking outside
    window.onclick = function(event) {
        let userModal = document.getElementById('updateUserModal');
        let adminModal = document.getElementById('updateAdminModal');
        let notificationModal = document.getElementById('updateNotificationModal');
        if (event.target == userModal) {
            closeUpdateUserModal();
        }
        if (event.target == adminModal) {
            closeUpdateAdminModal();
        }
        if (event.target == notificationModal) {
            closeUpdateNotificationModal();
        }
    }
</script>
</body>
</html>