# Device Service

Manages device categories, brands, common issues, and user-registered device inventories.

## Port
- 8083

## Database
- MongoDB (`device_db`)

## Endpoints
- `GET /api/devices/categories`
- `GET /api/devices/brands`
- `GET /api/devices/issues`
- `POST /api/devices`
- `GET /api/devices/user/{userId}`
- `PUT /api/devices/{id}`
- `DELETE /api/devices/{id}`
