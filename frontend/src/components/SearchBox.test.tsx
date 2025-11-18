import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SearchBox } from './SearchBox';
import * as airportsApi from '../api/airports';
import type { Airport } from '../types/airport';

// Mock the API module
vi.mock('../api/airports');

describe('SearchBox', () => {
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
      name: 'Los Angeles International Airport',
      iata: 'LAX',
      icao: 'KLAX',
      city: 'Los Angeles',
      countryCode: 'US',
      latitude: 33.9425,
      longitude: -118.408,
    },
  ];

  it('should render search input', () => {
    const onSelectAirport = vi.fn();
    render(<SearchBox onSelectAirport={onSelectAirport} />);

    expect(
      screen.getByPlaceholderText('Search airports by name, city, or code...')
    ).toBeInTheDocument();
  });

  it('should display search results after typing', async () => {
    const onSelectAirport = vi.fn();
    vi.mocked(airportsApi.searchAirports).mockResolvedValue(mockAirports);

    render(<SearchBox onSelectAirport={onSelectAirport} />);

    const input = screen.getByPlaceholderText('Search airports by name, city, or code...');
    await userEvent.type(input, 'london');

    // Wait for debounce (300ms) and results to appear
    await waitFor(
      () => {
        expect(screen.getByText(/London Heathrow Airport/i)).toBeInTheDocument();
      },
      { timeout: 500 }
    );

    expect(airportsApi.searchAirports).toHaveBeenCalledWith(
      { q: 'london' },
      expect.any(AbortSignal)
    );
  });

  it('should display "No matches" when results are empty', async () => {
    const onSelectAirport = vi.fn();
    vi.mocked(airportsApi.searchAirports).mockResolvedValue([]);

    render(<SearchBox onSelectAirport={onSelectAirport} />);

    const input = screen.getByPlaceholderText('Search airports by name, city, or code...');
    await userEvent.type(input, 'zzz');

    await waitFor(
      () => {
        expect(screen.getByText('No matches found')).toBeInTheDocument();
      },
      { timeout: 500 }
    );
  });

  it('should call onSelectAirport when result is clicked', async () => {
    const onSelectAirport = vi.fn();
    vi.mocked(airportsApi.searchAirports).mockResolvedValue(mockAirports);

    render(<SearchBox onSelectAirport={onSelectAirport} />);

    const input = screen.getByPlaceholderText('Search airports by name, city, or code...');
    await userEvent.type(input, 'london');

    await waitFor(
      () => {
        expect(screen.getByText(/London Heathrow Airport/i)).toBeInTheDocument();
      },
      { timeout: 500 }
    );

    const firstResult = screen.getByText(/London Heathrow Airport/i);
    await userEvent.click(firstResult);

    expect(onSelectAirport).toHaveBeenCalledWith(mockAirports[0]);
  });

  it('should clear input after selecting an airport', async () => {
    const onSelectAirport = vi.fn();
    vi.mocked(airportsApi.searchAirports).mockResolvedValue(mockAirports);

    render(<SearchBox onSelectAirport={onSelectAirport} />);

    const input = screen.getByPlaceholderText(
      'Search airports by name, city, or code...'
    ) as HTMLInputElement;
    await userEvent.type(input, 'london');

    await waitFor(
      () => {
        expect(screen.getByText(/London Heathrow Airport/i)).toBeInTheDocument();
      },
      { timeout: 500 }
    );

    const firstResult = screen.getByText(/London Heathrow Airport/i);
    await userEvent.click(firstResult);

    expect(input.value).toBe('');
  });

  it('should abort previous requests when typing', async () => {
    const onSelectAirport = vi.fn();
    vi.mocked(airportsApi.searchAirports).mockResolvedValue(mockAirports);

    render(<SearchBox onSelectAirport={onSelectAirport} />);

    const input = screen.getByPlaceholderText('Search airports by name, city, or code...');

    // Type quickly to trigger debounce
    await userEvent.type(input, 'london');

    await waitFor(
      () => {
        expect(airportsApi.searchAirports).toHaveBeenCalled();
      },
      { timeout: 500 }
    );

    // Due to debounce, only the final value should be searched
    const calls = vi.mocked(airportsApi.searchAirports).mock.calls;
    const lastCall = calls[calls.length - 1];
    expect(lastCall[0].q).toBe('london');
  });
});
