# HRMS Backend (scaffold)

This is a generated Spring Boot 3.3.5 project scaffold (DB-first orientation) containing:
- Entities matching the schema you provided
- Repositories, basic controllers for the listed APIs
- Basic JWT util and Spring Security configuration (simplified)
- Maven build (pom.xml) and application.yml

Notes:
- The project is **scaffold** and includes working endpoints, but **business logic, validations, DTOs, exception handling, and production-grade security** should be implemented further.
- The application expects a MySQL database `hrms_db` matching the schema. `spring.jpa.hibernate.ddl-auto` is set to `validate`. You can change to `update` for development.
- Replace `jwt.secret` in application.yml with a secure secret.

To build:
```bash
mvn -U -DskipTests package
```

OpenAPI UI is available at `/swagger-ui.html` when the app runs.

## What's included now (expanded)
- Full JWT authentication flow (AuthService, JWT util, filter, CustomUserDetailsService)
- Flyway migrations: `src/main/resources/db/migration/V1__init.sql` with the schema
- DTOs and validation for auth
- Global exception handler for validation and errors
- Postman collection starter at `postman_collection.json`

## Next steps to run locally
1. Start a MySQL server and create database `hrms_db`:
   ```sql
   CREATE DATABASE hrms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Update `src/main/resources/application.yml` with your DB credentials and a strong `jwt.secret`.
3. Build and run:
   ```bash
   mvn -U -DskipTests package
   java -jar target/hrms-backend-0.0.1-SNAPSHOT.jar
   ```
4. The application will run Flyway migrations automatically to create tables.
5. Use Postman collection to test login and other endpoints.

## What I didn't fully implement (can continue on request)
- DTOs + mappers for all entities (I added auth DTOs; others remain entity-based for brevity)
- Comprehensive unit & integration tests
- API documentation customizations (openapi tags, examples)
- Role/permission management UI or seed data (you can insert default admin role and user via SQL or code)
