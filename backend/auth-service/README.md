# Auth Service

Handles Authentication, Password Hashing (BCrypt), JWT Token Generation, and User/Technician Registration Events.

## Port
- 8081

## Database
- MongoDB (`auth_db`)

## Endpoints
- `POST /api/auth/register` - User / Technician / Admin Registration
- `POST /api/auth/login` - Authenticate & Get JWT
- `GET /api/auth/validate` - Validate Token
