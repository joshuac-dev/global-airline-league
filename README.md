# Global Airline League

A web-based airline MMORPG simulation - complete rewrite in Kotlin + Ktor + React + TypeScript.

## Overview

Global Airline League is a multiplayer airline management simulation where players build and operate their own airlines, compete for passengers, manage finances, and expand their networks across the world.

This is a **complete rewrite** of the original Scala/Play Framework application, modernizing the technology stack while maintaining and expanding on the original gameplay mechanics.

## Technology Stack

### Backend
- **Language:** Kotlin 1.9.22
- **Framework:** Ktor 2.3.7
- **ORM:** Exposed (Kotlin SQL framework)
- **Database:** PostgreSQL 16 with full-text search
- **Migrations:** Flyway
- **Build:** Gradle 8.5 (Kotlin DSL)

### Frontend
- **Framework:** React 18
- **Language:** TypeScript
- **Build Tool:** Vite
- **Routing:** React Router
- **Maps:** React Leaflet + OpenStreetMap

### Real-time
- **Protocol:** WebSockets (Ktor)
- **Updates:** Coroutine-based background jobs

## Important: Maps Policy

**This project uses OpenStreetMap exclusively.**

- ✅ OpenStreetMap (OSM) and OSM-compatible tile services
- ❌ Google Maps APIs are explicitly banned
- ❌ Do not integrate any Google Maps services

All mapping features must use OpenStreetMap tiles and React Leaflet.

## Project Structure

```
global-airline-league/
├── backend/
│   ├── core/          # Domain models and business logic
│   ├── persistence/   # Database access (Exposed + Flyway)
│   ├── api/           # Ktor HTTP API and WebSockets
│   └── jobs/          # Background simulation jobs
├── frontend/          # React TypeScript SPA
├── docs/              # Architecture and design documentation
├── repo-catalogue/    # Original codebase analysis
└── docker-compose.yml # Local PostgreSQL database
```

## Getting Started

### Prerequisites

- JDK 17 or higher
- Node.js 18 or higher
- Docker and Docker Compose (for PostgreSQL)
- npm or pnpm

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/joshuac-dev/global-airline-league.git
   cd global-airline-league
   ```

2. **Start the database:**
   ```bash
   docker compose up -d
   ```

3. **Run database migrations:**
   ```bash
   ./gradlew flywayMigrate
   ```

4. **Build the backend:**
   ```bash
   ./gradlew build
   ```

5. **Start the backend server:**
   ```bash
   ./gradlew :backend:api:run
   ```
   The API will be available at `http://localhost:8080`

6. **Install frontend dependencies:**
   ```bash
   cd frontend
   npm install
   ```

7. **Start the frontend dev server:**
   ```bash
   npm run dev
   ```
   The UI will be available at `http://localhost:5173`

### Environment Variables

Copy `.env.example` to `.env` and adjust as needed:

```bash
cp .env.example .env
```

Key variables:
- `DATABASE_URL` - PostgreSQL JDBC URL
- `DATABASE_USERNAME` - Database user
- `DATABASE_PASSWORD` - Database password
- `PORT` - Backend server port (default: 8080)

## Development

### Backend

**Run tests:**
```bash
./gradlew test
```

**Build:**
```bash
./gradlew build
```

**Run specific module:**
```bash
./gradlew :backend:api:run
```

### Frontend

**Run dev server:**
```bash
cd frontend
npm run dev
```

**Build for production:**
```bash
npm run build
```

**Lint:**
```bash
npm run lint
```

## API Endpoints

### REST API
- `GET /api/health` - Health check
- `GET /api/airlines` - List airlines
- `GET /api/airports` - List airports
- `GET /api/routes` - List routes
- `GET /api/search` - Search (placeholder)

### WebSocket
- `ws://localhost:8080/ws/world` - World updates
- `ws://localhost:8080/ws/airline/:id` - Airline-specific updates

## Documentation

- [Discovery Summary](docs/discovery-summary.md) - Analysis of original codebase
- [Architecture](docs/architecture.md) - System design and module structure
- [Domain Overview](docs/domain-overview.md) - Business entities and rules
- [Feature Parity](docs/feature-parity.md) - Implementation status tracking

For the complete specification and behavioral reference, see the [`repo-catalogue/`](repo-catalogue/) directory which contains analysis of the original Scala codebase.

## Current Status

**Phase:** Initial Scaffolding ✅

This is the first phase of development, establishing the foundation:

- ✅ Multi-module Gradle project structure
- ✅ Ktor API with REST and WebSocket endpoints
- ✅ PostgreSQL database with Flyway migrations
- ✅ React TypeScript frontend with routing
- ✅ OpenStreetMap integration
- ✅ Background simulation job skeleton
- ✅ Basic WebSocket real-time updates

**Next Steps:**
- Implement authentication and authorization
- Build out REST API endpoints with full CRUD
- Create comprehensive frontend UI
- Implement core simulation logic
- Add comprehensive test coverage
- Set up CI/CD pipeline

See [Feature Parity](docs/feature-parity.md) for detailed status.

## Contributing

Contributions are welcome! Please:

1. Follow the existing code style
2. Write tests for new features
3. Update documentation as needed
4. Ensure all tests pass before submitting PR

### Code Style
- **Backend:** Kotlin conventions, 4-space indentation
- **Frontend:** TypeScript with ESLint, 2-space indentation

## License

[License TBD]

## Acknowledgments

- Original game concept and design
- OpenStreetMap contributors
- Kotlin and Ktor communities
- React and TypeScript communities
