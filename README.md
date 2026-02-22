# Emergency Room (Admissions) Service

A RESTful API service for managing emergency room patient admissions built with Kotlin, Ktor, and PostgreSQL.

## Features

- **Patient Admission**: Register new patients with triage conditions
- **Status Management**: Track patient status (NEW, IN_PROGRESS, TREATED)
- **Triage System**: Support for medical condition priorities (GREEN, YELLOW, ORANGE, RED, BROWN, BLACK)
- **Patient Queries**: Filter and paginate patients by status, condition
- **PESEL Validation**: Ensures unique and valid Polish national identification numbers

## Tech Stack

- **Kotlin** 1.9.22 on JVM 17
- **Ktor** 2.3.7 (Web framework)
- **Koin** 3.5.3 (Dependency injection)
- **Exposed ORM** 0.46.0 (Database)
- **PostgreSQL** 14 (Database)
- **Flyway** 10.4.1 (Migrations)
- **Logback** (Logging)
- **JUnit5 + Mockk** (Testing)

## Prerequisites

- JDK 17 or higher
- Docker and Docker Compose (recommended)
- PostgreSQL 14+ (if running locally without Docker)

## Quick Start with Docker

1. **Build and start the services**:
```bash
docker-compose up --build
```

This will:
- Build the application
- Start PostgreSQL on port 5432
- Run Flyway migrations automatically
- Start the API service on port 8080

2. **Test the API**:
```bash
curl http://localhost:8080/patients/new
```

3. **Stop the services**:
```bash
docker-compose down
```

## Local Development Setup

### 1. Start PostgreSQL

Using Docker:
```bash
docker run --name er-postgres \
  -e POSTGRES_DB=er_db \
  -e POSTGRES_USER=er_user \
  -e POSTGRES_PASSWORD=er_password \
  -p 5432:5432 \
  -d postgres:14-alpine
```

### 2. Build the Project

```bash
./gradlew clean build
```

### 3. Run the Application

```bash
./gradlew run
```

The API will be available at `http://localhost:8080`

## Running Tests

```bash
./gradlew test
```

## API Endpoints

### 1. Admit a Patient
**POST** `/patients`

```bash
curl -X POST http://localhost:8080/patients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "pesel": "12345678901",
    "condition": "RED"
  }'
```

**Response** (201 Created):
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "pesel": "12345678901",
  "condition": "RED",
  "status": "NEW",
  "admittedAt": "2025-10-09T14:30:00"
}
```

### 2. Handle Patient (Mark as In Progress)
**PUT** `/patients/{id}/handle`

```bash
curl -X PUT http://localhost:8080/patients/1/handle
```

**Response** (200 OK):
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "pesel": "12345678901",
  "condition": "RED",
  "status": "IN_PROGRESS",
  "admittedAt": "2025-10-09T14:30:00"
}
```

### 3. Get All Patients
**GET** `/patients`

**Query Parameters**:
- `status` (optional): NEW, IN_PROGRESS, TREATED
- `condition` (optional): GREEN, YELLOW, ORANGE, RED, BROWN, BLACK
- `limit` (optional, default: 100)
- `offset` (optional, default: 0)
- `sort` (optional, default: desc): asc | desc

```bash
# Get all patients
curl http://localhost:8080/patients

# Get NEW patients only
curl http://localhost:8080/patients?status=NEW

# Get RED condition patients with pagination
curl "http://localhost:8080/patients?condition=RED&limit=10&offset=0"
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "pesel": "12345678901",
      "condition": "RED",
      "status": "NEW",
      "admittedAt": "2025-10-09T14:30:00"
    }
  ],
  "total": 1,
  "limit": 100,
  "offset": 0
}
```

### 4. Get New Patients
**GET** `/patients/new`

```bash
curl http://localhost:8080/patients/new
```

## Medical Condition Codes

- **GREEN**: Minor injuries, can wait
- **YELLOW**: Delayed care, stable but needs attention
- **ORANGE**: Urgent, needs prompt care
- **RED**: Immediate, life-threatening
- **BROWN**: Chemical/hazardous contamination
- **BLACK**: Deceased or expectant

## Project Structure

```
er-service/
├── src/main/kotlin/com/example/er/
│   ├── Application.kt          # Main entry point
│   ├── model/                  # Domain models
│   ├── dto/                    # Data transfer objects
│   ├── repository/             # Database access
│   ├── service/                # Business logic
│   ├── routes/                 # HTTP routes
│   └── di/                     # Dependency injection
├── src/main/resources/
│   ├── application.conf        # Configuration
│   ├── logback.xml            # Logging
│   └── db/migration/          # Flyway migrations
├── src/test/                   # Tests
├── build.gradle.kts           # Build configuration
├── Dockerfile                 # Container image
├── docker-compose.yml         # Multi-container setup
└── requests.http              # Sample requests
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/er_db` |
| `DB_USER` | Database username | `er_user` |
| `DB_PASSWORD` | Database password | `er_password` |
| `PORT` | HTTP server port | `8080` |

## Opening in IntelliJ IDEA

1. Open IntelliJ IDEA
2. Click **File > Open**
3. Navigate to the `er-service` directory
4. Click **OK**
5. IntelliJ will automatically detect the Gradle project and import it
6. Wait for Gradle sync to complete
7. Run the application using the green play button or `./gradlew run`

## Troubleshooting

### Database Connection Issues
- Verify PostgreSQL is running: `docker ps`
- Check connection parameters in `.env` or `application.conf`

### Port Already in Use
- Kill process using port 8080: `lsof -ti:8080 | xargs kill -9`
- Or change port in `application.conf`

### Build Failures
- Ensure JDK 17 is installed: `java -version`
- Clear Gradle cache: `./gradlew clean build --no-cache`

## License

This is a demonstration project.

---

**Built with ❤️ using Kotlin and Ktor**
