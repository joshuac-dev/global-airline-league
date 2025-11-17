/**
 * Core airport entity from the backend API.
 */
export interface Airport {
  id: number;
  name: string;
  iata: string | null;
  icao: string | null;
  city: string;
  countryCode: string;
  latitude: number;
  longitude: number;
}

/**
 * Paginated airport list response.
 */
export interface AirportListParams {
  offset?: number;
  limit?: number;
  country?: string;
}

/**
 * Airport search parameters.
 */
export interface AirportSearchParams {
  q: string;
  limit?: number;
}
