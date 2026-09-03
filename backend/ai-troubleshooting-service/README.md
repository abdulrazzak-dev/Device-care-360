# AI Troubleshooting Service

Houses Google Gemini AI Integration & Deterministic Safety Engine for hazard evaluation.

## Port
- 8084

## Database
- MongoDB (`troubleshooting_db`)

## Safety Engine Features
- Rule-based detection of electrical, battery thermal runaway, and gas pressure hazards.
- Forces `riskLevel = CRITICAL/HIGH` and `requiresProfessional = true`.
- Emits `HighRiskIssueDetectedEvent` via RabbitMQ.

## Endpoints
- `POST /api/troubleshooting/analyze`
- `POST /api/troubleshooting/chat`
- `POST /api/troubleshooting/category-search`
- `POST /api/troubleshooting/brand-search`
- `POST /api/troubleshooting/issue-search`
- `GET /api/troubleshooting/history/{userId}`
