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
├── frontend/         # React + TypeScript SPA (Vite)
├── docs/             # Project documentation
└── repo-catalogue/   # Original codebase analysis
```

## Prerequisites

- **JDK 17+** (for running the backend)
- **Gradle** (wrapper included)
- **PostgreSQL 14+** (optional for now; not required for basic health check)
- **Node.js 20+** and **npm 10+** (for frontend development)

## Quick Start

### Automated Setup (Recommended)

The easiest way to get started is using the automated setup script:

```bash
./setup-dev.sh
```

This script will:
- Start a PostgreSQL database in Docker
- Create the `.env` configuration file
- Build the backend
- Run database migrations
- Seed test airport data

After setup completes:

```bash
# Terminal 1: Start the backend
./gradlew :backend:api:run

# Terminal 2: Start the frontend
cd frontend
npm install
npm run dev
```

Then open http://localhost:5173 in your browser.

### Manual Setup

If you prefer to set up manually or don't have Docker:

#### 1. Set up PostgreSQL

Install PostgreSQL 14+ and create the database:

```bash
# Create database and user
sudo -u postgres psql
```

```sql
CREATE DATABASE gal;
CREATE USER gal WITH ENCRYPTED PASSWORD 'gal';
GRANT ALL PRIVILEGES ON DATABASE gal TO gal;
\q
```

#### 2. Configure Environment

Create a `.env` file:

```bash
cp .env.example .env
```

Edit if needed to match your database setup.

#### 3. Build and Run

```bash
# Build the project
./gradlew build

# Run the API Server (migrations run automatically)
./gradlew :backend:api:run
```

The server will start on `http://localhost:8080`.

#### 4. Seed Test Data

```bash
psql -U gal -d gal < docs/dev/seed_airports.sql
```

#### 5. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

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

The application is configured via environment variables. You can set them in two ways:

### Option 1: Using a .env file (Recommended)

Create a `.env` file in the project root directory (copy from `.env.example`):

```bash
cp .env.example .env
```

Edit the `.env` file with your configuration:

```env
# Database connection
DB_URL=jdbc:postgresql://localhost:5432/gal
DB_USER=gal
DB_PASSWORD=gal

# API server
PORT=8080

# Simulation
TICK_INTERVAL_SECONDS=5
```

### Option 2: Using system environment variables

Alternatively, export environment variables in your shell:

- `PORT` - HTTP server port (default: 8080)
- `DB_URL` - PostgreSQL JDBC URL (default: `jdbc:postgresql://localhost:5432/gal`)
- `DB_USER` - Database username (default: `gal`)
- `DB_PASSWORD` - Database password (default: `gal`)
- `TICK_INTERVAL_SECONDS` - Simulation tick frequency (default: 5)

**Note:** System environment variables take precedence over .env file values.

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

3. **Import airport data**
   
   Create a `.env` file with your configuration or set environment variables:
   ```bash
   # Option 1: Create .env file (recommended)
   cp .env.example .env
   # Edit .env and set IMPORT_AIRPORTS_CSV=/path/to/airports.csv
   
   # Download the dataset
   curl -o airports.csv https://davidmegginson.github.io/ourairports-data/airports.csv
   
   # Run the importer (reads from .env file)
   ./gradlew :backend:jobs:importAirports
   ```
   
   Or use environment variables:
   ```bash
   # Option 2: Use environment variables
   curl -o airports.csv https://davidmegginson.github.io/ourairports-data/airports.csv
   export IMPORT_AIRPORTS_CSV="$(pwd)/airports.csv"
   ./gradlew :backend:jobs:importAirports
   ```
   
   For detailed instructions, see the [Airport Import Guide](./docs/dev/airport-import.md).

#### Environment Variables for Database

You can configure database connection using a `.env` file or system environment variables:

**Using .env file (recommended):**
```bash
cp .env.example .env
# Edit .env with your database credentials
```

**Using system environment variables:**
```bash
export DB_URL="jdbc:postgresql://localhost:5432/gal"
export DB_USER="gal"
export DB_PASSWORD="gal"
```

The application will automatically load configuration from the `.env` file if it exists. System environment variables take precedence over `.env` file values.

Then initialize the database in your application startup (migrations run automatically).

### API Endpoints

Once the database is configured, the following endpoints are available:

- `GET /health` - Health check (always available)
- `GET /api/airports` - List airports with pagination (`?offset=0&limit=50&country=US`)
- `GET /api/airports/{id}` - Get a specific airport by ID
- `GET /api/search/airports` - Search airports (`?q=heathrow&limit=10`)

**Note:** If the database is not configured, airport endpoints return `503 Service Unavailable`.

## Frontend Development

The frontend is a React + TypeScript single-page application with an interactive OpenStreetMap.

### Setup Frontend

```bash
cd frontend
npm install
```

### Run Frontend Development Server

```bash
cd frontend
npm run dev
```

The frontend will be available at `http://localhost:5173`. API requests are automatically proxied to the backend at `http://localhost:8080`.

### Build Frontend for Production

```bash
cd frontend
npm run build
```

The production build will be output to `frontend/dist/`.

### Frontend Tests

```bash
cd frontend
npm test
```

For more details, see the [Frontend README](./frontend/README.md) and [Frontend Overview](./docs/frontend-overview.md).

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

### Backend
- **Language:** Kotlin 2.0.21
- **Build Tool:** Gradle with Kotlin DSL
- **Web Framework:** Ktor 2.3.12
- **Database:** PostgreSQL with Jetbrains Exposed ORM
- **Migrations:** Flyway
- **Serialization:** kotlinx-serialization
- **Concurrency:** Kotlin Coroutines
- **Testing:** JUnit 5, MockK

### Frontend
- **Language:** TypeScript (strict mode)
- **Framework:** React 18
- **Build Tool:** Vite 7
- **Mapping:** React Leaflet + OpenStreetMap tiles
- **Testing:** Vitest 4, React Testing Library
- **Linting:** ESLint

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
