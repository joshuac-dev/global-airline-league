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

## Module Boundaries: Airport Slice Example

The airport implementation demonstrates the clean separation of concerns across modules:

### `backend/core/src/main/kotlin/com/gal/core/airport/`
- **Airport.kt**: Domain entity with validation (lat/lon bounds, required fields)
- **Value objects**: `CountryCode`, `IATA`, `ICAO` (inline classes for type safety)
- **No dependencies** on frameworks, database, or HTTP
- Pure Kotlin data classes and business rules

### `backend/persistence/src/main/kotlin/com/gal/persistence/airport/`
- **Airports.kt**: Exposed table definition mapping to database schema
- **AirportRepository.kt**: Interface defining data access operations
- **AirportRepositoryExposed.kt**: Implementation using Exposed ORM + raw SQL for search
- **Migration**: `V1__create_airports.sql` (Flyway-managed schema)

### `backend/api/src/main/kotlin/com/gal/api/airport/`
- **AirportDto.kt**: Serializable response models (`AirportResponse`)
- **AirportRoutes.kt**: Ktor routes for list, get, and search endpoints
- **RepositoryLocator.kt**: Simple dependency wiring (can be replaced with DI framework later)
- Handles HTTP concerns: status codes, query parameters, error responses

This pattern will be replicated for airlines, routes, aircraft, and other domain slices.

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

## Frontend SPA / Mapping Layer

The frontend is a single-page application (SPA) built with modern web technologies, providing an interactive map-based interface for airline management.

