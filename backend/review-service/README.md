# Review Service

Manages technician ratings & reviews with strict validation against completed bookings.

## Port
- 8089

## Database
- MongoDB (`review_db`)

## Endpoints
- `POST /api/reviews`
- `GET /api/reviews/technician/{technicianId}`
- `GET /api/reviews/user/{userId}`
