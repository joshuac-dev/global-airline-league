# Architecture

## Overview

Global Airline League is a modular monolithic application built with Kotlin, Ktor, and PostgreSQL. The system is organized into discrete backend modules with clear boundaries and dependency rules, avoiding microservices complexity while maintaining separation of concerns.

## Module Structure

The backend is split into four Gradle modules:

### backend/core
**Pure domain and simulation services**

- Contains business logic, domain entities, value objects, and simulation algorithms
- Framework-free: no dependencies on HTTP, database, or UI frameworks
- Represents the "heart" of the application - game rules, economic calculations, route optimization, etc.
- Examples: `GameClock`, `AirlineId`, `AirportId`, domain services for route planning, pricing, reputation

### backend/persistence
**Database access layer**

- Depends on: `backend/core`
- Implements repositories and data access using Jetbrains Exposed (SQL DSL and DAO)
- Manages database schema via Flyway migrations
- Provides `DatabaseFactory` for connection pooling (HikariCP) and transaction management
- PostgreSQL-specific features: Full-Text Search (FTS) for airport/airline search
- Exposed table definitions map to domain entities from `core`

### backend/api
**HTTP/WebSocket API server**

- Depends on: `backend/core`, `backend/persistence`
- Ktor-based REST API and WebSocket endpoints
- Handles:
  - REST routes: `/api/airlines`, `/api/airports`, `/api/routes`, `/api/search`, etc.
  - WebSocket channels: `/ws/world` (global game state), `/ws/airline/{id}` (airline-specific updates)
  - Content negotiation (kotlinx-serialization JSON)
  - CORS, authentication, exception handling, request logging
- DTOs for request/response serialization (separate from domain models)
- `/health` endpoint for monitoring

### backend/jobs
**Background processing**

- Depends on: `backend/core`, `backend/persistence`
- Coroutine-based scheduled tasks and simulation tick processing
- Examples: `WorldTicker` (advances game clock, triggers simulation events every N seconds)
- Configurable intervals via environment variables
- Will be integrated into the Ktor lifecycle in a future PR

## Dependency Rules

```
       ┌─────────┐
       │   api   │
       └────┬────┘
            │
       ┌────▼────┐      ┌──────┐
       │ persist │◄─────┤ jobs │
       └────┬────┘      └──────┘
            │
       ┌────▼────┐
       │  core   │
       └─────────┘
```

- **core** has no dependencies on other modules
- **persistence** depends only on **core**
- **api** depends on **core** and **persistence**
- **jobs** depends on **core** and **persistence**
- No reverse dependencies allowed (e.g., core cannot depend on persistence)

## Technology Stack

### Backend
- **Language:** Kotlin 2.x with JVM target 17
- **Build:** Gradle with Kotlin DSL
- **Web Framework:** Ktor (CIO or Netty engine)
- **Database:** PostgreSQL with Jetbrains Exposed ORM
- **Migrations:** Flyway
- **Connection Pooling:** HikariCP
- **Serialization:** kotlinx-serialization (JSON)
- **Concurrency:** Kotlin Coroutines
- **Logging:** Logback with SLF4J

### Frontend (Future PR)
- **Framework:** React with TypeScript
- **Mapping:** OpenStreetMap via Leaflet or MapLibre GL JS
- **Build:** Vite or similar modern tooling
- **State:** To be determined (likely Redux Toolkit or Zustand)

## Real-Time Updates

Real-time communication uses **Ktor WebSockets** (not Server-Sent Events or polling).

### WebSocket Endpoints

#### `/ws/world`
Global game state updates broadcast to all connected clients:
- Simulation tick notifications
- Global market changes
- Major events (oil price changes, world events)
- Real-time chat messages (global channel)

#### `/ws/airline/{airlineId}`
Airline-specific updates for authenticated users:
- Airline financial changes
- Route performance updates
- Notifications and alerts
- Direct messages

### Message Categories (High-Level)
- `TICK` - Simulation advanced
- `AIRLINE_UPDATE` - Financial, reputation, or operational change
- `MARKET_UPDATE` - Oil prices, demand shifts
- `CHAT` - Chat messages
- `NOTIFICATION` - User alerts

Detailed message schemas will be defined in a subsequent PR.

## Database

### Technology
- **RDBMS:** PostgreSQL (version 14+)
- **ORM:** Jetbrains Exposed (type-safe SQL DSL + DAO layer)
- **Migrations:** Flyway for versioned schema evolution
- **Connection Pool:** HikariCP

### Configuration
Database connection configured via environment variables:
- `DB_URL` - JDBC connection string (e.g., `jdbc:postgresql://localhost:5432/gal`)
- `DB_USER` - Database username
- `DB_PASSWORD` - Database password

Additional tuning via `application.conf` or env vars as needed.

### Full-Text Search
PostgreSQL's built-in Full-Text Search (FTS) will be used for:
- Airport search (by name, city, IATA code)
- Airline search (by name, code)
- Potentially route/destination search

