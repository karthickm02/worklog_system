# Worklog System - API Specification (Spring Boot)

## Overview

This document defines the REST API endpoints for the Worklog System backend, implemented using Spring Boot 3.x with Spring Security and Spring Data JPA.

### Base URL
```
https://api.worklog.com/api/v1
```

### Authentication
- JWT-based authentication using Spring Security
- Token format: `Authorization: Bearer <token>`
- Access token validity: 24 hours
- Refresh token validity: 7 days

### Common Headers
```
Content-Type: application/json
Accept: application/json
Authorization: Bearer <token>
```

### Common Response Format

#### Success Response
```json
{
  "status": "SUCCESS",
  "data": {
    // Response payload
  },
  "timestamp": "2024-03-21T10:00:00Z"
}
```

#### Error Response
```json
{
  "status": "ERROR",
  "message": "Error description",
  "code": "ERROR_CODE",
  "details": {
    // Additional error details
  },
  "timestamp": "2024-03-21T10:00:00Z"
}
```

## Authentication Module (`AuthController`)

### POST /api/v1/auth/login
Authenticate user and obtain JWT token.

**Authentication Required**: No

**Request Body**:
```json
{
  "email": "string, required",
  "password": "string, required"
}
```

**Success Response**:
- Status: 200 OK
```json
{
  "status": "SUCCESS",
  "data": {
    "accessToken": "string",
    "refreshToken": "string",
    "expiresIn": 86400,
    "tokenType": "Bearer",
    "user": {
      "id": "uuid",
      "email": "string",
      "firstName": "string",
      "lastName": "string",
      "role": "ROLE_EMPLOYEE|ROLE_MANAGER|ROLE_ADMIN"
    }
  }
}
```

**Error Responses**:
- 400 Bad Request: Invalid request format
- 401 Unauthorized: Invalid credentials
- 429 Too Many Requests: Rate limit exceeded

**Security**:
- Rate limiting: 5 requests per minute per IP
- Spring Security authentication manager
- Password encoded with BCrypt

### POST /api/v1/auth/refresh
Refresh access token.

**Request Body**:
```json
{
  "refreshToken": "string, required"
}
```

**Success Response**:
- Status: 200 OK
```json
{
  "status": "SUCCESS",
  "data": {
    "accessToken": "string",
    "expiresIn": 86400
  }
}
```

## User Module (`UserController`)

### GET /api/v1/users/me
Get current user profile.

**Authentication**: Required

**Success Response**:
- Status: 200 OK
```json
{
  "status": "SUCCESS",
  "data": {
    "id": "uuid",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "role": "ROLE_EMPLOYEE|ROLE_MANAGER|ROLE_ADMIN",
    "department": {
      "id": "uuid",
      "name": "string"
    },
    "joinDate": "date",
    "status": "ACTIVE|INACTIVE|SUSPENDED"
  }
}
```

### GET /api/v1/users
List users with pagination and filtering.

**Authentication**: Required (ROLE_MANAGER, ROLE_ADMIN)

**Query Parameters**:
- `page`: integer, optional (default: 0)
- `size`: integer, optional (default: 20)
- `sort`: string, optional (format: "property,direction", e.g., "lastName,desc")
- `status`: string, optional (ACTIVE|INACTIVE|SUSPENDED)
- `role`: string, optional (ROLE_EMPLOYEE|ROLE_MANAGER|ROLE_ADMIN)
- `departmentId`: UUID, optional
- `search`: string, optional

**Success Response**:
- Status: 200 OK
```json
{
  "status": "SUCCESS",
  "data": {
    "content": [
      {
        "id": "uuid",
        "email": "string",
        "firstName": "string",
        "lastName": "string",
        "role": "string",
        "departmentId": "uuid",
        "status": "string"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true,
        "unsorted": false
      }
    },
    "totalElements": 100,
    "totalPages": 5,
    "last": false,
    "first": true
  }
}
```

### POST /api/v1/users
Create new user.

**Authentication**: Required (ROLE_ADMIN)

**Request Body**:
```json
{
  "email": "string, required, @Email",
  "firstName": "string, required, @Size(min=2, max=50)",
  "lastName": "string, required, @Size(min=2, max=50)",
  "role": "ROLE_EMPLOYEE|ROLE_MANAGER|ROLE_ADMIN, required",
  "departmentId": "uuid, required",
  "password": "string, required, @Pattern(regex=...)",
  "joinDate": "date, required"
}
```

**Success Response**:
- Status: 201 Created
```json
{
  "status": "SUCCESS",
  "data": {
    "id": "uuid",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "role": "string",
    "departmentId": "uuid",
    "status": "ACTIVE"
  }
}
```

## Time Entry Module (`TimeEntryController`)

### POST /api/v1/time-entries/clock-in
Record clock-in time for current user.

**Authentication**: Required

**Request Body**:
```json
{
  "projectId": "uuid, optional",
  "notes": "string, optional, @Size(max=500)"
}
```

**Success Response**:
- Status: 201 Created
```json
{
  "status": "SUCCESS",
  "data": {
    "id": "uuid",
    "clockIn": "datetime",
    "projectId": "uuid",
    "notes": "string",
    "status": "PENDING"
  }
}
```

