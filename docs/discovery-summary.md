# Discovery Summary

## Overview
This document summarizes the analysis of the existing Global Airline League codebase (Scala/Play Framework) to inform the complete rewrite in Kotlin + Ktor + React + TypeScript.

## Original Technology Stack
- **Backend:** Scala + Play Framework + Akka
- **Database:** MySQL with custom data access layer
- **Frontend:** Server-side rendering with Scala templates + jQuery
- **Real-time:** Akka Actors + WebSockets

## Key Findings from repo-catalogue

### Domain Model (from airline-data model package)
The original system implements a comprehensive airline simulation with the following major entities:

1. **Airlines:** Player/AI entities with reputation, finances, service quality metrics
2. **Airports:** Geographic locations with capacity, population, income levels
3. **Aircraft:** Fleet management with various airplane models
4. **Routes/Links:** Flight connections with pricing, frequency, capacity
5. **Demand Model:** Complex passenger demand calculation based on distance, population, income
6. **Pricing:** Dynamic pricing with discounts, campaigns
7. **Alliances:** Multi-airline cooperation with shared benefits
8. **Finance:** Loans, cash flow, maintenance costs, fuel contracts
9. **Events:** Random events affecting gameplay (oil price changes, disasters)
10. **Progression:** Airline leveling, base upgrades, reputation system

### Technical Debt Identified
1. **Monolithic Model Files:** Large Scala files (e.g., Model.scala, AirportAsset.scala) mixing concerns
2. **Implicit Coupling:** Business logic tightly coupled to persistence layer
3. **Complex State Management:** Mutable state in Akka actors
4. **Limited Test Coverage:** Many critical calculations lack unit tests
5. **Mixed Responsibilities:** HTTP controllers doing business logic

### Architecture Observations
- **Three-tier:** airline-data (model + persistence) → airline-rest (not fully implemented) → airline-web (controllers + views)
- **Database-Centric:** Heavy reliance on raw SQL queries
- **Simulation Loop:** Background actors running game tick cycles

## Rewrite Strategy

### Goals
1. **Separation of Concerns:** Clean layered architecture with distinct boundaries
2. **Testability:** Pure domain logic, dependency injection, comprehensive tests
3. **Modularity:** Multi-module Gradle project for clear boundaries
4. **Modern Stack:** Kotlin + Ktor for backend, React + TypeScript for frontend
5. **Performance:** Efficient WebSocket updates, database connection pooling
6. **Maintainability:** Clear code structure, comprehensive documentation

### Technology Decisions
- **Backend Framework:** Ktor 2.x (lightweight, Kotlin-native)
- **ORM:** Exposed (Kotlin-friendly, type-safe)
- **Database:** PostgreSQL 16 with full-text search
- **Migrations:** Flyway
- **Frontend:** React 18 + TypeScript + Vite
- **Mapping:** OpenStreetMap + React Leaflet (NO Google Maps)
- **State Management:** React hooks (local state for now)
- **Real-time:** Ktor WebSockets with coroutines

### Architecture Principles
1. **Modular Monolith:** Separate modules with clear dependencies
2. **Domain-Driven:** Core domain logic independent of infrastructure
3. **CQRS-lite:** Separate read models where beneficial
4. **Event-Driven:** Simulation tick events broadcast to clients
5. **Hexagonal:** Domain isolated from external concerns

## Next Steps
1. Complete scaffolding (this PR)
2. Implement core simulation engine
3. Build out REST API endpoints
4. Create comprehensive frontend UI
5. Migrate/seed initial data
6. Performance testing and optimization
