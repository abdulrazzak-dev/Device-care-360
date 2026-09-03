# Admin Service

Provides centralized administrative capabilities, dashboard aggregation, technician verification workflows, and audit logging without owning other services' databases.

## Port
- 8091

## Database
- MongoDB (`admin_db`)

## Endpoints
- `GET /api/admin/dashboard`
- `PUT /api/admin/technicians/{id}/verify`
