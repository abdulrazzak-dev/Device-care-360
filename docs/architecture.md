# DeviceCare 360 Microservice Architecture

## Topology

```text
React Frontend
      |
      v (Port 8080)
+----------------------------------------------------+
|                Spring Cloud Gateway                |
+----------------------------------------------------+
  |   |   |   |   |   |   |   |   |   |   |
  v   v   v   v   v   v   v   v   v   v   v
[Auth][User][Device][AI-Trouble][Guide][Tech][Book][Pay][Review][Notif][Admin]
  8081 8082   8083     8084     8085  8086 8087 8088  8089   8090   8091
```

## Database Per Service
Each service maintains its own isolated database instance:
- `auth_db`
- `user_db`
- `device_db`
- `troubleshooting_db`
- `repair_guide_db`
- `technician_db`
- `booking_db`
- `payment_db`
- `review_db`
- `notification_db`
- `admin_db`

## Inter-Service Messaging
Domain events are published to RabbitMQ Topic Exchange `devicecare.events`.
- `user.registered` -> `user.service.registered.queue`
- `technician.registered` -> `technician.service.registered.queue`
- `review.created` -> `technician.service.review.queue`
- `troubleshooting.highrisk` -> `notification.events.queue`
- `booking.*` -> `notification.events.queue`
- `payment.completed` -> `notification.events.queue`
