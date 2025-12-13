# Appointment Service

The Appointment Service manages the complete appointment lifecycle for the Healthcare System, including provider schedule management, slot booking, and appointment state management.

## Features

### 1. Provider Schedule Management
- Operations team can create availability schedules for providers
- Automatic generation of time slots based on schedule parameters
- Time slots stored as JSON for efficient access
- View all schedules for a provider

### 2. Slot Management
- Fetch available time slots for a provider
- Automatic blocking of slots when appointments are created
- Automatic release of slots when appointments are cancelled
- Slots stored as JSON mapping: `{slot_time: availability}`

### 3. Appointment Lifecycle Management

**Appointment States:**
```
REQUESTED → CONFIRMED → COMPLETED
    ↓           ↓
  CANCELLED  CANCELLED
```

**State Transitions:**
- **REQUESTED**: Initial state when patient books an appointment
- **CONFIRMED**: Provider accepts the appointment
- **CANCELLED**: Patient cancels or provider rejects
- **COMPLETED**: Appointment is completed (future enhancement)

### 4. Patient Operations
- Request appointments in available slots only
- Select desired provider and time
- View all their appointments
- Cancel REQUESTED or CONFIRMED appointments

### 5. Provider Operations
- Accept (confirm) REQUESTED appointments
- Reject REQUESTED appointments
- View all their appointments
- View pending REQUESTED appointments

## API Endpoints

### Provider Schedule APIs

#### 1. Create Provider Schedule
**Endpoint:** `POST /api/schedules`

**Request Body:**
```json
{
  "providerId": 1,
  "scheduleDate": "2025-12-20",
  "startTime": "2025-12-20T09:00:00",
  "endTime": "2025-12-20T17:00:00",
  "slotDurationMinutes": 30
}
```

**Response:**
```json
{
  "id": 1,
  "providerId": 1,
  "scheduleDate": "2025-12-20",
  "slots": [
    {
      "slotTime": "2025-12-20T09:00:00",
      "isAvailable": true
    },
    {
      "slotTime": "2025-12-20T09:30:00",
      "isAvailable": true
    }
    // ... more slots
  ],
  "message": "Schedule created successfully"
}
```

#### 2. Get Available Slots for Provider
**Endpoint:** `GET /api/schedules/provider/{providerId}/available-slots`

**Response:**
```json
[
  {
    "slotTime": "2025-12-20T09:00:00",
    "isAvailable": true
  },
  {
    "slotTime": "2025-12-20T09:30:00",
    "isAvailable": true
  }
  // ... more available slots
]
```

#### 3. Get All Schedules for Provider
**Endpoint:** `GET /api/schedules/provider/{providerId}`

### Appointment APIs

#### 1. Create Appointment (Patient)
**Endpoint:** `POST /api/appointments`

**Request Body:**
```json
{
  "patientId": 1,
  "providerId": 1,
  "appointmentDateTime": "2025-12-20T10:00:00"
}
```

**Response:**
```json
{
  "id": 1,
  "patientId": 1,
  "providerId": 1,
  "status": "REQUESTED",
  "appointmentDateTime": "2025-12-20T10:00:00",
  "createdAt": "2025-12-13T10:30:00",
  "message": "Appointment requested successfully"
}
```

#### 2. Provider Confirms/Rejects Appointment
**Endpoint:** `POST /api/appointments/{appointmentId}/provider/{providerId}/update-status`

**Request Body:**
```json
{
  "action": "CONFIRM"
}
```
OR
```json
{
  "action": "REJECT"
}
```

**Response:**
```json
{
  "id": 1,
  "patientId": 1,
  "providerId": 1,
  "status": "CONFIRMED",
  "appointmentDateTime": "2025-12-20T10:00:00",
  "createdAt": "2025-12-13T10:30:00",
  "message": "Appointment confirmed successfully"
}
```