### Technology Stack
- **Framework:** React 18 with TypeScript (strict mode)
- **Build Tool:** Vite 7 with Hot Module Replacement (HMR)
- **Mapping Library:** React Leaflet 4.2 (wrapper for Leaflet.js)
- **Tile Provider:** OpenStreetMap raster tiles (https://tile.openstreetmap.org)
- **Testing:** Vitest 4 + React Testing Library
- **Code Quality:** ESLint with TypeScript support, strict tsconfig

### Rationale vs. Original Google Maps

The original game used Google Maps JavaScript API for mapping functionality. The rewrite explicitly prohibits Google Maps due to:
1. **Licensing Costs**: Google Maps API is not free for commercial use
2. **Vendor Lock-in**: Reduces flexibility and control
3. **Open Source Alignment**: OSM is community-driven and free

**Migration Path**: React Leaflet was chosen for its:
- **Simplicity**: Easier to integrate than MapLibre GL for basic marker rendering
- **Maturity**: Well-documented, large community, stable API
- **Bundle Size**: Smaller than MapLibre GL for initial use case

**Future Consideration**: MapLibre GL offers better performance for large datasets (> 10K markers) and vector tiles. Migration path exists if needed.

### Architecture Overview

```
┌─────────────────────────────────────────┐
│         Frontend (React SPA)            │
├─────────────────────────────────────────┤
│  ┌─────────────┐     ┌──────────────┐  │
│  │   MapView   │     │  SearchBox   │  │
│  │  (Leaflet)  │     │  (debounced) │  │
│  └─────────────┘     └──────────────┘  │
│         │                     │         │
│  ┌──────▼─────────────────────▼──────┐ │
│  │      API Client (fetch)            │ │
│  └────────────────┬───────────────────┘ │
└───────────────────┼─────────────────────┘
                    │ HTTP/WS
          ┌─────────▼──────────┐
          │  Backend API (Ktor) │
          │   /api/airports     │
          │   /api/search       │
          │   /ws/world (future)│
          └─────────────────────┘
```

### Core Components

#### MapView (`src/components/MapView.tsx`)
- Renders interactive OpenStreetMap with airport markers
- Supports marker highlighting (selected airport uses blue icon)
- Popup display with airport details (name, IATA/ICAO, city, country)
- Fly-to animation when airport is selected
- Responsive layout (full viewport minus header)

**Future**: Marker clustering, viewport-based lazy loading, route polylines

#### SearchBox (`src/components/SearchBox.tsx`)
- Debounced search input (300ms) calling `/api/search/airports`
- Dropdown results with keyboard navigation
- Abort controller pattern to cancel stale requests
- "No matches" state for empty results

**Future**: Recent searches, advanced filters, client-side caching

#### App (`src/App.tsx`)
- Root component managing global state (airports, selection, loading)
- Pagination control ("Load More" button)
- Error handling with retry functionality
- Coordinates SearchBox and MapView interactions

### API Integration

**API Client** (`src/api/airports.ts`):
- `fetchAirports(params)` - Paginated airport list
- `searchAirports(params)` - Airport search by query

**Configuration**:
- Base URL from `VITE_API_BASE_URL` environment variable
- Dev proxy: `/api/*` → `http://localhost:8080/api/*`
- AbortSignal support for request cancellation

**CORS Handling**: In development, Vite dev server proxies API requests to avoid CORS issues. In production, use reverse proxy (Nginx/Caddy) or enable CORS in Ktor.

### State Management

**Current**: React built-in hooks (`useState`, `useEffect`) with local component state.

**Future**: When complexity grows (user sessions, multiple views, real-time updates), migrate to:
- **React Query**: Server state caching, WebSocket integration
- **Zustand**: Lightweight global state for UI state

### Mapping Features

#### Implemented
- OSM raster tiles with attribution
- Airport markers (custom icons for selected/default)
- Interactive popups with airport details
- Fly-to animation on selection
- Responsive map container

#### Future (Marked with `@future` comments)
- **Marker Clustering**: Use `react-leaflet-markercluster` for > 500 airports
- **Viewport-based Loading**: Fetch airports only in visible map bounds
- **Custom Marker Sprites**: Different icons for airport sizes/types
- **Route Polylines**: Display flight routes between airports
- **Demand Heatmaps**: Overlay passenger demand data

### WebSocket Support (Placeholder)

A `useWorldSocket` hook stub exists in `src/hooks/useWorldSocket.ts` for future real-time updates:
- `/ws/world` - Simulation tick, oil prices, events, chat
- `/ws/airline/{id}` - Airline-specific updates

**Implementation Plan**: Use `reconnecting-websocket` library (as in original), integrate with React Query for state updates.

### Testing Strategy

- **Unit Tests**: API client with mocked `fetch`
- **Component Tests**: SearchBox (debounce, results), MapView (markers, popups)
- **Integration Tests (Future)**: E2E with Playwright

**Current Coverage**: 16 tests passing (API client, SearchBox, MapView)

### Build & Deployment

**Development**:
```bash
cd frontend
npm install
npm run dev
```

**Production Build**:
```bash
npm run build
# Output: frontend/dist/
```

**Deployment Options**:
1. Static hosting (Cloudflare Pages, Netlify, Vercel)
2. Serve from Ktor static routes
3. Nginx reverse proxy

### Performance Considerations

- Initial load limited to 200 airports (configurable)
- Search debounced (300ms)
- Request cancellation for stale searches
- **TODO**: Marker clustering for large datasets

### Browser Support

- Modern evergreen browsers (Chrome, Firefox, Safari, Edge)
- ES2022 target (no IE11 support)
- No polyfills required

### Documentation

- [Frontend Overview](./frontend-overview.md) - Detailed component responsibilities and extension points
- [Frontend README](../frontend/README.md) - Getting started, build commands

### References

- Original JS implementation: `repo-catalogue/airline-web__public__javascripts.md`
- Backend API: `repo-catalogue/airline-web__app__controllers.md`
- OSM Tile Policy: https://operations.osmfoundation.org/policies/tiles/


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

#### Airport Search Implementation

The airport search functionality uses a pragmatic approach:

**Database Schema (V1__create_airports.sql):**
- Generated `tsvector` column combining IATA, ICAO, name, city, and country code
- Weighted search vectors: IATA/ICAO (weight A), name (weight A), city (weight B), country (weight C)
- GIN index on `search_vector` for efficient full-text queries
- Uses English text search configuration for name/city, simple for codes

**Repository Implementation:**
- Primary implementation uses ILIKE pattern matching for simplicity and portability
- Full-text search infrastructure prepared in database schema for future optimization
- Search query: `SELECT * FROM airports WHERE name ILIKE '%query%' OR iata ILIKE '%query%' OR icao ILIKE '%query%' OR city ILIKE '%query%'`
- Results ordered by name (alphabetically)

**Future Enhancements:**
- Implement native PostgreSQL FTS ranking: `ts_rank(search_vector, plainto_tsquery('english', query))`
- Add fuzzy matching for typo tolerance
- Consider trigram similarity for better partial matches

**Benefits of this approach:**
- PostgreSQL's built-in FTS avoids external dependencies (Elasticsearch, etc.)
- ILIKE works across all database backends (H2, PostgreSQL) for testing
- Simple to understand and maintain
- Scales well for expected airport dataset size (~10K-40K records)

#### Future Search Features
- Airline search (by name, code) - not yet implemented
- Route/destination search - not yet implemented

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
