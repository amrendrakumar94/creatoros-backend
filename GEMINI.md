# Project Instructions: CreatorOS Backend

Welcome to the **CreatorOS Backend** repository! This document serves as the shared instructional context and playbook for developing, building, and testing this application.

## 1. Project Overview
- **Core Purpose**: Backend service for CreatorOS.
- **Language & Runtime**: Java 21
- **Framework**: Spring Boot 4.0.7
- **Database**: MySQL with Flyway schema migration.
- **Security**: Spring Security configured for authentication/authorization.
- **Primary Dependencies**:
  - `spring-boot-starter-webmvc` - Web MVC support (REST endpoints)
  - `spring-boot-starter-data-jpa` - JPA / Hibernate ORM
  - `spring-boot-starter-flyway` / `flyway-mysql` - Database migrations
  - `spring-boot-starter-security` - Security framework
  - `spring-boot-starter-validation` - Input validation
  - `spring-boot-starter-actuator` - Production-ready monitoring & metrics
  - `lombok` - Boilerplate reduction with annotation processors

---

## 2. Getting Started & Local Environment Setup

### Prerequisites
- Java Development Kit (JDK) 21 installed.
- Local or Dockerized MySQL instance.

### Configuration
Update the database connection details in `src/main/resources/application.properties`:
```properties
spring.application.name=creatoros-backend

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/creatoros?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_secure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

---

## 3. Building and Running

### Build Command
Compile and build the application executable JAR using the Maven wrapper:
```bash
# On Unix-based systems (macOS, Linux)
./mvnw clean package

# On Windows (cmd/PowerShell)
mvnw.cmd clean package
```

### Running Locally
To launch the Spring Boot application locally:
```bash
./mvnw spring-boot:run
```

### Running Tests
To run the full suite of unit and integration tests:
```bash
./mvnw test
```

---

## 4. Development Conventions & Architecture

To maintain high code quality and consistency, all development must adhere to the following architecture and conventions:

### Layered Architecture
Files should be organized into the following package structure under `com.creatoros`:
- `com.creatoros.config` / `com.creatoros.security`: Application configuration and security filters.
- `com.creatoros.controller`: REST APIs. Always use `@RestController` and map endpoints appropriately (e.g., `/api/v1/...`). Validate inputs using `@Valid`.
- `com.creatoros.dto`: Data Transfer Objects for requests and responses. Avoid exposing raw JPA entities.
- `com.creatoros.entity`: JPA database models. Annotate with `@Entity`, `@Table`, and use Lombok annotations.
- `com.creatoros.repository`: Spring Data JPA interfaces extending `JpaRepository`.
- `com.creatoros.service`: Business logic interfaces and implementation classes (annotated with `@Service`). Make transactions explicit with `@Transactional`.
- `com.creatoros.exception`: Global exception handling (using `@RestControllerAdvice` and custom exception classes).

### Coding Practices
1. **Lombok**: Utilize annotation processors to eliminate boilerplate. Use `@Getter`, `@Setter`, `@Builder`, and `@Slf4j` rather than writing getters, setters, constructors, or custom loggers manually.
2. **Flyway Migrations**: Never modify existing migration files once they are committed. Create a new SQL migration file under `src/main/resources/db/migration/` following the pattern `V<TIMESTAMP>__<description>.sql` or `V<INCREMENTAL_NUMBER>__<description>.sql`.
3. **Validation**: Validate all inbound REST request models using validation annotations (e.g., `@NotNull`, `@NotBlank`, `@Size`, `@Email`).
4. **Testing**:
   - Write unit tests for services using JUnit 5 and Mockito.
   - Write integration tests for controllers using `@WebMvcTest` or `@SpringBootTest` with `@AutoConfigureMockMvc`.
   - Prefer mocking external services and databases where applicable, or use profiles (e.g., `test` profile) for in-memory or localized test configurations.
