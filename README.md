# Global Airline League

A web-based airline MMORPG built with Kotlin, Ktor, and PostgreSQL.

## Overview

Global Airline League is a modular monolithic application where players manage virtual airlines, create routes, purchase aircraft, and compete in a real-time simulation. This is a rewrite of the original Scala/Play Framework game, modernized with Kotlin and organized as a clean, maintainable codebase.

## Documentation

- [Architecture](./docs/architecture.md) - System design, module boundaries, and tech stack
- [Ubuntu Installation Guide](./docs/ubuntu-installation.md) - From-scratch setup on Ubuntu Server
- [Feature Parity Checklist](./docs/feature-parity-checklist.md) - Progress tracking against original game features
- [Repository Catalogue](./repo-catalogue/INDEX.md) - Analysis of the original game codebase

## Project Structure

```
global-airline-league/
├── backend/
│   ├── core/         # Pure domain logic (framework-free)
│   ├── persistence/  # Database layer (Exposed + PostgreSQL)
│   ├── api/          # Ktor REST API + WebSockets
│   └── jobs/         # Background coroutine-based jobs
├── docs/             # Project documentation
└── repo-catalogue/   # Original codebase analysis
```

## Prerequisites

- **JDK 17+** (for running the application)
- **Gradle** (wrapper included)
- **PostgreSQL 14+** (optional for now; not required for basic health check)

## Quick Start

### Build the Project

```bash
./gradlew build
```

### Run the API Server

```bash
./gradlew :backend:api:run
```

The server will start on `http://localhost:8080` (or port specified by `PORT` env var).

### Test the Health Endpoint

```bash
curl http://localhost:8080/health
```

Expected response:
```json
{"status":"ok"}
```

### Run Tests

```bash
./gradlew test
```

## Configuration

The application is configured via environment variables:

- `PORT` - HTTP server port (default: 8080)
- `DB_URL` - PostgreSQL JDBC URL (default: `jdbc:postgresql://localhost:5432/gal`)
- `DB_USER` - Database username (default: `gal`)
- `DB_PASSWORD` - Database password (default: `gal`)
- `TICK_INTERVAL_SECONDS` - Simulation tick frequency (default: 5)

### Database Setup

The application requires PostgreSQL 14+ for full functionality. Database migrations are managed by Flyway and run automatically on startup.

#### Setting up PostgreSQL locally

1. **Install PostgreSQL 14+**
   ```bash
   # Ubuntu/Debian
   sudo apt install postgresql postgresql-contrib
   
   # macOS
   brew install postgresql@14
   ```

2. **Create database and user**
   ```bash
   sudo -u postgres psql
   ```
   ```sql
   CREATE DATABASE gal;
   CREATE USER gal WITH ENCRYPTED PASSWORD 'gal';
   GRANT ALL PRIVILEGES ON DATABASE gal TO gal;
   \q
   ```

3. **Seed sample airport data** (optional, for testing)
   ```bash
   psql -U gal -d gal -f docs/dev/seed_airports.sql
   ```

#### Environment Variables for Database

Set these before running the application:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/gal"
export DB_USER="gal"
export DB_PASSWORD="gal"
```

Then initialize the database in your application startup (migrations run automatically).

### API Endpoints

Once the database is configured, the following endpoints are available:

- `GET /health` - Health check (always available)
- `GET /api/airports` - List airports with pagination (`?offset=0&limit=50&country=US`)
- `GET /api/airports/{id}` - Get a specific airport by ID
- `GET /api/search/airports` - Search airports (`?q=heathrow&limit=10`)

**Note:** If the database is not configured, airport endpoints return `503 Service Unavailable`.

## Development Workflow

### Build a Specific Module

```bash
./gradlew :backend:core:build
./gradlew :backend:api:build
```

### Run Tests for a Specific Module

```bash
./gradlew :backend:core:test
./gradlew :backend:api:test
```

### Clean Build Artifacts

```bash
./gradlew clean
```

## Key Technologies

- **Language:** Kotlin 2.0.21
- **Build Tool:** Gradle with Kotlin DSL
- **Web Framework:** Ktor 2.3.12
- **Database:** PostgreSQL with Jetbrains Exposed ORM
- **Migrations:** Flyway
- **Serialization:** kotlinx-serialization
- **Concurrency:** Kotlin Coroutines
- **Testing:** JUnit 5, MockK

## Contributing

This project follows a modular architecture with strict dependency rules:

- `backend/core` has no framework dependencies
- `backend/persistence` depends only on `core`
- `backend/api` depends on `core` and `persistence`
- `backend/jobs` depends on `core` and `persistence`

See [Architecture](./docs/architecture.md) for detailed guidelines.

## Roadmap

Development follows the [Feature Parity Checklist](./docs/feature-parity-checklist.md), guided by the analysis in the [Repository Catalogue](./repo-catalogue/INDEX.md). Work proceeds in small, focused PRs.

## License

[To be determined]

## Contact

[To be determined]
