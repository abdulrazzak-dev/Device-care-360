# Payment Service

Handles decoupled payment processing via provider abstraction (`Stripe`, `PayHere`, `Mock`), payment history, and financial receipts.

## Port
- 8088

## Database
- MongoDB (`payment_db`)

## Endpoints
- `POST /api/payments`
- `GET /api/payments/{id}`
- `GET /api/payments/user/{userId}`
- `GET /api/payments/booking/{bookingId}`
