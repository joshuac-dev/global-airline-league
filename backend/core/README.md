# Backend Core Module

## Overview

The `backend/core` module contains the pure domain logic and business rules for Global Airline League. This module is **framework-free** and has no dependencies on HTTP, database, or UI frameworks.

## Principles

- **Pure Domain Logic:** Business rules, calculations, and simulations
- **Framework Independence:** No Ktor, Exposed, or other infrastructure dependencies
- **Testability:** Easy to unit test without external dependencies
- **Value Objects:** Leverage Kotlin's value classes for type-safe IDs

## Structure

```
com.gal.core/
├── domain/          # Domain entities and value objects
├── service/         # Domain services and business logic
└── simulation/      # Simulation engines and algorithms
```

## Current Contents

### Value Classes
- `GameClock` - Represents the current game cycle/tick
- `AirlineId` - Type-safe airline identifier
- `AirportId` - Type-safe airport identifier

## Guidelines for Contributors

1. **No Framework Dependencies:** Do not add Ktor, Exposed, or other infrastructure libraries
2. **Immutability Preferred:** Use `val` and immutable data structures where possible
3. **Pure Functions:** Prefer pure functions that don't rely on external state
4. **Comprehensive Tests:** Aim for high test coverage on business logic
5. **Domain-Driven Design:** Model the business domain accurately

## Future Additions

As we implement game features, this module will grow to include:
- Airline entities and services
- Airport and route calculations
- Passenger demand modeling
- Financial calculations (profit, costs, loans)
- Reputation algorithms
- Event handling and random events
- Mission and progression logic
