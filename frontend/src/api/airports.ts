import type { Airport, AirportListParams, AirportSearchParams } from '../types/airport';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

/**
 * Fetch a paginated list of airports.
 */
export async function fetchAirports(
  params: AirportListParams = {},
  signal?: AbortSignal
): Promise<Airport[]> {
  const { offset = 0, limit = 200, country } = params;
  const queryParams = new URLSearchParams({
    offset: offset.toString(),
    limit: limit.toString(),
  });

  if (country) {
    queryParams.set('country', country);
  }

  const response = await fetch(`${API_BASE_URL}/api/airports?${queryParams}`, {
    signal,
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch airports: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Search airports by query string.
 */
export async function searchAirports(
  params: AirportSearchParams,
  signal?: AbortSignal
): Promise<Airport[]> {
  const { q, limit = 10 } = params;
  const queryParams = new URLSearchParams({
    q,
    limit: limit.toString(),
  });

  const response = await fetch(`${API_BASE_URL}/api/search/airports?${queryParams}`, {
    signal,
  });

  if (!response.ok) {
    throw new Error(`Failed to search airports: ${response.statusText}`);
  }

  return response.json();
}
