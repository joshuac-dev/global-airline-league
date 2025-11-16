# Domain Overview

## Major Entities and Subsystems

### 1. Airlines
**Description:** Player or AI-controlled airline companies.

**Key Attributes:**
- Name, reputation, cash balance
- Service quality targets
- Headquarters location
- Maintenance quality level

**Business Rules:**
- Reputation affects passenger preference
- Cash balance cannot go negative (with loan limits)
- Service quality affects customer satisfaction

### 2. Airports
**Description:** Geographic locations where airlines operate.

**Key Attributes:**
- IATA/ICAO codes
- Location (lat/lon)
- Country, city, timezone
- Size/capacity
- Population, income level
- Runway length, features

**Business Rules:**
- Runway length determines compatible aircraft
- Population and income affect demand
- Slot capacity limits operations

### 3. Routes (Links)
**Description:** Flight connections between airports.

**Key Attributes:**
- From/to airports
- Frequency, capacity (by class)
- Price (economy, business, first)
- Aircraft assignment
- Flight duration

**Business Rules:**
- Distance determines fuel cost and flight time
- Demand varies by route characteristics
- Pricing affects load factor

### 4. Aircraft (Fleet)
**Description:** Individual airplanes owned by airlines.

**Key Attributes:**
- Model (e.g., Boeing 737, Airbus A320)
- Age, condition
- Seating configuration
- Home base
- Maintenance schedule

**Business Rules:**
- Age degrades condition
- Condition affects reliability and maintenance cost
- Configuration is customizable

### 5. Airplane Models
**Description:** Aircraft types available for purchase.

**Key Attributes:**
- Manufacturer, family, name
- Capacity (seats)
- Range, speed, fuel efficiency
- Price, running cost

**Business Rules:**
- Range limits route distance
- Fuel efficiency affects profitability
- Different models suit different route types

### 6. Demand
**Description:** Passenger demand for routes.

**Factors:**
- Distance between airports
- Population and wealth
- Airport features (hub bonus)
- Time of year (seasonality)
- Competition
- Airline reputation

**Calculation:** Complex formula considering all factors.

### 7. Pricing
**Description:** Ticket pricing and revenue management.

**Mechanisms:**
- Base price calculation
- Dynamic pricing based on load factor
- Discounts and campaigns
- Class-based pricing (economy, business, first)

**Business Rules:**
- Higher prices reduce demand
- Lower prices increase load factor but reduce revenue
- Optimal pricing balances load and profit

### 8. Finance
**Description:** Airline financial management.

**Components:**
- Cash flow (revenue, expenses)
- Loans (with interest)
- Maintenance costs
- Fuel contracts
- Airport fees
- Staff salaries

**Business Rules:**
- Positive cash flow required for expansion
- Interest payments on loans
- Maintenance quality affects costs

### 9. Alliances
**Description:** Cooperation between airlines.

**Features:**
- Shared flights (code-sharing)
- Reputation bonuses
- Joint marketing
- Alliance missions

**Business Rules:**
- Members share benefits
- Missions provide rewards
- Reputation affects all members

### 10. Events
**Description:** Random or scheduled events affecting gameplay.

**Types:**
- Economic events (recessions, booms)
- Natural disasters (affecting specific airports)
- Oil price fluctuations
- Regulatory changes
- Seasonal events (holidays)

**Impact:** Affects demand, costs, or operations.

### 11. Assets (Airport Infrastructure)
**Description:** Airline investments at airports.

**Types:**
- Bases (hubs)
- Lounges
- Gates
- Maintenance facilities

**Business Rules:**
- Investments provide operational benefits
- Upgrades require capital
- Depreciation over time

### 12. Progression
**Description:** Airline leveling and unlocks.

**Mechanics:**
- Experience from flights
- Level-based unlocks (routes, aircraft)
- Base upgrades
- Reputation milestones

### 13. Search
**Description:** Full-text search for airports and routes.

**Features:**
- Search by airport name, code, country
- Route search
- Fast autocomplete

**Implementation:** PostgreSQL FTS with GIN indexes.

### 14. Real-time Updates
**Description:** Live game state synchronization.

**Mechanism:**
- WebSocket connections
- Tick-based updates
- Broadcast world state
- Per-airline updates

### 15. Maps
**Description:** Geographic visualization.

**Features:**
- World map showing airports
- Route visualization
- Airline network display
- Demand heatmaps (future)

**Implementation:** React Leaflet + OpenStreetMap (NO Google Maps).

## Subsystem Dependencies

```
┌─────────────┐
│   Airlines  │
└──────┬──────┘
       │ owns
       ▼
┌─────────────┐      ┌──────────┐
│  Aircraft   │─────▶│  Models  │
└──────┬──────┘      └──────────┘
       │ flies on
       ▼
┌─────────────┐      ┌──────────┐
│   Routes    │─────▶│ Airports │
└──────┬──────┘      └──────────┘
       │
       ▼
┌─────────────┐
│   Demand    │
└─────────────┘
       │
       ▼
┌─────────────┐
│   Pricing   │
└─────────────┘
       │
       ▼
┌─────────────┐
│   Finance   │
└─────────────┘
```

## Data Relationships

- **Airlines** have many **Aircraft**
- **Airlines** operate many **Routes**
- **Routes** connect two **Airports**
- **Routes** use **Aircraft** of specific **Models**
- **Airlines** have **Assets** at **Airports**
- **Airlines** belong to **Alliances**
- **Events** affect **Airlines** and **Airports**
- **Demand** calculated for each **Route**
- **Pricing** determines **Revenue** for **Airlines**

## Simulation Cycle

1. **Tick Event** triggered by background job
2. **Demand Calculation** for all routes
3. **Passenger Allocation** based on pricing and reputation
4. **Revenue Calculation** from ticket sales
5. **Cost Deduction** (fuel, maintenance, fees)
6. **Financial Update** (cash flow, loan interest)
7. **Aircraft Aging** (condition degradation)
8. **Event Processing** (random events)
9. **State Broadcast** to connected clients via WebSocket

## Future Extensions

- **Cargo routes:** Freight operations
- **Charters:** One-off flights
- **Codeshare agreements:** Non-alliance partnerships
- **Dynamic weather:** Affects operations
- **Staff management:** Pilots, crew hiring
- **Marketing campaigns:** Targeted promotions
- **Mergers & acquisitions:** Buying other airlines
