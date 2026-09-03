# Booking Service

Manages repair appointments, status transitions (`PENDING`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`), and booking lifecycle events.

## Port
- 8087

## Database
- MongoDB (`booking_db`)

## Endpoints
- `POST /api/bookings`
- `GET /api/bookings/{id}`
- `GET /api/bookings/user/{userId}`
- `GET /api/bookings/technician/{technicianId}`
- `PATCH /api/bookings/{id}/status`
- `PUT /api/bookings/{id}/reschedule`
- `DELETE /api/bookings/{id}`