#### 3. Patient Cancels Appointment
**Endpoint:** `POST /api/appointments/{appointmentId}/patient/{patientId}/cancel`

**Response:**
```json
{
  "id": 1,
  "status": "CANCELLED",
  "message": "Appointment cancelled successfully"
}
```

#### 4. Get Patient Appointments
**Endpoint:** `GET /api/appointments/patient/{patientId}`

#### 5. Get Provider Appointments
**Endpoint:** `GET /api/appointments/provider/{providerId}`

#### 6. Get Provider's Requested Appointments
**Endpoint:** `GET /api/appointments/provider/{providerId}/requested`

#### 7. Get Appointment by ID
**Endpoint:** `GET /api/appointments/{appointmentId}`

## Business Rules

1. **Slot Booking:**
   - Patients can only book available slots
   - Appointments must be in the future
   - Slot is immediately blocked when appointment is created

2. **Appointment State Transitions:**
   - Only REQUESTED appointments can be confirmed by provider
   - Only REQUESTED appointments can be rejected by provider
   - Patient can cancel REQUESTED or CONFIRMED appointments
   - Cancelled/rejected appointments release the time slot

3. **Provider Actions:**
   - Providers can only act on their own appointments
   - Can only accept/reject REQUESTED appointments

4. **Patient Actions:**
   - Patients can only act on their own appointments
   - Can cancel REQUESTED or CONFIRMED appointments

5. **Data Integrity:**
   - Patient ID must reference valid user with patient role
   - Provider ID must reference valid user with provider role
   - Foreign key constraints ensure referential integrity

## Database Schema

### Tables

#### `provider_time_slots`
- `id` (Primary Key)
- `provider_id`
- `schedule_date`
- `slots` (JSON) - Maps slot time to availability: `{"2025-12-20T09:00:00": true, ...}`
- `created_at`
- `updated_at`

**JSON Structure:**
```json
{
  "2025-12-20T09:00:00": true,
  "2025-12-20T09:30:00": true,
  "2025-12-20T10:00:00": false,
  "2025-12-20T10:30:00": true
}
```
- Key: ISO-formatted datetime string
- Value: Boolean (true = available, false = booked)

#### `appointments`
- `id` (Primary Key)
- `patient_id` (Foreign Key → users.id)
- `provider_id` (Foreign Key → users.id)
- `status` (ENUM: REQUESTED, CONFIRMED, COMPLETED, CANCELLED)
- `appointment_date_time`
- `created_at`
- `updated_at`

**Foreign Key Constraints:**
- `fk_appointments_patient`: References `users(id)` - Ensures patient exists
- `fk_appointments_provider`: References `users(id)` - Ensures provider exists
- ON DELETE RESTRICT - Prevents deletion of users with appointments
- ON UPDATE CASCADE - Updates propagate to appointments

**Indexes:**
- `idx_patient_id` - Fast lookup by patient
- `idx_provider_id` - Fast lookup by provider
- `idx_appointment_date_time` - Fast lookup by date/time
- `idx_status` - Fast filtering by status

## Running the Service

### Prerequisites
- Java 17
- MySQL 8.0+
- Maven 3.6+

### Database Setup
The service uses the same database as other services: `healthcare_system`

```sql
CREATE DATABASE IF NOT EXISTS healthcare_system;
```

### Foreign Key Setup

After the service creates the tables, run the FK setup script:

```bash
mysql -u root -p healthcare_system < FK_SETUP.sql
```

This adds foreign key constraints for `patient_id` and `provider_id` to ensure data integrity.

### Running Locally

```bash
# Navigate to Appointment Service directory
cd Appointment-Service

# Build the service
mvn clean package -DskipTests

# Run the service
java -jar target/Appointment-Service-0.0.1-SNAPSHOT.jar
```

The service will start on port **8082**.

### Configuration

The service configuration is in `src/main/resources/application.properties`:

