<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.vehiclerentwebapp.Vehicle" %>
<%@ page import="com.example.vehiclerentwebapp.Booking" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Booking - VehicleRent</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 font-sans">
<header class="bg-blue-900 text-white">
    <nav class="container mx-auto px-4 py-4 flex justify-between items-center">
        <div class="text-2xl font-bold">VehicleRent</div>
        <ul class="flex space-x-6">
            <li><a href="index.jsp" class="hover:text-blue-300">Home</a></li>
            <li><a href="vehicles" class="hover:text-blue-300">Vehicles</a></li>
            <li><a href="dashboard" class="hover:text-blue-300">Dashboard</a></li>
            <li><a href="logout" class="hover:text-blue-300">Logout</a></li>
        </ul>
    </nav>
</header>

<main class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold text-gray-800 mb-6">Edit Booking</h1>

    <%
        Booking booking = (Booking) request.getAttribute("booking");
        Vehicle vehicle = (Vehicle) request.getAttribute("vehicle");
        String error = request.getParameter("error");
    %>

    <% if (booking == null || vehicle == null) { %>
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
        Error: Booking or vehicle not found.
    </div>
    <a href="dashboard" class="inline-block bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Back to Dashboard</a>
    <% } else { %>
    <% if (error != null) { %>
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
        <%= error %>
    </div>
    <% } %>

    <div class="bg-white p-6 rounded-lg shadow-md mb-8">
        <h2 class="text-xl font-semibold mb-4">Vehicle Details</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <p class="text-gray-600"><span class="font-medium">Brand:</span> <%= vehicle.getBrand() %></p>
            <p class="text-gray-600"><span class="font-medium">Model:</span> <%= vehicle.getModel() %></p>
            <p class="text-gray-600"><span class="font-medium">Type:</span> <%= vehicle.getType() %></p>
            <p class="text-gray-600"><span class="font-medium">Year:</span> <%= vehicle.getYear() %></p>
            <p class="text-gray-600"><span class="font-medium">Price/Day:</span> $<%= String.format("%.2f", vehicle.getPricePerDay()) %></p>
        </div>
    </div>

    <div class="bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-xl font-semibold mb-4">Edit Booking Form</h2>
        <form id="bookingForm" action="bookings" method="post" enctype="multipart/form-data" onsubmit="return validateForm()">
            <input type="hidden" name="action" value="edit">
            <input type="hidden" name="bookingId" value="<%= booking.getBookingId() %>">
            <input type="hidden" name="vehicleId" value="<%= vehicle.getVehicleId() %>">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                <div>
                    <label for="startDate" class="block text-sm font-medium text-gray-700">Start Date</label>
                    <input type="date" id="startDate" name="startDate" value="<%= booking.getStartDate() %>" required
                           class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div>
                    <label for="endDate" class="block text-sm font-medium text-gray-700">End Date</label>
                    <input type="date" id="endDate" name="endDate" value="<%= booking.getEndDate() %>" required
                           class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
                </div>
            </div>
            <div class="mb-4">
                <label for="totalPrice" class="block text-sm font-medium text-gray-700">Total Price</label>
                <input type="text" id="totalPrice" readonly
                       class="mt-1 p-2 w-full border rounded bg-gray-100" value="$<%= String.format("%.2f", booking.getTotalPrice()) %>">
            </div>
            <div class="mb-4">
                <label for="paymentSlip" class="block text-sm font-medium text-gray-700">Payment Slip (JPEG, max 5MB, optional)</label>
                <input type="file" id="paymentSlip" name="paymentSlip" accept="image/jpeg"
                       class="mt-1 p-2 w-full border rounded focus:ring-blue-500 focus:border-blue-500">
            </div>
            <div id="formError" class="hidden text-red-700 text-sm mb-4"></div>
            <div class="flex justify-end space-x-2">
                <a href="dashboard" class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600">Cancel</a>
                <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Update Booking</button>
            </div>
        </form>
    </div>
    <% } %>
</main>

<footer class="bg-blue-900 text-white text-center py-4">
    <p>© 2025 VehicleRent. All rights reserved.</p>
</footer>

<script>
    console.log('Edit booking script started');
    try {
        const form = document.getElementById('bookingForm');
        const startDateInput = document.getElementById('startDate');
        const endDateInput = document.getElementById('endDate');
        const totalPriceInput = document.getElementById('totalPrice');
        const formError = document.getElementById('formError');
        const pricePerDay = Number('<%= vehicle != null ? vehicle.getPricePerDay() : 0 %>');
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        console.log('Price per day:', pricePerDay);
        console.log('Form elements:', { startDateInput, endDateInput, totalPriceInput, formError });

        if (!startDateInput || !endDateInput || !totalPriceInput || !formError) {
            console.error('Form elements not found');
            formError.textContent = 'Form initialization error. Please try again.';
            formError.classList.remove('hidden');
            throw new Error('Missing form elements');
        }

        startDateInput.min = today.toISOString().split('T')[0];
        endDateInput.min = today.toISOString().split('T')[0];

        function calculateTotalPrice() {
            console.log('Calculating total price');
            const startDate = new Date(startDateInput.value);
            const endDate = new Date(endDateInput.value);
            formError.classList.add('hidden');

            if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
                totalPriceInput.value = '$0.00';
                return;
            }

            if (endDate >= startDate) {
                const timeDiff = endDate - startDate;
                const days = Math.max(1, Math.ceil(timeDiff / (1000 * 60 * 60 * 24)));
                const total = days * pricePerDay;
                totalPriceInput.value = `$${total.toFixed(2)}`;
                console.log('Days:', days, 'Total:', total);
            } else {
                totalPriceInput.value = '$0.00';
            }
        }

        function validateForm() {
            console.log('Validating form');
            const startDate = new Date(startDateInput.value);
            const endDate = new Date(endDateInput.value);
            const paymentSlip = document.getElementById('paymentSlip').files[0];
            formError.classList.add('hidden');

            if (!startDateInput.value || !endDateInput.value) {
                showError('Please select both start and end dates.');
                return false;
            }

            if (startDate < today) {
                showError('Start date cannot be in the past.');
                return false;
            }

            if (endDate < startDate) {
                showError('End date must be on or after start date.');
                return false;
            }

            if (paymentSlip) {
                if (paymentSlip.type !== 'image/jpeg') {
                    showError('Payment slip must be a JPEG file.');
                    return false;
                }

                if (paymentSlip.size > 5 * 1024 * 1024) {
                    showError('Payment slip must be less than 5MB.');
                    return false;
                }
            }

            console.log('Form validation passed');
            return true;
        }

        function showError(message) {
            formError.textContent = message;
            formError.classList.remove('hidden');
            console.error('Validation error:', message);
        }

        startDateInput.addEventListener('change', () => {
            console.log('Start date changed:', startDateInput.value);
            calculateTotalPrice();
        });
        endDateInput.addEventListener('change', () => {
            console.log('End date changed:', endDateInput.value);
            calculateTotalPrice();
        });

        form.addEventListener('submit', (e) => {
            console.log('Form submitted:', {
                action: form.action,
                bookingId: form.bookingId.value,
                vehicleId: form.vehicleId.value,
                startDate: form.startDate.value,
                endDate: form.endDate.value,
                paymentSlip: form.paymentSlip.files[0]?.name
            });
        });

        console.log('Edit booking script initialized');
    } catch (e) {
        console.error('Script error:', e);
        const formError = document.getElementById('formError');
        if (formError) {
            formError.textContent = 'An error occurred. Please refresh the page.';
            formError.classList.remove('hidden');
        }
    }
</script>
</body>
</html>
```