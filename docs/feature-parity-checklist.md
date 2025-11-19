# Feature Parity Checklist

This document tracks progress on achieving feature parity with the original Global Airline League game. It is seeded from the [Repository Catalogue](../repo-catalogue/INDEX.md) analysis and organized by major system.

**Status Legend:**
- ❌ Not Started
- 🚧 In Progress
- ✅ Complete

---

## Core Simulation Loop
Track the game clock and orchestrate simulation updates.

- ❌ Game clock/cycle management
- ❌ Simulation tick orchestration (order of operations)
- ❌ Side effect propagation (finances → reputation → demand)
- ❌ Event scheduling and triggering

## Domain Entities & Lifecycles

### Airlines
- ❌ Airline entity (identity, name, code, country)
- ❌ Airline financials (cash, balance sheet, loans)
- ❌ Airline reputation system
- ❌ Service quality metrics and targets
- ❌ Airline base management (headquarters, hubs)
- ❌ AI airline behavior and decision-making

### Airports
- 🚧 **Airport entity + persistence + list/search API** ([PR #XX](link-to-pr))
  - ✅ Airport entity (IATA code, name, location, size)
  - ✅ Persistence layer with Exposed + PostgreSQL
  - ✅ List API with pagination and country filter
  - ✅ Search API (ILIKE-based, FTS support in migration for future)
  - ✅ API tests with stub repository
- ❌ Airport slots and capacity management
- ❌ Runway data and aircraft compatibility
- ❌ Airport fees and pricing
- ❌ Airport features and bonuses
- ❌ Airport-to-airport distance calculations

### Aircraft & Fleet
- ❌ Aircraft model definitions (capacity, range, speed, fuel burn)
- ❌ Aircraft lifecycle (purchase, assignment, maintenance, sell)
- ❌ Fleet composition and home base assignment
- ❌ Aircraft condition and depreciation
- ❌ Configuration (seats by class)

### Routes & Links
- 🚧 **Routes – identity & CRUD** ([This PR](link-to-pr))
  - ✅ Route entity (origin, destination, distance calculation)
  - ✅ Persistence layer with Exposed + PostgreSQL
  - ✅ Unique constraint per airline (airline_id, origin, destination)
  - ✅ Distance calculation using DistanceCalculator
  - ✅ CRUD API endpoints (POST, GET, DELETE)
  - ✅ Airport-centric route listing endpoint
  - ✅ Frontend Routes page (list, create, delete)
  - ❌ Link capacity and pricing by class (TODO)
  - ❌ Route assignment to aircraft (TODO)
  - ❌ Passenger assignment and load factor (TODO)
  - ❌ Link profitability calculation (TODO)
  - ❌ Link history and performance tracking (TODO)
- ❌ Flight time calculation and scheduling
- ❌ Route optimization and network planning tools

### Passengers & Demand
- ❌ Passenger demand modeling by airport pair
- ❌ Passenger preference logic (price, frequency, quality)
- ❌ Loyalty and airline affinity
- ❌ Passenger flow simulation (booking, boarding)
- ❌ VIP passenger handling

### Finance & Economics
- ❌ Income statements (revenue, costs, profit)
- ❌ Cash flow tracking
- ❌ Loan system (borrowing, repayment, interest)
- ❌ Bank relationships
- ❌ Maintenance costs (aircraft, base)
- ❌ Fuel costs tied to oil prices
- ❌ Staff/crew costs

### Reputation & Service Quality
- ❌ Reputation system (per-country and global)
- ❌ Service quality metrics (on-time, comfort, entertainment)
- ❌ Quality target setting and adjustments
- ❌ Reputation breakdowns and detailed attribution

### Alliances
- ❌ Alliance creation and management
- ❌ Alliance members and roles
- ❌ Alliance missions and rewards
- ❌ Shared benefits (codeshare, loyalty, reputation)

### Events & Dynamic Systems
- ❌ World events (disasters, pandemics, economic shifts)
- ❌ Oil price fluctuations
- ❌ Loan offers and negotiations
- ❌ Random events affecting demand or costs

### Missions & Progression
- ❌ Tutorial system for new players
- ❌ Mission definitions and tracking
- ❌ Progression milestones (airline level, achievements)

### Rankings & Leaderboards
- ❌ Airline rankings (by size, profit, reputation, etc.)
- ❌ Historical ranking snapshots
- ❌ Leaderboard UI integration

## Data Management

### Initial Data Import
- 🚧 **Airport data import/seed** ([This PR](link-to-pr))
  - ✅ CSV importer with OurAirports format support
  - ✅ Batch insert with configurable batch size
  - ✅ Field mapping and validation (IATA/ICAO, coordinates, elevation conversion)
  - ✅ Skip-if-non-empty and optional truncate modes
  - ✅ Progress logging and error handling
  - ✅ Gradle task (:backend:jobs:importAirports)
  - ✅ Documentation and sample data
- ❌ Runway data import
- ❌ City/region data import
- ❌ Weather/climate data import (if retained)
- ❌ Aircraft model seed data

### Persistence & Schema
- ❌ Define Exposed table schemas for all entities
- ❌ Repository pattern for data access
- ❌ Transaction management
- ✅ Indexes for performance (airport IATA/ICAO/country lookups, GIN index for FTS)

### Migrations
- 🚧 Convert legacy SQL scripts to Flyway migrations
- ✅ Initial schema creation (V1__create_airports.sql)
- ✅ Incremental migrations infrastructure (Flyway configured)
- 🚧 Data seeding scripts (docs/dev/seed_airports.sql for testing)

### Search
- ✅ Full-Text Search (FTS) setup in PostgreSQL (migration ready, ILIKE fallback implemented)
- ✅ Airport search by name, city, IATA code
- ❌ Airline search by name or code
- ✅ Search API endpoints (/api/search/airports)

## Analytics & History

### Financial History
- ❌ Income history snapshots (per cycle)
- ❌ Cash flow analysis
- ❌ Expense breakdown tracking

### Operational History
- ❌ Link consumption history (passengers carried per link per cycle)
- ❌ Link changes (additions, removals, pricing adjustments)
- ❌ Aircraft utilization history

### Performance Metrics
- ❌ Dashboard metrics (total passengers, revenue, profit)
- ❌ KPI calculations and aggregations
- ❌ Historical trend charting data

## Real-Time Updates & Communication

### WebSocket Infrastructure
- ❌ WebSocket connection management (Ktor)
- ❌ Client authentication for WebSocket channels
- ❌ Message serialization and protocols

### World Updates (`/ws/world`)
- ❌ Simulation tick notifications
- ❌ Oil price updates
- ❌ Global event broadcasts
- ❌ World chat messages

### Airline-Specific Updates (`/ws/airline/{id}`)
- ❌ Airline financial updates
- ❌ Route performance changes
- ❌ Notifications (maintenance due, loan offers, etc.)
- ❌ Direct messages

### Chat System
- ❌ Global chat channel
- ❌ Alliance chat channels
- ❌ Private messaging
- ❌ Chat history and persistence

## Maps & Visualizations

### OpenStreetMap Integration
- 🚧 **Frontend SPA bootstrap & OSM airport markers** ([This PR](link-to-pr))
  - ✅ Frontend map component (React Leaflet)
  - ✅ OSM tile loading (public tiles with attribution)
  - ✅ Airport markers with popups
  - ✅ Interactive controls (zoom, pan, search)
  - ❌ Route polylines (flight paths)
- ❌ Self-hosted OSM tile server (future performance optimization)

### Data Visualizations
- ❌ Heatmaps (demand, coverage)
- ❌ Route network diagrams
- ❌ Financial charts (revenue, profit over time)
- ❌ Leaderboard tables

## API Endpoints

### REST API
- ❌ `/api/airlines` - CRUD operations (GET list, GET by id implemented; POST pending)
- ✅ `/api/airports` - List, search, details (GET list, GET by id implemented)
- ✅ `/api/routes` - Link management (CRUD fully implemented)
- ✅ `/api/airports/{id}/routes` - Airport route listing
- ❌ `/api/aircraft` - Fleet management
- ✅ `/api/search/airports` - Full-text search for airports
- ❌ `/api/alliances` - Alliance operations
- ❌ `/api/rankings` - Leaderboards
- ✅ `/health` - Health check (implemented in initial PR)

### WebSocket API
- ❌ `/ws/world` - Global updates
- ❌ `/ws/airline/{id}` - Airline-specific updates

## Authentication & Authorization

### Account System (if retained)
- ❌ User registration and login
- ❌ Session management
- ❌ Password reset flows
- ❌ Email verification

### OAuth/Social Login (optional)
- ❌ Google OAuth integration
- ❌ GitHub OAuth integration

### Role-Based Access Control
- ❌ Player role (own airline only)
- ❌ Admin role (global access)
- ❌ API endpoint authorization

## Email & Notifications

### Email System (modernized)
- ❌ Email provider integration (SendGrid, AWS SES, etc.)
- ❌ Transactional emails (registration, password reset)
- ❌ Notification emails (game events, maintenance alerts)

### In-App Notifications
- ❌ Notification queue
- ❌ Real-time push via WebSocket
- ❌ Notification history

## Admin Tools & Maintenance

### Admin Dashboard
- ❌ System health monitoring
- ❌ User management (ban, reset, impersonate)
- ❌ Database stats and queries
- ❌ Simulation control (pause, skip ticks)

### Patch System
- ❌ Apply data patches (bulk updates)
- ❌ Schema migration tools
- ❌ Data cleanup utilities

### Diagnostics
- ❌ Logging and error tracking
- ❌ Performance profiling
- ❌ Database query analysis

## Frontend Application

### SPA Scaffolding
- 🚧 **Frontend SPA bootstrap** ([This PR](link-to-pr))
  - ✅ React + TypeScript project setup (Vite)
  - ✅ Routing (single page view; React Router deferred)
  - ✅ State management (React hooks; global state deferred)
  - ✅ API client (fetch with TypeScript types)
  - ✅ Testing infrastructure (Vitest + React Testing Library)
  - ✅ Build and dev scripts
  - ✅ ESLint + TypeScript strict mode configuration

### Core Views
- 🚧 **Map view** ([Initial PR](link-to-pr))
  - ✅ OSM map with airport markers
  - ✅ Airport search and fly-to functionality
  - ❌ Route visualization
  - ❌ Heatmaps
- 🚧 **Routes management** ([This PR](link-to-pr))
  - ✅ List routes for an airline
  - ✅ Create new routes with airport selection
  - ✅ Delete routes
  - ✅ Display computed distance
  - ❌ Route pricing and capacity configuration
  - ❌ Route visualization on map
- ❌ Dashboard / Home
- ❌ Airline management
- ❌ Fleet management
- ❌ Finance view
- ❌ Rankings / Leaderboards

### UI Components
- ❌ Component library (or custom components)
- ❌ Forms and validation
- ❌ Data tables with sorting/filtering
- ❌ Charts and graphs

## Developer Experience

### Local Development
- ✅ Gradle build system (implemented in initial PR)
- ✅ Multi-module project structure (implemented in initial PR)
- ❌ Docker Compose for local PostgreSQL
- ❌ Hot reload for API (ktor auto-reload)
- ❌ Frontend dev server with HMR

### Testing
- 🚧 **Frontend tests** ([This PR](link-to-pr))
  - ✅ Unit test suite for API client
  - ✅ Component tests with React Testing Library
  - ✅ Vitest configuration and setup
  - ❌ E2E tests (Playwright or Cypress)
- ❌ Unit test suite for core logic (backend)
- ❌ Integration tests for persistence (backend)
- ❌ API tests with Ktor test utilities (backend)
- ❌ Testcontainers for database tests (backend)

### Linting & Code Quality
- ❌ Kotlin linting (detekt or ktlint)
- 🚧 **Frontend linting** ([This PR](link-to-pr))
  - ✅ TypeScript linting (ESLint with TypeScript plugin)
  - ✅ TypeScript strict mode
  - ✅ EditorConfig for consistent formatting
- ❌ Code formatting (Prettier for frontend, ktfmt for backend)
- ❌ Pre-commit hooks

### CI/CD
- ❌ GitHub Actions workflow (build, test, lint)
- ❌ Automated dependency updates (Renovate or Dependabot)
- ❌ Docker image builds
- ❌ Deployment pipeline

---

## Notes

### Guidance for Parity Work
- The [Repository Catalogue Index](../repo-catalogue/INDEX.md) provides detailed documentation of the original game's implementation.
- Each catalogue file describes entities, logic, dependencies, and test coverage from the original Scala codebase.
- Use these catalogues to understand **what** needs to be preserved while **refactoring how** it's implemented.

### Scope Management
- This checklist will evolve as we discover additional features or decide to drop/defer certain systems.
- Mark items ✅ when they are fully implemented, tested, and merged to main.
- Break large items into sub-tasks in their own PRs as needed.

### Out of Scope (Explicitly Deferred or Dropped)
- Google Maps integration (prohibited; use OSM only)
- Legacy monolithic architecture (replaced by modular monolith)
- Scala/Play framework (replaced by Kotlin/Ktor)
- Any proprietary or third-party features not essential to core gameplay

---

**Last Updated:** 2025-11-16
