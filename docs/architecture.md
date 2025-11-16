# Architecture

## Overview
Global Airline League uses a **modular monolith** architecture with clear boundaries between layers and modules. The system is divided into backend (Kotlin + Ktor) and frontend (React + TypeScript) communicating via REST API and WebSockets.

## Module Structure

### Backend Modules

#### backend/core
**Purpose:** Pure domain logic and simulation models.

**Responsibilities:**
- Domain entities (Airline, Airport, Route, Aircraft, etc.)
- Business rules and validations
- Simulation models (Demand, Pricing, Finance)
- No dependencies on HTTP, database, or external systems

**Key Interfaces:**
- `SimulationClock` - Game tick management
- `DemandModel` - Passenger demand calculations
- `PricingModel` - Dynamic pricing logic

#### backend/persistence
**Purpose:** Database access and migrations.

**Responsibilities:**
- Exposed table definitions
- Repository interfaces and implementations
- Flyway migration scripts
- DatabaseFactory for connection pooling (HikariCP)

**Dependencies:** `backend/core`

**Database:**
- PostgreSQL 16
- Full-text search for airports
- Transactions managed by Exposed

#### backend/api
**Purpose:** HTTP API and WebSocket endpoints.

**Responsibilities:**
- Ktor application setup
- REST route handlers
- WebSocket connection management
- Request/response serialization (kotlinx.serialization)
- CORS, logging, error handling

**Dependencies:** `backend/core`, `backend/persistence`

**Endpoints:**
- REST: `/api/health`, `/api/airlines`, `/api/airports`, `/api/routes`, `/api/search`
- WebSocket: `/ws/world`, `/ws/airline/{id}`

#### backend/jobs
**Purpose:** Background simulation tick engine.

**Responsibilities:**
- Coroutine-based tick job
- World state updates
- Broadcasting to WebSocket clients
- Tied to Ktor application lifecycle

**Dependencies:** `backend/core`, `backend/api`

### Frontend

**Structure:** React + TypeScript SPA with Vite.

**Key Components:**
- **Pages:** Home, World, Airports, Airline, Routes
- **API Client:** Fetch-based client for REST endpoints
- **WebSocket Hooks:** Custom React hooks for real-time updates
- **Map Component:** React Leaflet with OpenStreetMap tiles

**Routing:** React Router for navigation

## Layered Architecture

```
┌─────────────────────────────────────────────┐
│           Frontend (React + TS)             │
│  ┌────────┬────────┬────────┬──────────┐   │
│  │ Pages  │ Hooks  │ API    │ Components│   │
│  └────────┴────────┴────────┴──────────┘   │
└─────────────────┬───────────────────────────┘
                  │ HTTP/WS
┌─────────────────▼───────────────────────────┐
│          backend/api (Ktor)                 │
│  ┌──────────┬───────────┬──────────────┐   │
│  │  Routes  │ WebSockets│ Serialization│   │
│  └──────────┴───────────┴──────────────┘   │
└─────────────┬───────────────────────────────┘
              │
    ┌─────────┼─────────┐
    │         │         │
┌───▼─────┐ ┌─▼──────┐ ┌▼──────────────┐
│ backend/│ │backend/│ │  backend/jobs │
│  core   │ │persist │ │  (tick loop)  │
│         │ │        │ └───────────────┘
└─────────┘ └────┬───┘
                 │
           ┌─────▼──────┐
           │ PostgreSQL │
           │    + FTS   │
           └────────────┘
```

## Key Design Patterns

### 1. Repository Pattern
- Abstract data access behind interfaces in `backend/persistence`
- Domain layer depends on repository interfaces, not implementations

### 2. Dependency Injection (Manual)
- Repositories instantiated in API module
- Passed to route handlers as needed
- Future: Consider Koin or Kodein for DI

### 3. Event Broadcasting
- Simulation tick emits WorldUpdate events
- WebSocketBroadcaster distributes to connected clients
- Future: Consider event bus for complex scenarios

### 4. Coroutines for Concurrency
- All async operations use Kotlin coroutines
- Structured concurrency via `CoroutineScope`
- Background jobs run in supervised scope

## Data Flow

### Read Path (Query)
1. Frontend makes GET request to `/api/airlines`
2. Ktor route handler receives request
3. Handler calls repository method
4. Repository queries database via Exposed
5. Results serialized to JSON and returned
6. Frontend updates UI

### Write Path (Command)
1. Frontend makes POST/PUT to `/api/airlines`
2. Ktor route handler validates request
3. Domain entities created/updated
4. Repository persists changes
5. Success response returned
6. (Future) Events emitted for simulation engine

### Real-time Path (WebSocket)
1. Frontend connects to `/ws/world`
2. Connection registered in WebSocketBroadcaster
3. Background tick job runs every N seconds
4. Tick job updates simulation state
5. WorldUpdate event broadcast to all connections
6. Frontend receives update and re-renders

## Configuration

### Backend
- `application.conf` (HOCON format)
- Environment variables override defaults
- Database URL, credentials, ports

### Frontend
- Vite config for dev server and proxy
- Environment variables (future)

## Security Considerations

### Current (Development)
- CORS: Allow all origins (dev only)
- No authentication/authorization
- Database credentials in environment variables

### Future (Production)
- JWT-based authentication
- Role-based authorization
- Rate limiting
- Input validation and sanitization
- HTTPS only
- Restricted CORS origins

## Deployment Model

### Development
- Backend: `./gradlew :backend:api:run` (port 8080)
- Frontend: `npm run dev` (port 5173, proxies to 8080)
- Database: Docker Compose (port 5432)

### Production (Future)
- Backend: JAR deployment or Docker container
- Frontend: Static build served via CDN or embedded in backend
- Database: Managed PostgreSQL (RDS, Cloud SQL, etc.)
- Reverse proxy (nginx) for SSL termination

## Observability

### Logging
- SLF4J + Logback
- Ktor CallLogging for HTTP requests
- Structured logging (JSON) for production

### Monitoring (Future)
- Micrometer for metrics
- Health checks
- Database connection pool metrics
- WebSocket connection count

### Tracing (Future)
- OpenTelemetry for distributed tracing

## Extensibility

### Adding New Features
1. Define domain model in `backend/core`
2. Add database migration in `backend/persistence`
3. Create repository interface and impl
4. Add REST/WebSocket endpoints in `backend/api`
5. Update frontend with new pages/components

### Plugin Architecture (Future)
- Event-driven system for modularity
- Plugin loading for custom airline behaviors
- Scriptable game events

## Testing Strategy

### Unit Tests
- Domain logic in `backend/core` (JUnit5 + Kotest)
- Pure functions, no I/O

### Integration Tests
- Repository tests with test database
- API tests with Ktor test client

### End-to-End Tests (Future)
- Playwright or Cypress for frontend
- Full stack scenarios

## Migration from Original
1. Data export from MySQL
2. Transform to match new schema
3. Import via Flyway seed migrations
4. Validate data integrity
5. Gradual feature parity

## Performance Targets
- API response time: < 100ms (p95)
- WebSocket latency: < 50ms
- Database queries: < 50ms (with indexes)
- Frontend load time: < 2s
- Simulation tick: < 1s

## Scalability (Future)
- Horizontal scaling: Multiple API instances behind load balancer
- Database: Read replicas for queries
- WebSocket: Redis pub/sub for cross-instance broadcasting
- Caching: Redis for hot data
