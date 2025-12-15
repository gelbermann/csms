# CSMS Project Overview

## Project Summary

**CSMS** (Charging Station Management System) is a Spring Boot-based microservice that handles EV charging station authorization flows using asynchronous Kafka messaging. The system validates driver authentication requests through a distributed event-driven architecture.

**Project Name:** csms  
**Architecture:** Monorepo with modular structure  

## 💡 Personal notes 💡

Below is a comprehensive overview of the CSMS project, including its architecture, tech stack, setup instructions, testing strategy, and future enhancements. 

Before diving into the details, here are some personal notes, thoughts and assumptions made during development:

### 🤔 Assumptions 🤔
- There is only one form of driver identifier for simplicity. The logic is easily extensible to support multiple types.
- DTO naming is inconsistent (authorization vs authentication) to match the provided PDF and reduce confusion. Ideally, I would standardize the names.

### 📈 Taking this to production (scaling up) 📈

#### Token Status Management
I'm using an in-memory map for `TokenStatusProvider` to simulate a database or external service. In a real-world scenario, this would be replaced with a proper persistence layer.

#### Multi-instance support (Most interesting point) 
This application is currently designed as a single instance for simplicity.  If the application goes down, all pending authorization requests in the in-memory cache would be lost.

Simply running multiple instances with local caches would lead to inconsistent states across instances:
- Instance A receives a request, passes it to the authentication service via kafka, and caches the pending request.
- The authentication service responds with an authentication status.
- Instance B (not A) receives the response from Kafka, but it has no record of the pending request in its local cache, leading to a failed authorization.

To support multiple instances, we would need a distributed solution for routing authorization responses back to their originating instances.

One possible solution to use Kafka as the input channel for the authentication service, but use Redis Pub/Sub as the response channel. Each instance would subscribe to a unique Redis channel (e.g., based on instance ID) for receiving authorization responses. The flow would be:
1. On startup, each instance generates a unique ID and subscribes to a Redis channel named after that ID.
2. Instance A receives an authorization request:
   1. It caches it locally.
   2. It sends the request to Kafka, along with the Redis channel name.
3. Authentication service processes the request and publishes the response to the specified Redis channel.
4. Instance A receives the response from Redis:
   1. It matches it with the cached request.
   2. It completes the pending authorization and invalidates the cache entry.
   3. It sends the final response back to the client.
5. No other instance is subscribed to that Redis channel, so they won't receive the response.



---

## Tech Stack

### Core Framework & Runtime
- **Spring Boot:** 4.0.0 (latest)
- **Java Version:** 21 (JDK 21)
- **Kotlin:** 2.2.21 (with Spring plugin)
- **Build Tool:** Gradle 9.2.1 with Kotlin DSL
- **Application Type:** RESTful Web Service with Kafka Integration

### Key Dependencies

#### Messaging & Integration
- **Apache Kafka:** `spring-boot-starter-kafka`
- **Spring Kafka Test:** For embedded Kafka testing
- **Jackson Kotlin Module:** JSON serialization for Kotlin data classes

#### Web & REST
- **Spring Web:** `spring-boot-starter-web` (RESTful APIs)

#### Caching
- **Caffeine Cache:** In-memory caching for pending authorization requests

#### Testing
- **JUnit 5:** `kotlin-test-junit5`
- **Mockito:** `mockito-junit-jupiter`
- **AssertJ:** Fluent assertions
- **Spring Boot Test:** Embedded Kafka for E2E tests

#### Development Tools
- **Lombok:** Reduces boilerplate (for Java classes)
- **Spring Boot DevTools:** Docker Compose support for local development

### Infrastructure
- **Docker & Docker Compose:** For local environment setup
- **Kafka:** Confluent Platform 7.6.0
- **Zookeeper:** 7.6.0 (for Kafka coordination)
- **MySQL:** 8.0 (configured but not yet fully integrated)

---

## Architecture Overview

### System Design Pattern
**Event-Driven Architecture** with Request-Response pattern over Kafka

### Main Components

```
┌─────────────────┐
│  API Gateway    │  TransactionController
│  (REST Layer)   │  POST /api/v1/transaction/authorize
└────────┬────────┘
         │
         ▼
┌─────────────────────────────┐
│  Authorization Service      │  (Transaction Service)
│  - Sends auth requests      │
│  - Manages pending requests │
│  - Caffeine cache           │
└────────┬────────────────────┘
         │
         ▼
    ┌────────────┐
    │   Kafka    │
    │   Broker   │
    └─────┬──────┘
          │
    ┌─────┴──────┐
    │            │
    ▼            ▼
[auth-request] [auth-response]
    │            │
    ▼            │
┌──────────────────────────┐  │
│ Authentication Service   │  │
│ - Validates tokens       │  │
│ - Checks token status    │──┘
└──────────────────────────┘
```

---


## Working with the Project

### Prerequisites
- **JDK 21** installed
- **Docker** and **Docker Compose** installed
- **Gradle** (or use included wrapper)

### Quick Reference Commands

```bash
# Environment
docker-compose up -d                    # Start all services
docker-compose down                     # Stop all services

# Build
./gradlew build                         # Full build with tests
./gradlew build -x test                 # Build without tests

# Run
docker-compose up csms-app              # Run in Docker

# Test
./gradlew test                          # Run all tests
./gradlew test --tests AuthorizationE2ETest  # Run specific test
open build/reports/tests/test/index.html     # View test report

# API Testing
curl -X POST http://localhost:8080/api/v1/transaction/authorize \
  -H "Content-Type: application/json" \
  -d '{"stationUuid":"station-123","driverIdentifier":{"id":"driverABC-1234567890"}}'

# Kafka Monitoring
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic auth-request --from-beginning
```

