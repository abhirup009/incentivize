# Technical Context

**Created:** 2025-05-24  
**Status:** [ACTIVE]  
**Author:** [Your Name]  
**Last Modified:** 2025-06-04 02:55
**Last Updated By:** Cascade AI Assistant

## Table of Contents
- [Technology Stack](#technology-stack)
- [Database Schema](#database-schema)
- [Development Environment](#development-environment)
- [Build & Deployment](#build--deployment)
- [Infrastructure](#infrastructure)
- [Development Workflow](#development-workflow)
- [Testing Strategy](#testing-strategy)
- [Monitoring & Logging](#monitoring--logging)
- [Security Considerations](#security-considerations)
- [Expression Evaluation](#expression-evaluation)
- [Error Handling](#error-handling)
- [Stress Testing](#stress-testing)

## Technology Stack
### Core Technologies
- **Backend Framework:** Spring Boot 3.5.0
- **Database:** PostgreSQL 15.13, DynamoDB 6.0, Redis 7.0
- **Programming Languages:** Kotlin 1.9.25, Java 17
- **Build Tool:** Gradle 8.13

### Important Notes
- **ID Type Handling:** Domain models use UUID for IDs while some database tables use BIGINT
- **Health Check Endpoint:** Implemented at `/api/v1/health` with basic status monitoring
- **Global Exception Handling:** Centralized error handling via `GlobalExceptionHandler` to provide standardized `ErrorResponse` objects with appropriate HTTP status codes and detailed error information.

### JOOQ Configuration
- **Version:** 3.19.23 (OSS Edition)
- **Generated Code Location:** `build/generated-src/jooq/main`
- **Target Package:** `com.abhirup.jooq`
- **Key Features:**
  - Record Generation
  - Immutable POJOs
  - Fluent Setters
  - Kotlin Data Classes
  - Java Time Types

### Flyway Configuration
- **Plugin Version:** 9.16.1
- **Migration Scripts Location:** `src/main/resources/db/migration` (classpath reference `classpath:db/migration`)
- **Baseline Behaviour:** Starts at `V1__create_tables.sql`; no baseline on migrate
- **Gradle Tasks:** `flywayInfo`, `flywayMigrate` wired via plugin; run automatically before tests in CI

### Dependencies
#### Runtime Dependencies
- `org.springframework.boot:spring-boot-starter-jooq` - JOOQ integration for Spring Boot
- `org.jooq:jooq:3.19.23` - JOOQ core library
- `org.postgresql:postgresql:42.7.3` - PostgreSQL JDBC driver
- `org.flywaydb:flyway-core` - Database migration tool
- `software.amazon.awssdk:dynamodb` - DynamoDB integration
- `org.springframework.boot:spring-boot-starter-data-redis` - Redis integration
- `io.lettuce:lettuce-core` - Redis client
- `com.fasterxml.jackson.module:jackson-module-kotlin` - Kotlin support for Jackson
- `org.slf4j:slf4j-api` - Logging facade
- `ch.qos.logback:logback-classic` - Logging implementation

## Database Schema
### PostgreSQL Tables
#### users
- **Primary Key:** `id` (BIGINT)
- **Columns:**
  - `id` - BIGINT, Primary Key
  - `name` - VARCHAR(255)
  - `email` - VARCHAR(255)
  - `created_at` - TIMESTAMP
  - `updated_at` - TIMESTAMP
- **Indexes:** 
  - Primary key on `id`
  - Unique index on `email`

### Migrations
1. **V1__create_users_table.sql** - Creates the users table

## Development Environment
### Prerequisites
- JDK 17
- Docker and Docker Compose
- PostgreSQL 15.13
- DynamoDB 6.0
- Redis 7.0
- Gradle 8.13

### Setup Instructions
1. Clone the repository
2. Start database services:
   ```bash
   docker-compose up -d
   ```
3. Build the application:
   ```bash
   ./gradlew build
   ```
4. Run the application:
   ```bash
   ./gradlew bootRun
   ```

### Configuration
#### Environment Variables
```
# PostgreSQL Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=sheets
DB_USER=postgres
DB_PASSWORD=postgres

# DynamoDB
MONGO_URI=mongodb://mongo:mongopass@localhost:27017/sheets?authSource=admin
MONGO_DATABASE=sheets

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redispass
REDIS_DATABASE=0

# Application
SERVER_PORT=8080
```

## Build & Deployment
### Build Process
```bash
./gradlew clean build
```

### Deployment
#### Development
```bash
./gradlew bootRun
```

#### Production
```bash
java -jar build/libs/sheets-0.0.1-SNAPSHOT.jar
```

## Infrastructure
### Development
- Local PostgreSQL 15.13 database via Docker
- Local DynamoDB 6.0 database via Docker
- Local Redis 7.0 cache via Docker
- Spring Boot embedded Tomcat server

### Staging/Production
- Not yet configured

## Development Workflow
### Branching Strategy
- Feature branches from `main`
- Pull requests for code review
- Merge to `main` after approval

### Code Review Process
1. Create feature branch from `main`
2. Make changes and commit with descriptive messages
3. Push and create pull request
4. Address review comments
5. Merge to `main` after approval

## Testing Strategy
### Testing Framework
- **Unit Testing:** JUnit 5 with MockK for mocking
- **Integration Testing:** Spring Boot Test with TestContainers
- **End-to-End Testing:** Custom shell scripts for API testing

### Test Structure
- **Unit Tests:** Located in `src/test/kotlin/com/sheets/`
- **Integration Tests:** Located in `src/test/kotlin/com/sheets/integration/`
- **Test Resources:** Located in `src/test/resources/`
- **Test Configuration:** `application-test.yml` for test-specific properties

### Testing Patterns
- **Arrange-Act-Assert:** All tests follow this pattern for clarity
- **Given-When-Then:** BDD-style test naming for readability
- **Test Doubles:** Use of mocks, stubs, and spies as appropriate
- **Test Data Builders:** For creating test data with fluent interfaces
- **Parameterized Tests:** For testing multiple scenarios with the same logic

When testing components that use A1 notation, special care is needed for:

1. **Cell ID Format:**
   - Cell IDs follow the format `sheetId:row:column` where column is an alphabetical letter (e.g., "1:1:A")
   - Helper methods must correctly extract sheet ID and cell coordinates
   - Test data must use consistent A1 notation format

2. **Type Handling:**
   - Ensure proper type handling for timestamps (Instant vs. String)
   - Use appropriate type converters in test setup
   - Handle type coercion in mock expectations

3. **Dependency Mocking:**
   - Mock both source and target cell dependencies
   - Ensure proper handling of dependency creation and deletion
   - Use empty lists for dependencies when appropriate to simplify tests

### Mock Configuration Best Practices
1. **Global Mocks Setup:**
   - Configure common mocks in the setUp() method
   - Set up default behaviors for frequently used dependencies
   - Initialize test data and fixtures

2. **Flexible Matchers:**
   - Use `any()` matchers instead of exact object matching to avoid type mismatch issues
   - Employ `slot` capture for complex object verification
   - Use `match { }` for custom matching logic

3. **Verification Strategy:**
   - Focus on essential operations in verifications
   - Verify end results rather than implementation details
   - Use ordered verification only when sequence matters

### Test Coverage Goals
- **Service Layer:** 85% line coverage
- **Repository Layer:** 80% line coverage
- **Domain Model:** 90% line coverage
- **Controllers:** 75% line coverage
- **Overall:** 85% line coverage

### Recent Test Improvements
- Fixed CellServiceExpressionTest and CellServiceBasicOperationsTest to work with A1 notation
- Resolved type mismatch issues between String timestamps and Instant objects in CellDependency mocks
- Updated test cell IDs to use A1 notation format (e.g., "1:1:A" instead of "1:1:1")
- Simplified verification steps to focus only on essential operations
- Added proper mocking for dependency creation and deletion

### Gatling Framework
- **Version:** Gatling 3.10.3
- **Scala Version:** 2.13.x
- **Key Features:**
  - HTTP protocol support
  - Scenario-based test design
  - Feeder-based data generation
  - Comprehensive reporting
  - Session variable management
  - Concurrent user simulation
  - Ramp-up and steady-state load patterns

### Stress Test Module Structure
```
stress-test/
├── build.gradle.kts         # Gradle configuration for stress tests
├── src/
│   └── gatling/
│       ├── scala/           # Scala test implementations
│       │   └── com/stress/test/simulation/
│       │       └── SpreadsheetSimulation.scala  # Main simulation file
│       └── resources/       # Test resources and configuration
└── run-spreadsheet-test.sh  # Test execution script
```

### Test Scenarios
- **Full Workflow Scenario:**
  - Creates a new sheet
  - Updates cells with primitive values
  - Updates cells with expressions
  - Retrieves cell values
  - Deletes cells
  - Shares the sheet with other users
  - Lists user sheets
  
- **Concurrent Primitive Updates Scenario:**
  - Creates a shared sheet accessible by all test users
  - Multiple users (20) concurrently update the same set of cells (A1-E5)
  - Verifies final cell values after concurrent updates
  
- **Concurrent Expression Updates Scenario:**
  - Creates a shared sheet accessible by all test users
  - Populates base values in cells
  - Multiple users (20) concurrently update cells with expressions referencing other cells
  - Verifies final expression evaluation after concurrent updates
  
- **Circular Dependency Test Scenario:**
  - Creates a test sheet
  - Sets up a circular dependency chain (A1→C1→B1→A1)
  - Verifies proper error handling for circular references

### Data Generation
- **User ID Feeder:** Generates consistent user IDs (1-5) for proper access control
- **Cell Coordinate Feeder:** Generates random cell coordinates and data values
- **Fixed Cell Feeder:** Provides a limited set of cell coordinates (A1-E5) to force concurrent updates
- **Expression Feeder:** Generates random cell expressions using A1 notation

### Test Execution
- **Command:** `./gradlew :stress-test:gatlingRun-com.stress.test.simulation.SpreadsheetSimulation`
- **Health Check:** Verifies service availability at `/v1/health` before test execution
- **Report Generation:** Creates HTML reports with detailed performance metrics
- **Test Duration:** Configurable, default 120 seconds maximum

### Performance Metrics
- **Request Count:** Total number of API requests executed
- **Response Times:** Min, max, mean, and percentile response times
- **Throughput:** Requests per second
- **Error Rate:** Percentage of failed requests
- **Status Codes:** Distribution of HTTP status codes

### Test Results (Latest Run)
- **Total Requests:** 750 requests
- **Success Rate:** 100% (0 failures)
- **Response Times:**
  - Min: 1ms
  - Max: 255ms
  - Mean: 24ms
  - 95th percentile: 102ms
- **Throughput:** ~26.8 requests/second
- **Test Duration:** 27 seconds

### Integration with CI/CD
- **Status:** Planned
- **Implementation:** Jenkins pipeline stage for automated stress testing
- **Thresholds:** Performance regression detection based on response time and error rate
- **Artifacts:** HTML reports stored as build artifacts

## Monitoring & Logging
### Logging
- SLF4J with Logback
- Log levels:
  - INFO for production
  - DEBUG for development
- Structured logging format:
  ```
  %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
  ```

### Metrics
- Spring Boot Actuator for basic metrics
- Health check endpoint at `/v1/health`
- Custom metrics for:
  - Cell update response time
  - Redis cache hit rate
  - Circular dependency detection time

## Security Considerations
### Authentication & Authorization
- Not yet implemented

### Data Protection
- No sensitive data stored
- Redis password protection
- DynamoDB authentication

### API Security
- Basic input validation
- No rate limiting yet

## Expression Evaluation
### Expression Functions
- **SUM:** Calculates the sum of values in cell references and ranges
- **AVERAGE:** Calculates the average of values in cell references and ranges
- **MIN:** Finds the minimum value among cell references and ranges
- **MAX:** Finds the maximum value among cell references and ranges

### Cell Reference Formats
- **A1 Notation:** Uses alphabetical column and numeric row (e.g., A1, B2, C3)
- **Legacy Numeric Notation:** Uses row:column format (e.g., 1:1, 2:3)
- **Cell Ranges:** Supports both A1 notation (e.g., A1:C3) and legacy format (e.g., 1:1-3:3)

### Column Conversion Utilities
- **Column Letter to Number:** Converts alphabetical column to numeric index
- **Column Number to Letter:** Converts numeric index to alphabetical column

## Error Handling
### Custom Exceptions
- **Base Exception:** `SheetException` serves as the base for all custom exceptions.
- **Specific Exceptions:**
  - `ResourceLockException` (and its subclasses `SheetLockException`, `CellLockException`): For concurrency control and resource locking issues.
  - `CircularReferenceException`: For detecting and handling circular dependencies in cell formulas.
  - `CellDependencyException`: For issues related to cell dependencies preventing operations.
  - `PersistenceException`: For errors during data persistence (Redis, DynamoDB).

### Global Exception Handler (`GlobalExceptionHandler`)
- **Purpose:** Provides a centralized mechanism to handle all exceptions thrown within the application.
- **Functionality:**
  - Catches custom and standard exceptions.
  - Maps exceptions to appropriate HTTP status codes (e.g., 409 Conflict, 400 Bad Request).
  - Formats error responses into a consistent `ErrorResponse` structure, including `status`, `error`, `message`, `timestamp`, and a `details` field for additional context.
  - Ensures user-friendly error messages are returned to the client.

### Error Response Structure
- **Model:** Defined in `api.yaml` as `ErrorResponse`
- **Fields:**
  - `status`: HTTP status code (e.g., 400, 404, 409, 500)
  - `error`: Error type (e.g., "Bad Request", "Not Found", "Conflict")
  - `message`: User-friendly error message
  - `path`: Request path that triggered the error
  - `timestamp`: Time when the error occurred
  - `details`: Additional error details specific to the error type (e.g., resource ID, lock owner, retry information)

This approach significantly improves error clarity, debugging, and client-side error handling capabilities.

### Hot Campaign CMS Monitoring
- **Metric Name:** `cms.hotCampaigns` (Micrometer Gauge)
- **Purpose:** Exposes current count of "hot" campaigns across all tenants, updated via Count-Min Sketch logic in `HotCampaignService`.
- **Sampling Guidance (Local):** Query `/actuator/metrics/cms.hotCampaigns` every 200 ms during load tests to observe threshold crossings.
- **Threshold (Dev):** `hotCampaign.threshold` property set to **50** to surface metric quickly.
- **Redis Keys:**
  - `cms:<tenantId>:<windowBucket>` – Hash storing per-campaign counts per 10-min window.
  - `hot:set:<tenantId>` – Set tracking campaigns whose count ≥ threshold; TTL = 20 min.
- **Known Pitfalls:**
  1. Mismatched tenant ID ⇒ gauge remains 0.
  2. Publishing events for unrelated `actionCode`s inflates hot set; repository filtering still TODO.
- **Debug Workflow:**
  ```bash
  TENANT=$(docker exec -i postgres psql -U incentivize -d incentivize -At -c "SELECT tenant_id FROM campaign LIMIT 1")
  curl -X POST "http://localhost:8080/helper/generate?tenantId=$TENANT&action=LOGIN&count=2500"
  watch -n0.2 curl -s http://localhost:8080/actuator/metrics/cms.hotCampaigns
  ```
