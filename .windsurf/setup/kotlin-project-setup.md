# Kotlin Spring Boot Project Setup

This guide will help you set up a new Kotlin Spring Boot project using VS Code's Spring Initializer extension with a streamlined project structure.

## Prerequisites

- VS Code with Spring Initializr Java Support extension installed
- Java 21 or later
- Docker and Docker Compose
- Gradle 8.5 or later (will be installed if not present)

## Project Structure

```
project/                # Project root directory/
├── .windsurf/              # Windsurf configuration and workflows
│   └── workflows/
│       └── kotlin-spring-setup.md
├── src/                    # Source code
│   ├── main/kotlin/        # Kotlin source files
│   ├── main/resources/     # Application properties and resources
│   └── test/kotlin/        # Test source files
├── docker/                 # Docker-related files
│   └── docker-compose.yml  # Docker Compose configuration
├── .gitignore             # Git ignore file
├── build.gradle.kts       # Gradle build configuration
└── README.md              # Project documentation
```

## Setup Process

### 1. Gather Project Information

1. **Group Name**: 
   - This is typically your organization's domain in reverse (e.g., com.example)
   - Enter group name: [your-group-name]

2. **Artifact Name**: 
   - This will be your project name (e.g., payment-service)
   - Enter artifact name: [your-artifact-name]

### 2. Create Project Using Spring Initializer

1. Open VS Code
2. Press `Ctrl+Shift+P` (or `Cmd+Shift+P` on Mac) to open the command palette
3. Type "Spring Initializr: Create a Gradle Project" and press Enter
4. Configure project settings:
   - Language: Kotlin
   - Group: [your-group-name] (from step 1.1)
   - Artifact: [your-artifact-name] (from step 1.2)
   - Package name: [your-group-name].[your-artifact-name] (auto-generated)
   - Java version: 21
   - Packaging: Jar
   - Spring Boot version: 3.2.0 (or latest stable)

5. Add required dependencies:
   - Spring Web (for building web applications)
   - (No other dependencies needed for initial setup)

6. Click "Generate" and select the project directory

### 2. Initialize Git Repository

```bash
git init
curl -o .gitignore https://www.gitignore.io/api/gradle,kotlin,intellij+all,visualstudiocode
```
     ```bash
     ./gradlew build --refresh-dependencies
     ```
   - The following files should be committed to version control:
     - gradlew (Unix script)
     - gradlew.bat (Windows script)
     - gradle/wrapper/gradle-wrapper.jar
     - gradle/wrapper/gradle-wrapper.properties

7. Client Setup
   - Add dependencies and imports for the client for the technologies in the step 4
   - Set up client code for selected technologies in step 4 inside src/main/kotlin/[package_name]/util
   - For Redis: add Redis client config using Lettuce or Jedis in RedisClient.kt
   - For Elasticsearch: use RestHighLevelClient or ElasticsearchClient in ElasticsearchClient.kt
   - For PostgreSQL/MySQL/MongoDB: configure datasource properties in application.yml and define DatabaseConfig.kt
   - For Kafka: create KafkaProducerConfig.kt and KafkaConsumerConfig.kt with required serializers and topic settings
   - For RabbitMQ: add RabbitMQConfig.kt with ConnectionFactory, Queue, and RabbitTemplate beans
   - Keep each client config reusable and injectable via Spring's @Configuration and @Bean annotations
   - Validate each client by writing a basic health-check or ping method in a corresponding ClientHealthUtil.kt

   
Always Do these things
   - Use Java 21
   - Use gradle 8.5
   - download the latest version of all the depenedencies
   - test docker-compose by running it once and then shut it off
   - ensure docker-compose up brings up all services including Spring Boot application
   - use objectMapper


## Notes
- Keep sensitive information in environment variables
- Update .gitignore for your project needs
- Add appropriate documentation
- Docker Compose should start all services including Spring Boot application
- Use environment variables in docker-compose.yml for service configuration

## Support
For any issues or questions, please refer to:
- Docker Documentation
- Spring Boot Documentation
- Maven/Gradle Documentation