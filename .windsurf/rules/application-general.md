---
trigger: always_on
---

Application Development Rules: Spring Boot (Synchronous)
Purpose
This document provides exhaustive and mandatory rules for developing different layers of a Spring Boot application. These directives ensure consistency, maintainability, and effective interaction between components, specifically for synchronous API development.


Controller Layer Rules
Please Create Controllers using the generated Interfaces by Open API generator

The Controller layer handles incoming HTTP requests and returns responses, acting as the application's entry point.

Request/Response Mapping (DTOs):

Mandatory DTO Usage: Controller layer MUST use dedicated DTOs for request/response bodies. DTOs MUST NOT expose internal domain model details.

Mapping Layer: A clear mapping layer MUST exist between Controller DTOs and Service-layer models. This mapping SHOULD use dedicated mapper classes (e.g., MapStruct, ModelMapper, or manual).

Incoming Request: Request DTOs MUST be mapped to Service-layer input models before delegation.

Outgoing Response: Service-layer output models MUST be mapped to Response DTOs before returning HTTP response.

Validation: All incoming request DTOs MUST be validated using Java Bean Validation (e.g., @Valid). Validation failures MUST result in HTTP 4xx responses (e.g., 400 Bad Request).

Purpose: Ensures separation of concerns, protects domain model, and enables API versioning.

HTTP Method Semantics: Controller endpoints MUST adhere to standard HTTP method semantics (GET, POST, PUT, PATCH, DELETE).

RESTful Principles: Endpoints SHOULD follow RESTful principles, using nouns for resources and appropriate HTTP status codes (e.g., 200, 201, 204, 404, 500).

Minimal Business Logic: The Controller layer MUST NOT contain significant business logic. Its primary responsibilities are:

Receiving HTTP requests.

Validating input.

Mapping request DTOs to service models.

Delegating calls to the Service layer.

Mapping service output models to response DTOs.

Returning HTTP responses.

Service Layer Rules
The Service layer encapsulates core business logic, orchestrates operations, applies business rules, and manages transactions.

Design Patterns for Tasks:

The Service layer MUST apply appropriate design patterns for tasks to enhance modularity, maintainability, and scalability. The chosen pattern MUST be explicitly defined in task documentation.

Common Patterns (Examples):

Facade: Simplifies interface to complex sub-systems.

Strategy: Encapsulates varying business logic/algorithms.

Builder: Constructs complex domain/input models.

Repository: Abstracts data access.

Factory: Creates domain object instances based on criteria.

Explicit Definition: Any service method/class implementing a pattern MUST be documented with its name and rationale.

Connection to Database Layer:

The Service layer MUST interact with the Database Layer exclusively through its defined interfaces (e.g., Repository interfaces). Direct database client manipulation is STRICTLY PROHIBITED.

Dependency Injection: Database Layer interfaces MUST be injected into Service layer components using Constructor Dependency Injection. Field/setter injection SHOULD NOT be used.

Transaction Management: All business operations involving multiple database operations MUST be wrapped in a transaction. Spring's @Transactional SHOULD be used for declarative management. Programmatic management MAY be used for complex scenarios.

Domain Model: The Service layer operates on domain models (plain Java/Kotlin objects). Mapping between domain models and database models MUST occur at the Service/Database layer boundary.

Business Logic Enforcement: All core business rules and validations MUST reside within the Service layer.

Database Layer Rules
The Database layer handles all interactions with PostgreSQL, abstracting data storage.

Interface and Implementation Layer:

Mandatory Interface Definition: For each distinct data entity/aggregate root, a dedicated interface MUST be defined in the Database Layer, declaring all necessary data access operations (e.g., findById, save).

Implementation Class: A corresponding implementation class MUST be created for each interface, implementing data access using Jooq.

Connection to Downstream Database: The implementation layer MUST utilize the configured Jooq DSLContext to interact with PostgreSQL. It MUST NOT directly manage JDBC connections.

Database Configuration and Client:

Configuration Check: Before startup, Database Layer initialization MUST verify existence/validity of database connection configurations (e.g., spring.datasource.url, username, password) in application.yml or environment variables.

Client Creation: If valid config is present, application context MUST automatically create/configure the PostgreSQL client (e.g., DataSource and Jooq DSLContext beans) using Spring Boot auto-configuration or explicit @Configuration classes.

Missing Configuration Handling: If critical database configuration is missing/invalid, the application MUST fail fast during startup, logging a clear error. It MUST NOT attempt to proceed with an unconfigured connection.

Jooq DSLContext: The Jooq DSLContext bean MUST be configured to use the DataSource provided by Spring Boot, acting as the primary entry point for all Jooq-based operations.