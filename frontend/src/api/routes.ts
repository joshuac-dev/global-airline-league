import type { Route, RouteCreateRequest, RouteErrorResponse } from '../types/route';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * Fetch routes with optional filtering and pagination.
 */
export async function fetchRoutes(params?: {
  airlineId?: number;
  offset?: number;
  limit?: number;
}): Promise<Route[]> {
  const queryParams = new URLSearchParams();
  
  if (params?.airlineId !== undefined) {
    queryParams.append('airlineId', params.airlineId.toString());
  }
  if (params?.offset !== undefined) {
    queryParams.append('offset', params.offset.toString());
  }
  if (params?.limit !== undefined) {
    queryParams.append('limit', params.limit.toString());
  }

  const url = `${API_BASE}/api/routes${queryParams.toString() ? `?${queryParams}` : ''}`;
  const response = await fetch(url);

  if (!response.ok) {
    const error: RouteErrorResponse = await response.json();
    throw new Error(error.error.message || 'Failed to fetch routes');
  }

  return response.json();
}

/**
 * Fetch a single route by ID.
 */
export async function fetchRoute(id: number): Promise<Route> {
  const response = await fetch(`${API_BASE}/api/routes/${id}`);

  if (!response.ok) {
    const error: RouteErrorResponse = await response.json();
    throw new Error(error.error.message || 'Failed to fetch route');
  }

  return response.json();
}

/**
 * Create a new route.
 */
export async function createRoute(request: RouteCreateRequest): Promise<Route> {
  const response = await fetch(`${API_BASE}/api/routes`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const error: RouteErrorResponse = await response.json();
    throw new Error(error.error.message || 'Failed to create route');
  }

  return response.json();
}

/**
 * Delete a route by ID.
 */
export async function deleteRoute(id: number): Promise<void> {
  const response = await fetch(`${API_BASE}/api/routes/${id}`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    const error: RouteErrorResponse = await response.json();
    throw new Error(error.error.message || 'Failed to delete route');
  }
}

/**
 * Fetch routes for a specific airport (as origin or destination).
 */
export async function fetchAirportRoutes(
  airportId: number,
  params?: { offset?: number; limit?: number }
): Promise<Route[]> {
  const queryParams = new URLSearchParams();
  
  if (params?.offset !== undefined) {
    queryParams.append('offset', params.offset.toString());
  }
  if (params?.limit !== undefined) {
    queryParams.append('limit', params.limit.toString());
  }

  const url = `${API_BASE}/api/airports/${airportId}/routes${queryParams.toString() ? `?${queryParams}` : ''}`;
  const response = await fetch(url);

  if (!response.ok) {
    const error: RouteErrorResponse = await response.json();
    throw new Error(error.error.message || 'Failed to fetch airport routes');
  }

  return response.json();
}
