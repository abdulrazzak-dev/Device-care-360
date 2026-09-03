# Notification Service

Asynchronous event consumer for booking status notifications, payment receipts, and high-risk safety alerts.

## Port
- 8090

## Database
- MongoDB (`notification_db`)

## Endpoints
- `GET /api/notifications`
- `PATCH /api/notifications/{id}/read`