---

#### 1. Raise Local Environment

```bash
# Start all services (Kafka, Zookeeper, MySQL, Application)
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f csms-app

# Stop all services
docker-compose down

# Clean up (remove volumes)
docker-compose down -v
```

**Application URLs:**
- **API:** http://localhost:8080 (local) or http://localhost:8081 (Docker)
- **Kafka:** localhost:9092

---

#### 2. Build the Project

```bash
# Build project (compiles Kotlin + Java)
./gradlew build

# Build without tests
./gradlew build -x test

# Clean build
./gradlew clean build

# Compile only
./gradlew compileKotlin compileJava
```

**Build Output:** `build/libs/csms-0.0.1-SNAPSHOT.jar`

---

#### 3. Run the Application

#### Using Docker
```bash
# Build Docker image
docker build -t csms-app .

# Run container
docker run -p 8080:8080 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092 \
  csms-app
```

---

#### 4. Execute Tests

##### Run All Tests
```bash
./gradlew test
```

##### Run Specific Test Class
```bash
./gradlew test --tests AuthorizationE2ETest
./gradlew test --tests PerformanceE2ETest
```

##### Run with Coverage
```bash
./gradlew test jacocoTestReport
```

##### View Test Reports
```bash
# Open in browser
open build/reports/tests/test/index.html
```

**Test Types:**
- **E2E Tests:** Full integration tests with embedded Kafka (`@EmbeddedKafka`)
- **Unit Tests:** Component-level tests with mocks
- **Test Location:** `src/test/java` and `src/test/kotlin`

---

#### 5. Test the API

##### Authorization Endpoint

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/transaction/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "stationUuid": "station-123",
    "driverIdentifier": {
      "id": "driverABC-1234567890"
    }
  }'
```

**Successful Response (200 OK):**
```json
{
  "authenticationStatus": "ACCEPTED"
}
```

**Test with Disabled Token:**
```bash
curl -X POST http://localhost:8080/api/v1/transaction/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "stationUuid": "station-456",
    "driverIdentifier": {
      "id": "DISABLED_suspended-account-driver-token-abc"
    }
  }'
```

**Response:**
```json
{
  "authenticationStatus": "REJECTED"
}
```

**Test with Invalid Token:**
```bash
curl -X POST http://localhost:8080/api/v1/transaction/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "stationUuid": "station-789",
    "driverIdentifier": {
      "id": "abc"
    }
  }'
```

**Response:**
```json
{
  "authenticationStatus": "INVALID"
}
```

---


#### 6. Common Development Tasks

##### Check Dependencies
```bash
# List all dependencies
./gradlew dependencies

# Check for updates
./gradlew dependencyUpdates
```

##### Format Code
```bash
# Format Kotlin code (if ktlint is configured)
./gradlew ktlintFormat
```

##### View Application Logs
```bash
# Docker Compose
docker-compose logs -f csms-app

# Local logs are in console output
```

---

## Key Design Decisions

### 1. Monorepo Structure
- Single repository with domain-separated packages (`authentication`, `transactions`, `common`)
- Simplifies development for this scale
- Production recommendation: Extract to microservices or separate modules

### 2. Caffeine Cache for Pending Requests
- **Max Size:** 10,000 entries
- **Expiry:** 10 minutes after write
- **Purpose:** Store `CompletableFuture` objects for async request handling
- **Alternative:** Redis for distributed systems

### 3. Kafka Request-Response Pattern
- Uses `@SendTo` annotation for automatic response routing
- Consumer groups ensure proper message distribution
- Error handling via `ErrorHandlingDeserializer`

### 4. Extensible Validation Framework
- Chain of Responsibility pattern
- Spring auto-wiring of validator beans
- Easy to add new validators without modifying consumer logic
- See `AUTHENTICATION_VALIDATOR_SUGGESTIONS.md` for details

### 5. TokenStatusProvider Placeholder
- Currently in-memory map
- Represents database or external service interface
- Easy to replace with real persistence layer

---

## Testing Strategy

### E2E Tests with Embedded Kafka
- **Framework:** Spring Boot Test + Embedded Kafka
- **Base Class:** `BaseE2ETest` provides shared configuration
- **Key Features:**
  - Real Spring context
  - Real Kafka message serialization/deserialization
  - No mocking of Kafka
  - Fast execution (in-memory broker)
  - `@DirtiesContext` ensures test isolation

### Test Coverage
- **AuthorizationE2ETest:** Full flow validation
- **PerformanceE2ETest:** Load and performance testing
- **Unit Tests:** Individual component testing

---

## Troubleshooting

### Common Issues

#### 1. Kafka Connection Failed
```
Error: Connection to node -1 could not be established
```
**Solution:** Ensure Kafka is running
```bash
docker-compose up -d kafka
docker-compose ps kafka
```

#### 2. Port Already in Use
```
Error: Address already in use: bind
```
**Solution:** Change port in `compose.yaml` or stop conflicting service
```bash
lsof -ti:8080 | xargs kill -9
```

#### 3. Tests Timeout
**Solution:** Increase timeout in `application.yaml`
```yaml
transaction-service:
  authorization:
    timeout-seconds: 10
```

#### 4. Gradle Build Issues
```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches
./gradlew clean build --refresh-dependencies
```
