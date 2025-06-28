---
trigger: always_on
---

Docker Compose Configuration Rules
👋 Pre-generation Prompt
Always ask the user before generating:
"What technologies, services, or dependencies (e.g., PostgreSQL, Redis, MongoDB, Kafka, etc.) do you want in the docker-compose.yml file?"

"Do you want to include multiple services (e.g., multiple databases or caches)?"

❗Expectations for the Coding Agent
Do not include the application service in the Docker Compose file.

The coding agent must:

Configure all requested infrastructure services (e.g., PostgreSQL, Redis, Kafka, MongoDB, etc.)

Set up all required ports and expose them so the application can connect externally

Include all connection credentials and environment variables the application would need

Support multiple instances of the same type of service (e.g., two PostgreSQL databases with different names/ports)

Ensure services are ready to use by including health checks

Document all exposed variables, ports, and service names

✅ Service Dependencies
Use depends_on with condition: service_healthy for proper startup sequencing

Include health checks for each service

⚙️ Environment Variables
Use environment variables for all service configuration

Provide default values using ${VARIABLE:-default} syntax

Document environment variables in the docker-compose.yml file

🗂️ Volumes
Use named volumes for data persistence (e.g., for database storage)

Define volume mounts for configuration/logs if required by the service

🔍 Health Checks
Configure health checks with:

Appropriate interval, timeout, and start_period

Correct protocol and target port

Health checks must be reliable indicators of service readiness

🌐 Docker Compose Network Configuration
Use custom user-defined networks for isolating services

Configure network_aliases for easier internal access

Expose only required ports to the host machine

Use internal networks for inter-service communication

🔐 Docker Security Best Practices
Do not store secrets in Docker images or version-controlled files

Use Docker secrets or environment variables for sensitive data

Pin versions for all service images (avoid latest)

Run containers as non-root users where possible

Limit container capabilities and use read-only file systems

Ensure logs are enabled for all services