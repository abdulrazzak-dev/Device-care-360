# Config Server

Centralized Spring Cloud Config Server for DeviceCare 360 microservices.

## Port
- 8888

## Endpoints
- `GET /{application}/{profile}` - Retrieve configuration for a specific microservice.
- `GET /actuator/health` - Health check.
