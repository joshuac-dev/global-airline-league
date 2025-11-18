/**
 * Route entity representing a connection between two airports operated by an airline.
 */
export interface Route {
  id: number;
  airlineId: number;
  originAirportId: number;
  destinationAirportId: number;
  distanceKm: number;
  createdAt: number; // Unix timestamp in seconds
}

/**
 * Request DTO for creating a new route.
 */
export interface RouteCreateRequest {
  airlineId: number;
  originAirportId: number;
  destinationAirportId: number;
}

/**
 * Error response from the API.
 */
export interface RouteErrorResponse {
  error: {
    code: string;
    message: string;
    details?: Record<string, string>;
  };
}
