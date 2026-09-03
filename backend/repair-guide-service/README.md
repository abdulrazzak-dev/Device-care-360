# Repair Guide Service

Manages official repair guides, safety instructions, documentation links, and maintenance recommendations.

## Port
- 8085

## Database
- MongoDB (`repair_guide_db`)

## Endpoints
- `GET /api/repair-guides`
- `GET /api/repair-guides/{id}`
- `POST /api/repair-guides` (Admin)
- `PUT /api/repair-guides/{id}` (Admin)
- `DELETE /api/repair-guides/{id}` (Admin)
