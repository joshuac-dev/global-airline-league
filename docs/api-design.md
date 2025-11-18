# API Design

This document describes the REST API endpoints for the Global Airline League backend.

## Base URL

```
http://localhost:8080/api
```

## Common Response Formats

### Success Response

```json
{
  "field1": "value1",
  "field2": "value2"
}
```

### Error Response

```json
{
  "error": {
    "code": "error_code",
    "message": "Human-readable error message",
    "details": {
      "field": "additional info"
    }
  }
}
```

## Status Codes

- `200 OK` - Successful GET request
- `201 Created` - Successful POST request creating a resource
- `204 No Content` - Successful DELETE request
- `400 Bad Request` - Invalid input or validation error
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists (duplicate)
- `503 Service Unavailable` - Database unavailable

---

## Airports

### GET /api/airports

List airports with pagination and optional country filter.

**Query Parameters:**
- `offset` (optional, default: 0) - Starting position (0-based)
- `limit` (optional, default: 50) - Maximum number of results
- `country` (optional) - ISO country code filter (e.g., "US", "GB")

**Response:** `200 OK`

```json
[
  {
    "id": 1,
    "name": "London Heathrow Airport",
    "iata": "LHR",
    "icao": "EGLL",
    "city": "London",
    "countryCode": "GB",
    "latitude": 51.4775,
    "longitude": -0.461389
  }
]
```

### GET /api/airports/{id}

Get a single airport by ID.

**Path Parameters:**
- `id` - Airport ID (integer)

**Response:** `200 OK`

```json
{
  "id": 1,
  "name": "London Heathrow Airport",
  "iata": "LHR",
  "icao": "EGLL",
  "city": "London",
  "countryCode": "GB",
  "latitude": 51.4775,
  "longitude": -0.461389
}
```

**Error:** `404 Not Found` if airport doesn't exist

### GET /api/airports/{id}/routes

List all routes that touch the specified airport (as origin or destination).

**Path Parameters:**
- `id` - Airport ID (integer)

**Query Parameters:**
- `offset` (optional, default: 0) - Starting position (0-based)
- `limit` (optional, default: 50, max: 100) - Maximum number of results

**Response:** `200 OK`

Returns an array of Route objects (see Routes section).

**Errors:**
- `400 Bad Request` - Invalid airport ID or pagination parameters
- `404 Not Found` - Airport doesn't exist

### GET /api/search/airports

Search airports by name, city, or IATA/ICAO code.

**Query Parameters:**
- `q` (required) - Search query string
- `limit` (optional, default: 10) - Maximum number of results

**Response:** `200 OK`

Returns an array of Airport objects (same format as list endpoint).

---

## Airlines

### GET /api/airlines

List airlines with pagination.

**Query Parameters:**
- `offset` (optional, default: 0) - Starting position (0-based)
- `limit` (optional, default: 50) - Maximum number of results

**Response:** `200 OK`

```json
[
  {
    "id": 1,
    "name": "Example Airways"
  }
]
```

### GET /api/airlines/{id}

Get a single airline by ID.

**Path Parameters:**
- `id` - Airline ID (integer)

**Response:** `200 OK`

```json
{
  "id": 1,
  "name": "Example Airways"
}
```

**Error:** `404 Not Found` if airline doesn't exist

---

## Routes

Routes represent direct connections between two airports operated by an airline.

### POST /api/routes

Create a new route.

**Request Body:**

```json
{
  "airlineId": 1,
  "originAirportId": 100,
  "destinationAirportId": 200
}
```

**Validation Rules:**
- All IDs must be positive integers
- `originAirportId` must differ from `destinationAirportId`
- Airline must exist
- Both airports must exist
- Route must be unique per airline (no duplicate origin→destination for same airline)

**Response:** `201 Created`

```json
{
  "id": 1,
  "airlineId": 1,
  "originAirportId": 100,
  "destinationAirportId": 200,
  "distanceKm": 5574,
  "createdAt": 1700000000
}
```

**Note:** `distanceKm` is automatically calculated using the great circle distance formula.

**Errors:**
- `400 Bad Request` - Invalid input (IDs not positive, origin == destination)
- `404 Not Found` - Airline or airport doesn't exist
- `409 Conflict` - Route already exists for this airline

**Example Error Response:**

```json
{
  "error": {
    "code": "duplicate_route",
    "message": "Route already exists for this airline"
  }
}
```

### GET /api/routes

List routes with optional airline filter and pagination.

**Query Parameters:**
- `airlineId` (optional) - Filter routes by airline ID
- `offset` (optional, default: 0) - Starting position (0-based)
- `limit` (optional, default: 50, max: 100) - Maximum number of results

**Response:** `200 OK`

```json
[
  {
    "id": 1,
    "airlineId": 1,
    "originAirportId": 100,
    "destinationAirportId": 200,
    "distanceKm": 5574,
    "createdAt": 1700000000
  }
]
```

**Errors:**
- `400 Bad Request` - Invalid pagination parameters (offset < 0, limit < 1 or > 100)

### GET /api/routes/{id}

Get a single route by ID.

**Path Parameters:**
- `id` - Route ID (integer)

**Response:** `200 OK`

```json
{
  "id": 1,
  "airlineId": 1,
  "originAirportId": 100,
  "destinationAirportId": 200,
  "distanceKm": 5574,
  "createdAt": 1700000000
}
```

**Errors:**
- `400 Bad Request` - Invalid route ID format
- `404 Not Found` - Route doesn't exist

### DELETE /api/routes/{id}

Delete a route.

**Path Parameters:**
- `id` - Route ID (integer)

**Response:** `204 No Content` (successful deletion, no body)

**Errors:**
- `400 Bad Request` - Invalid route ID format
- `404 Not Found` - Route doesn't exist

---

## Health Check

### GET /health

Simple health check endpoint.

**Response:** `200 OK`

```json
{
  "status": "ok"
}
```

---

## Future Endpoints (Not Yet Implemented)

The following endpoints are planned but not yet implemented:

- `POST /api/airlines` - Create airline
- `PUT/PATCH /api/routes/{id}` - Update route (pricing, capacity)
- `GET /api/routes/{id}/performance` - Route performance metrics
- Flight scheduling and simulation endpoints
- User authentication and authorization endpoints

---

## CORS Configuration

For development, the API accepts requests from any origin. In production, CORS should be configured to only allow specific frontend origins.

## Rate Limiting

Rate limiting is not currently implemented but may be added in the future for production deployments.

## Pagination Best Practices

- Always provide sensible defaults for `offset` and `limit`
- Maximum `limit` is typically capped at 100 to prevent excessive resource usage
- Use consistent pagination across all list endpoints
- Future enhancement: Consider cursor-based pagination for large datasets
