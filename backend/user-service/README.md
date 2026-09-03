# User Service

Manages user profile data and reacts asynchronously to UserRegisteredEvent.

## Port
- 8082

## Database
- MongoDB (`user_db`)

## Endpoints
- `GET /api/users/me` - Get logged-in user profile
- `GET /api/users/{id}` - Get profile by ID
- `PUT /api/users/{id}` - Update profile
- `DELETE /api/users/{id}` - Soft delete profile
