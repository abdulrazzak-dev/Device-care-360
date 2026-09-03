# Technician Service

Manages technician profiles, specializations, availability, admin verification workflows, and review-based rating score aggregation.

## Port
- 8086

## Database
- MongoDB (`technician_db`)

## Endpoints
- `POST /api/technicians/register`
- `GET /api/technicians`
- `GET /api/technicians/{id}`
- `PUT /api/technicians/{id}`
- `GET /api/technicians/search`
- `PUT /api/technicians/{id}/availability`
