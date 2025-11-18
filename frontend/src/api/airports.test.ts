import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchAirports, searchAirports } from '../api/airports';
import type { Airport } from '../types/airport';

describe('Airport API Client', () => {
  beforeEach(() => {
    // Reset fetch mock before each test
    vi.resetAllMocks();
  });

  describe('fetchAirports', () => {
    it('should fetch and parse airports list', async () => {
      const mockAirports: Airport[] = [
        {
          id: 1,
          name: 'London Heathrow Airport',
          iata: 'LHR',
          icao: 'EGLL',
          city: 'London',
          countryCode: 'GB',
          latitude: 51.4775,
          longitude: -0.461389,
        },
        {
          id: 2,
          name: 'John F Kennedy International Airport',
          iata: 'JFK',
          icao: 'KJFK',
          city: 'New York',
          countryCode: 'US',
          latitude: 40.639751,
          longitude: -73.778925,
        },
      ];

      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => mockAirports,
      }) as any;

      const result = await fetchAirports({ offset: 0, limit: 50 });

      expect(result).toEqual(mockAirports);
      expect(globalThis.fetch).toHaveBeenCalledWith(
        '/api/airports?offset=0&limit=50',
        expect.objectContaining({ signal: undefined })
      );
    });

    it('should include country filter in query params', async () => {
      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => [],
      }) as any;

      await fetchAirports({ offset: 0, limit: 50, country: 'US' });

      expect(globalThis.fetch).toHaveBeenCalledWith(
        '/api/airports?offset=0&limit=50&country=US',
        expect.any(Object)
      );
    });

    it('should throw error on failed request', async () => {
      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: false,
        statusText: 'Internal Server Error',
      }) as any;

      await expect(fetchAirports()).rejects.toThrow(
        'Failed to fetch airports: Internal Server Error'
      );
    });
  });

  describe('searchAirports', () => {
    it('should search airports and parse results', async () => {
      const mockResults: Airport[] = [
        {
          id: 1,
          name: 'London Heathrow Airport',
          iata: 'LHR',
          icao: 'EGLL',
          city: 'London',
          countryCode: 'GB',
          latitude: 51.4775,
          longitude: -0.461389,
        },
      ];

      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => mockResults,
      }) as any;

      const result = await searchAirports({ q: 'heathrow' });

      expect(result).toEqual(mockResults);
      expect(globalThis.fetch).toHaveBeenCalledWith(
        '/api/search/airports?q=heathrow&limit=10',
        expect.objectContaining({ signal: undefined })
      );
    });

    it('should support custom limit', async () => {
      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => [],
      }) as any;

      await searchAirports({ q: 'test', limit: 20 });

      expect(globalThis.fetch).toHaveBeenCalledWith(
        '/api/search/airports?q=test&limit=20',
        expect.any(Object)
      );
    });

    it('should throw error on failed search', async () => {
      globalThis.fetch = vi.fn().mockResolvedValue({
        ok: false,
        statusText: 'Bad Request',
      }) as any;

      await expect(searchAirports({ q: 'test' })).rejects.toThrow(
        'Failed to search airports: Bad Request'
      );
    });
  });
});
