<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Manage Notifications</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .notification-card {
            margin-bottom: 1rem;
            border: 1px solid #dee2e6;
            border-radius: 0.25rem;
            padding: 1rem;
        }
        .notification-card.inactive {
            background-color: #f8f9fa;
            opacity: 0.7;
        }
    </style>
</head>
<body>
    <div class="container mt-4">
        <h2>Manage Notifications</h2>
        
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger">${param.error}</div>
        </c:if>
        <c:if test="${not empty param.success}">
            <div class="alert alert-success">${param.success}</div>
        </c:if>

        <!-- Create Notification Form -->
        <div class="card mb-4">
            <div class="card-header">
                <h4>Create New Notification</h4>
            </div>
            <div class="card-body">
                <form action="notifications" method="post">
                    <input type="hidden" name="action" value="create">
                    <div class="mb-3">
                        <label for="title" class="form-label">Title</label>
                        <input type="text" class="form-control" id="title" name="title" required>
                    </div>
                    <div class="mb-3">
                        <label for="message" class="form-label">Message</label>
                        <textarea class="form-control" id="message" name="message" rows="3" required></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary">Create Notification</button>
                </form>
            </div>
        </div>

        <!-- List of Notifications -->
        <h4>Existing Notifications</h4>
        <c:forEach items="${notifications}" var="notification">
            <div class="notification-card ${notification.active ? '' : 'inactive'}">
                <div class="d-flex justify-content-between align-items-start">
                    <div>
                        <h5>${notification.title}</h5>
                        <p class="mb-1">${notification.message}</p>
                        <small class="text-muted">Created: ${notification.createdAt}</small>
                        <c:if test="${!notification.active}">
                            <span class="badge bg-secondary ms-2">Inactive</span>
                        </c:if>
                    </div>
                    <div>
                        <a href="notifications?action=edit&id=${notification.notificationId}" 
                           class="btn btn-sm btn-primary">Edit</a>
                        <form action="notifications" method="post" class="d-inline">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="notificationId" value="${notification.notificationId}">
                            <button type="submit" class="btn btn-sm btn-danger" 
                                    onclick="return confirm('Are you sure you want to delete this notification?')">
                                Delete
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html> 