This avoids external search engines (Elasticsearch, etc.) and keeps the stack simple.

## Mapping and Geospatial

**OpenStreetMap (OSM) only** - Google Maps is explicitly prohibited.

### Frontend Mapping (Future)
- **Tile Provider:** OpenStreetMap tiles or a self-hosted tile server
- **Library:** Leaflet.js or MapLibre GL JS
- **Features:**
  - Airport markers with popups
  - Flight route polylines
  - Heatmaps for demand or coverage
  - Interactive controls (zoom, pan, search)

### Geocoding
- Use OSM Nominatim API (rate-limited) or pre-seeded airport coordinates
- No Google Geocoding API

## Testing Strategy

### Unit Tests
- **Location:** Within each module's `src/test` directory
- **Focus:** Pure business logic in `backend/core`
- **Framework:** JUnit 5 (Jupiter) with Kotlin test utilities
- **Assertions:** Kotlin test or AssertJ
- **Mocking:** MockK for Kotlin

### Integration Tests
- **Persistence Layer:**
  - Use Testcontainers for PostgreSQL to test real database interactions
  - Verify Exposed queries, migrations, and repository logic
- **API Layer:**
  - Ktor `testApplication` utilities for HTTP/WebSocket testing
  - Test routes, serialization, error handling
  - Mock persistence layer or use in-memory test database

### Test Coverage Goals
- Core domain logic: High coverage (>80%)
- Persistence: Medium coverage (integration tests on key repositories)
- API: Medium coverage (happy path + error cases)

## Configuration

### Application Configuration
- **Ktor Config:** `application.conf` (HOCON format) in `backend/api/src/main/resources`
- **Environment Variables:** Override config values for deployment
  - `PORT` - HTTP server port (default: 8080)
  - `DB_URL`, `DB_USER`, `DB_PASSWORD` - Database connection
  - `TICK_INTERVAL_SECONDS` - Simulation tick frequency (default: 5)
  - `LOG_LEVEL` - Logging verbosity (default: INFO)

### Profiles
- **Local Development:** Use defaults from `application.conf`
- **Production:** Override via environment variables or external config file

## Development Workflow

### Local Setup
1. Clone repository
2. Ensure JDK 21+ installed
3. Run `./gradlew build` to compile and test
4. (Optional) Start PostgreSQL locally for full integration tests
5. Run API server: `./gradlew :backend:api:run`
6. Access health check: `http://localhost:8080/health`

### Build Commands
- `./gradlew build` - Compile all modules and run tests
- `./gradlew :backend:core:build` - Build specific module
- `./gradlew :backend:api:run` - Start API server
- `./gradlew test` - Run all tests
- `./gradlew clean` - Clean build artifacts

### CI/CD (Future)
- GitHub Actions for PR checks (build, test, lint)
- Docker images for deployment
- Database migrations run on deployment

## Security Considerations

### Authentication & Authorization (Future PR)
- To be determined: JWT tokens, session-based, or OAuth2
- Role-based access control (player vs. admin)
- Rate limiting on API endpoints

### Data Validation
- Input validation at API boundary (Ktor)
- Domain invariants enforced in `core` module
- SQL injection prevention via Exposed parameterized queries

### Secrets Management
- No hardcoded credentials
- Database credentials via environment variables
- Future: Use secret management service (AWS Secrets Manager, HashiCorp Vault)

## Future Enhancements
- Dependency Injection framework (Koin or Kodein) if wiring becomes complex
- API versioning (`/api/v1/...`)
- GraphQL endpoint as alternative to REST (if needed)
- Caching layer (Redis) for hot data (leaderboards, airport lists)
- Observability: Prometheus metrics, distributed tracing
- Multi-region deployment with read replicas

## Constraints and Decisions

### Modular Monolith (Not Microservices)
We deliberately choose a modular monolith over microservices:
- Simpler deployment and operations
- Easier to refactor and evolve boundaries
- Lower latency for inter-module communication
- Can still extract modules into separate services later if needed

### OpenStreetMap Only
- Google Maps API is explicitly prohibited (licensing, cost)
- All mapping/geocoding uses OSM data and services
- Self-hosted tiles considered for production (avoids rate limits)

### PostgreSQL for Everything
- Single database for simplicity (no polyglot persistence yet)
- Full-Text Search in Postgres (no Elasticsearch)
- JSONB columns for flexible/semi-structured data if needed

### Incremental Development
- Work proceeds in small, focused PRs
- Guided by feature parity checklist (see `feature-parity-checklist.md`)
- Catalogue files from original game (`repo-catalogue/`) inform requirements

## References
- [Feature Parity Checklist](./feature-parity-checklist.md)
- [Repository Catalogue Index](../repo-catalogue/INDEX.md)
- Ktor Documentation: https://ktor.io/
- Exposed Documentation: https://github.com/JetBrains/Exposed
- OpenStreetMap: https://www.openstreetmap.org/
