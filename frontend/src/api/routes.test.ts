import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchRoutes, createRoute, deleteRoute, fetchRoute } from './routes';
import type { Route, RouteCreateRequest } from '../types/route';

describe('Routes API Client', () => {
  beforeEach(() => {
    // Reset fetch mock before each test
    vi.resetAllMocks();
  });

  describe('fetchRoutes', () => {
    it('should fetch routes list without filter', async () => {
      const mockRoutes: Route[] = [
        {
          id: 1,
          airlineId: 1,
          originAirportId: 100,
          destinationAirportId: 200,
          distanceKm: 5000,
          createdAt: 1700000000,
        },
      ];

      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => mockRoutes,
      }) as any;

      const result = await fetchRoutes();
      expect(result).toEqual(mockRoutes);
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/routes')
      );
    });

    it('should fetch routes filtered by airline', async () => {
      const mockRoutes: Route[] = [
        {
          id: 1,
          airlineId: 1,
          originAirportId: 100,
          destinationAirportId: 200,
          distanceKm: 5000,
          createdAt: 1700000000,
        },
      ];

      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => mockRoutes,
      }) as any;

      const result = await fetchRoutes({ airlineId: 1 });
      expect(result).toEqual(mockRoutes);
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('airlineId=1')
      );
    });

    it('should handle fetch error', async () => {
      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: false,
        json: async () => ({ error: { code: 'error', message: 'Test error' } }),
      }) as any;

      await expect(fetchRoutes()).rejects.toThrow('Test error');
    });
  });

  describe('fetchRoute', () => {
    it('should fetch a single route by ID', async () => {
      const mockRoute: Route = {
        id: 1,
        airlineId: 1,
        originAirportId: 100,
        destinationAirportId: 200,
        distanceKm: 5000,
        createdAt: 1700000000,
      };

      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => mockRoute,
      }) as any;

      const result = await fetchRoute(1);
      expect(result).toEqual(mockRoute);
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/routes/1')
      );
    });

    it('should handle not found error', async () => {
      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: false,
        json: async () => ({ error: { code: 'not_found', message: 'Route not found' } }),
      }) as any;

      await expect(fetchRoute(999)).rejects.toThrow('Route not found');
    });
  });

  describe('createRoute', () => {
    it('should create a new route', async () => {
      const request: RouteCreateRequest = {
        airlineId: 1,
        originAirportId: 100,
        destinationAirportId: 200,
      };

      const mockCreatedRoute: Route = {
        id: 1,
        airlineId: 1,
        originAirportId: 100,
        destinationAirportId: 200,
        distanceKm: 5000,
        createdAt: 1700000000,
      };

      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => mockCreatedRoute,
      }) as any;

      const result = await createRoute(request);
      expect(result).toEqual(mockCreatedRoute);
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/routes'),
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(request),
        })
      );
    });

    it('should handle duplicate route error', async () => {
      const request: RouteCreateRequest = {
        airlineId: 1,
        originAirportId: 100,
        destinationAirportId: 200,
      };

      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: false,
        json: async () => ({
          error: { code: 'duplicate_route', message: 'Route already exists for this airline' },
        }),
      }) as any;

      await expect(createRoute(request)).rejects.toThrow('Route already exists');
    });

    it('should handle validation error for same origin and destination', async () => {
      const request: RouteCreateRequest = {
        airlineId: 1,
        originAirportId: 100,
        destinationAirportId: 100,
      };

      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: false,
        json: async () => ({
          error: {
            code: 'invalid_input',
            message: 'Origin and destination airports must be different',
          },
        }),
      }) as any;

      await expect(createRoute(request)).rejects.toThrow('different');
    });
  });

  describe('deleteRoute', () => {
    it('should delete a route successfully', async () => {
      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({}),
      }) as any;

      await deleteRoute(1);
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/routes/1'),
        expect.objectContaining({ method: 'DELETE' })
      );
    });

    it('should handle not found error on delete', async () => {
      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: false,
        json: async () => ({ error: { code: 'not_found', message: 'Route not found' } }),
      }) as any;

      await expect(deleteRoute(999)).rejects.toThrow('Route not found');
    });
  });
});