**Error Responses**:
- 400 Bad Request: Already clocked in
- 404 Not Found: Project not found
- 409 Conflict: Existing active session

### POST /api/v1/time-entries/{id}/clock-out
Record clock-out time for an entry.

**Authentication**: Required

**Path Parameters**:
- `id`: UUID of time entry

**Request Body**:
```json
{
  "breakDuration": "integer, optional, @Min(0), @Max(480)",
  "notes": "string, optional, @Size(max=500)"
}
```

**Success Response**:
- Status: 200 OK
```json
{
  "status": "SUCCESS",
  "data": {
    "id": "uuid",
    "clockIn": "datetime",
    "clockOut": "datetime",
    "breakDuration": "integer",
    "totalDuration": "integer",
    "status": "PENDING"
  }
}
```

## Report Module (`ReportController`)

### POST /api/v1/reports
Generate a new report.

**Authentication**: Required (ROLE_MANAGER, ROLE_ADMIN)

**Request Body**:
```json
{
  "type": "DAILY|MONTHLY|YEARLY|CUSTOM, required",
  "periodStart": "date, required",
  "periodEnd": "date, required",
  "departmentId": "uuid, required",
  "filters": {
    "projectId": "uuid, optional",
    "userId": "uuid, optional",
    "status": "string, optional"
  }
}
```

**Success Response**:
- Status: 202 Accepted
```json
{
  "status": "SUCCESS",
  "data": {
    "id": "uuid",
    "type": "string",
    "status": "PENDING",
    "periodStart": "date",
    "periodEnd": "date"
  }
}
```

### GET /api/v1/reports/{id}
Retrieve a specific report.

**Authentication**: Required (ROLE_MANAGER, ROLE_ADMIN)

**Path Parameters**:
- `id`: UUID of report

**Success Response**:
- Status: 200 OK
```json
{
  "status": "SUCCESS",
  "data": {
    "id": "uuid",
    "type": "string",
    "periodStart": "date",
    "periodEnd": "date",
    "generatedBy": {
      "id": "uuid",
      "name": "string"
    },
    "department": {
      "id": "uuid",
      "name": "string"
    },
    "status": "string",
    "reportData": {
      // Report specific structure
    }
  }
}
```

## Time Off Request Module (`TimeOffController`)

### POST /api/v1/time-off-requests
Submit a time off request.

**Authentication**: Required

**Request Body**:
```json
{
  "startDate": "date, required, @Future",
  "endDate": "date, required, @Future",
  "type": "VACATION|SICK|PERSONAL|OTHER, required",
  "notes": "string, optional, @Size(max=500)"
}
```

**Success Response**:
- Status: 201 Created
```json
{
  "status": "SUCCESS",
  "data": {
    "id": "uuid",
    "startDate": "date",
    "endDate": "date",
    "type": "string",
    "status": "PENDING",
    "notes": "string"
  }
}
```

### PATCH /api/v1/time-off-requests/{id}/status
Update request status (approve/reject).

**Authentication**: Required (ROLE_MANAGER, ROLE_ADMIN)

**Path Parameters**:
- `id`: UUID of request

**Request Body**:
```json
{
  "status": "APPROVED|REJECTED, required",
  "notes": "string, optional, @Size(max=500)"
}
```

**Success Response**:
- Status: 200 OK
```json
{
  "status": "SUCCESS",
  "data": {
    "id": "uuid",
    "status": "string",
    "approvedBy": {
      "id": "uuid",
      "name": "string"
    },
    "notes": "string"
  }
}
```

## Exception Handling

### Global Exception Handler (`GlobalExceptionHandler`)

All exceptions are handled centrally using `@ControllerAdvice`:

```java
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    // Standard Spring exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleValidationExceptions(...)

    // Custom business exceptions
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(...)

    // Security exceptions
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> handleAccessDeniedException(...)
}
```

### Common Error Codes

#### Authentication Errors (1xxx)
- `1001`: Invalid credentials
- `1002`: Token expired
- `1003`: Invalid token
- `1004`: Insufficient permissions

#### Validation Errors (2xxx)
- `2001`: Invalid request format
- `2002`: Invalid date range
- `2003`: Invalid status transition

#### Resource Errors (3xxx)
- `3001`: Resource not found
- `3002`: Resource already exists
- `3003`: Resource state conflict

#### Business Logic Errors (4xxx)
- `4001`: Active clock-in exists
- `4002`: No active clock-in found
- `4003`: Time off request overlap
- `4004`: Report generation failed

## Security Implementation

### Spring Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

### Rate Limiting

Using Spring Boot Actuator and Bucket4j:

```java
@Configuration
public class RateLimitConfig {
    @Bean
    public RateLimiter authRateLimiter() {
        return Bucket4j.builder()
            .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
            .build();
    }
}
```

### Input Validation

Using Spring Validation:

```java
@Validated
@RestController
public class UserController {
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
        @Valid @RequestBody CreateUserRequest request
    ) {
        // Implementation
    }
}
```

### Audit Logging

Using Spring Data JPA Auditing:

```java
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class Auditable {
    @CreatedBy
    private String createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

## API Documentation

The API is documented using SpringDoc OpenAPI:

```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
  packages-to-scan: com.worklog.api.controllers
```

Access Swagger UI: `https://api.worklog.com/swagger-ui.html` 