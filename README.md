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

4. **Appointment Service** (Port 8082)
   - Provider schedule management
   - Time slot generation and management
   - Appointment lifecycle management (REQUESTED → CONFIRMED → COMPLETED)
   - Patient booking and cancellation
   - Provider confirmation/rejection workflows

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