```properties
spring.application.name=Appointment-Service
server.port=8082

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/healthcare_system
spring.datasource.username=root
spring.datasource.password=Mysqlpass@2025

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Integration with API Gateway

All appointment service endpoints are accessible through the API Gateway on port 8080 with RBAC enabled:

- **Schedule Management**: `admin`, `provider` roles
- **View Available Slots**: `admin`, `provider`, `patient` roles
- **Create Appointment**: `patient` role
- **Confirm/Reject Appointment**: `provider` role
- **Cancel Appointment**: `patient` role
- **View Appointments**: Role-based access

## Error Handling

The service provides comprehensive error handling:

- `AppointmentNotFoundException`: 404 - Appointment not found
- `ProviderTimeSlotsNotFoundException`: 404 - Schedule not found
- `SlotNotAvailableException`: 400 - Slot is not available for booking
- `InvalidAppointmentStateException`: 400 - Invalid state transition
- Validation errors: 400 - With field-level error messages
- Foreign key violations: 500 - Invalid patient or provider ID

## Example Workflow

### 1. Operations Team Creates Provider Schedule
```bash
POST /api/schedules
{
  "providerId": 1,
  "scheduleDate": "2025-12-20",
  "startTime": "2025-12-20T09:00:00",
  "endTime": "2025-12-20T17:00:00",
  "slotDurationMinutes": 30
}
```
→ System generates 16 time slots of 30 minutes each stored as JSON

### 2. Patient Views Available Slots
```bash
GET /api/schedules/provider/1/available-slots
```
→ Returns all available slots for provider

### 3. Patient Books Appointment
```bash
POST /api/appointments
{
  "patientId": 1,
  "providerId": 1,
  "appointmentDateTime": "2025-12-20T10:00:00"
}
```
→ Appointment created in REQUESTED state
→ Slot at 10:00 AM is marked unavailable in JSON
→ Foreign keys validate patient and provider IDs

### 4. Provider Views Pending Requests
```bash
GET /api/appointments/provider/1/requested
```
→ Returns all REQUESTED appointments

### 5. Provider Confirms Appointment
```bash
POST /api/appointments/1/provider/1/update-status
{
  "action": "CONFIRM"
}
```
→ Appointment moves to CONFIRMED state

### 6. (Optional) Patient Cancels Appointment
```bash
POST /api/appointments/1/patient/1/cancel
```
→ Appointment moves to CANCELLED state
→ Slot at 10:00 AM is released (marked available in JSON)

## Architecture Highlights

### Simplified Schema
- **Single table for schedules**: `provider_time_slots` stores all slot information as JSON
- **Streamlined appointments**: Direct reference to appointment date/time
- **Referential integrity**: Foreign keys ensure valid patient and provider references
- **Efficient slot management**: JSON storage allows fast updates and queries

### Benefits
- Reduced database joins
- Flexible slot structure
- Easy to add/remove slots dynamically
- Better performance for slot availability checks
- Data integrity through foreign key constraints

## Future Enhancements

1. **Appointment Completion**: Add workflow to mark appointments as COMPLETED
2. **Recurring Schedules**: Support for recurring availability patterns
3. **Appointment Reminders**: Integration with notification service
4. **Appointment History**: Track complete audit trail of state changes
5. **Slot Blocking**: Allow manual blocking of slots for breaks/emergencies
6. **Multi-day Schedules**: Bulk creation of schedules for multiple days
7. **Appointment Rescheduling**: Allow patients to reschedule appointments
8. **Wait List**: Manage waiting list for fully booked providers
9. **Analytics**: Dashboard for appointment statistics

## Logging

The service uses SLF4J with Logback for comprehensive logging:
- INFO: Business operations (create, update, state changes)
- DEBUG: Detailed service layer operations
- ERROR: Exception details with stack traces

## Testing

Run unit tests:
```bash
mvn test
```

## Support

For issues or questions, please refer to the main Healthcare System documentation.
