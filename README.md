# Healthcare System

A comprehensive microservices-based healthcare management system built with Java Spring Boot.

## Architecture

This system follows a microservices architecture with the following components:

### Services

1. **API Gateway** (Port 8080)
   - Central entry point for all client requests
   - JWT-based authentication and authorization
   - Role-based access control (RBAC)
   - Routes requests to backend services

2. **Auth Service** (Port 9000)
   - User authentication and login
   - JWT token generation and validation
   - User signup

3. **User Service** (Port 8081)
   - User management (CRUD operations)
   - Role assignment
   - User profile management
   - Supports Patient and Provider roles

4. **Appointment Service** (Port 8082) ✨ **NEW**
   - Provider schedule management
   - Time slot generation and management
   - Appointment lifecycle management (REQUESTED → CONFIRMED → COMPLETED)
   - Patient booking and cancellation
   - Provider confirmation/rejection workflows

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Java Version**: 21
- **Database**: MySQL 8.0
- **ORM**: Hibernate/JPA
- **Security**: Spring Security with JWT
- **API Gateway**: Spring Cloud Gateway
- **Build Tool**: Maven
- **Migration**: Flyway

## Getting Started

### Prerequisites

- Java 21 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

### Database Setup

```sql
CREATE DATABASE healthcare_system;
```

Update database credentials in each service's `application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=Mysqlpass@2025
```

### Running the Services

1. **Start Auth Service**
   ```bash
   cd Auth-Service
   ./mvnw spring-boot:run
   ```

2. **Start User Service**
   ```bash
   cd User-Service
   ./mvnw spring-boot:run
   ```

3. **Start Appointment Service**
   ```bash
   cd Appointment-Service
   ./mvnw spring-boot:run
   ```

4. **Start API Gateway**
   ```bash
   cd API-Gateway
   ./mvnw spring-boot:run
   ```

### Service Ports

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Main entry point |
| Auth Service | 9000 | Authentication |
| User Service | 8081 | User management |
| Appointment Service | 8082 | Appointments & Scheduling |

## API Documentation

### Authentication Flow

1. **Signup**: `POST /api/signup`
2. **Login**: `POST /api/login` → Returns JWT token
3. **Use Token**: Add `Authorization: Bearer <token>` header to subsequent requests

### User Roles

- **Admin**: Full system access
- **Provider**: Healthcare providers (doctors, specialists)
- **Patient**: Patients seeking appointments

### Key Features

#### Appointment Service Features

- **Schedule Management**: Operations team creates availability schedules for providers
- **Slot Booking**: Patients book appointments in available time slots
- **State Management**: Complete appointment lifecycle (REQUESTED → CONFIRMED → COMPLETED)
- **Cancellation**: Patients can cancel appointments, slots are automatically released
- **Provider Workflow**: Providers can accept or reject appointment requests

For detailed API documentation, refer to:
- [Appointment Service README](./Appointment-Service/README.md)
- [Postman Collection](./POSTMAN_COLLECTION.json)

## Project Structure

```
Healthcare-System/
├── API-Gateway/          # API Gateway service
├── Auth-Service/         # Authentication service
├── User-Service/         # User management service
├── Appointment-Service/  # Appointment management service
├── pom.xml              # Parent POM
└── README.md            # This file
```

## Development

### Building All Services

```bash
mvn clean install
```

### Running Tests

```bash
mvn test
```

## Security

- JWT-based authentication
- Role-based access control (RBAC)
- Secure password hashing
- CORS configuration
- Session management

## Error Handling

All services implement comprehensive error handling with:
- Validation error messages
- Custom exception handling
- HTTP status codes
- Structured error responses

## Logging

All services use SLF4J with Logback:
- Request/response logging
- Business operation logging
- Error tracking with stack traces

## Future Enhancements

1. **Appointment Service**
   - Appointment completion workflow
   - Recurring schedules
   - Appointment reminders
   - Analytics dashboard

2. **System-wide**
   - Service discovery (Eureka)
   - Distributed tracing
   - API documentation (Swagger/OpenAPI)
   - Monitoring and metrics
   - Notification service
   - Payment integration

## Contributing

1. Create a feature branch
2. Make your changes
3. Test thoroughly
4. Submit a pull request

## License

This project is part of a healthcare system implementation.
