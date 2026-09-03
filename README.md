# DeviceCare 360 – AI-Powered Electronic Device Troubleshooting, Repair Guidance and Technician Booking Platform

DeviceCare 360 is built using a **fully decoupled Java Spring Boot microservices architecture**. Each microservice owns its business domain, database instance, REST contracts, and RabbitMQ event publishers/consumers.

---

## 🚀 Core Technology Stack

* **Java 17** & **Spring Boot 3.2.3**
* **Spring Cloud 2023.0.0** (Spring Cloud Gateway, Eureka Service Discovery, Config Server, OpenFeign)
* **Spring Data MongoDB** (Database-per-service isolation)
* **RabbitMQ** (Asynchronous event-driven messaging)
* **Spring Security & JWT** (Role-based access control: `USER`, `TECHNICIAN`, `ADMIN`)
* **Google Gemini AI API** (Isolated inside AI Troubleshooting Service)
* **Lombok & Jakarta Validation**
* **Springdoc OpenAPI / Swagger**
* **Docker & Docker Compose**

---

## 🏗️ Architecture Overview & Port Allocation

| Service | Port | Database | Role & Purpose |
|---|---|---|---|
| **API Gateway** | 8080 | N/A | Single entry point, routing, JWT authentication filter, CORS, rate limiting. |
| **Config Server** | 8888 | N/A | Centralized Spring Cloud Config properties server. |
| **Discovery Service** | 8761 | N/A | Eureka Service Registration & Dynamic Lookup Server. |
| **Auth Service** | 8081 | `auth_db` | User/Technician registration, BCrypt password hashing, JWT issuing & validation. |
| **User Service** | 8082 | `user_db` | User profile details and settings. Reacts to `UserRegisteredEvent`. |
| **Device Service** | 8083 | `device_db` | Device categories, brands, common issues, user device inventory. |
| **AI Troubleshooting Service** | 8084 | `troubleshooting_db` | Google Gemini AI integration + **Deterministic Safety Engine**. |
| **Repair Guide Service** | 8085 | `repair_guide_db` | Official repair manuals, safety instructions, tool requirements. |
| **Technician Service** | 8086 | `technician_db` | Technician profiles, search, availability, verification, rating calculation. |
| **Booking Service** | 8087 | `booking_db` | Repair appointment lifecycle (`PENDING`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`). |
| **Payment Service** | 8088 | `payment_db` | Decoupled payment provider abstraction (`Stripe`, `PayHere`, `Mock`). |
| **Review Service** | 8089 | `review_db` | Verified completed booking reviews & ratings. |
| **Notification Service** | 8090 | `notification_db` | Asynchronous notification queue consumer (Booking, Payment, Safety alerts). |
| **Admin Service** | 8091 | `admin_db` | Centralized admin dashboard analytics & technician verification workflows. |

---

## 🛡️ Deterministic Safety Engine (AI Troubleshooting Service)

Safety is the highest priority of DeviceCare 360. In addition to Google Gemini AI recommendations, all troubleshooting requests pass through a **Deterministic Safety Engine** that inspects symptom text and AI responses for hazardous conditions:

* **Electrical Hazard**: High voltage, electrical shock, exposed wiring, smoke, sparks, burning smell, mains power, capacitors.
* **Thermal / Battery Hazard**: Swollen battery, expanding battery, battery overheating, lithium thermal runaway, battery fire.
* **Gas / Pressure Hazard**: Refrigerant leaks, gas lines, compressor pressure.

### Safety Override Behavior:
When a hazard keyword is matched:
1. `riskLevel` is forced to `HIGH` or `CRITICAL`.
2. `requiresProfessional` is forced to `true`.
3. Unsafe DIY instructions are stripped and replaced with mandatory safety warnings.
4. `recommendedAction` directs the user to immediately disconnect power and book a certified technician.
5. Emits `HighRiskIssueDetectedEvent` via RabbitMQ to trigger emergency alerts in Notification Service.

---

## 🔑 Environment Variables & Security Configuration

All sensitive secrets are passed via environment variables:

```bash
# MongoDB
export MONGODB_URI=mongodb://localhost:27017/dbname

# RabbitMQ
export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_USER=guest
export RABBITMQ_PASS=guest

# Eureka
export EUREKA_HOST=localhost

# JWT Secret Key
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Google Gemini API
export GEMINI_API_KEY=your_google_gemini_api_key
```

---

## 🛠️ Startup Instructions

### 1. Build All Microservices (Maven)
From the `backend` directory, run:
```bash
cd backend
mvn clean package -DskipTests
```

### 2. Launch Full Microservices Stack (Docker Compose)
From the root directory:
```bash
docker-compose up -d --build
```

### 3. Verify Eureka Service Discovery
Open Eureka Dashboard at `http://localhost:8761` to verify all 11 microservices are registered and UP.

---

## 📖 API Documentation & Links

* **API Gateway**: `http://localhost:8080`
* **Eureka Discovery Dashboard**: `http://localhost:8761`
* **RabbitMQ Management Dashboard**: `http://localhost:15672` (User: `guest`, Pass: `guest`)
* **OpenAPI Specs**: Refer to [`docs/openapi.md`](file:///c:/Users/FSL/Desktop/Device%20care%20360%202/docs/openapi.md)
