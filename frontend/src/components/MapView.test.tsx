import { describe, it, expect, vi } from 'vitest';
import { render } from '@testing-library/react';
import { MapView } from './MapView';
import type { Airport } from '../types/airport';

// Mock react-leaflet components
vi.mock('react-leaflet', () => ({
  MapContainer: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="map-container">{children}</div>
  ),
  TileLayer: () => <div data-testid="tile-layer" />,
  Marker: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="marker">{children}</div>
  ),
  Popup: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="popup">{children}</div>
  ),
  useMap: () => ({
    flyTo: vi.fn(),
  }),
}));

describe('MapView', () => {
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

  it('should render without crashing', () => {
    const { container } = render(
      <MapView
        airports={[]}
        selectedAirportId={null}
        onMarkerClick={vi.fn()}
      />
    );

    expect(container.querySelector('[data-testid="map-container"]')).toBeInTheDocument();
  });

  it('should render markers for provided airports', () => {
    const { getAllByTestId } = render(
      <MapView
        airports={mockAirports}
        selectedAirportId={null}
        onMarkerClick={vi.fn()}
      />
    );

    const markers = getAllByTestId('marker');
    expect(markers).toHaveLength(mockAirports.length);
  });

  it('should display airport details in popups', () => {
    const { getByText } = render(
      <MapView
        airports={mockAirports}
        selectedAirportId={null}
        onMarkerClick={vi.fn()}
      />
    );

    expect(getByText('London Heathrow Airport')).toBeInTheDocument();
    expect(getByText('John F Kennedy International Airport')).toBeInTheDocument();
  });

  it('should render with selected airport', () => {
    const { container } = render(
      <MapView
        airports={mockAirports}
        selectedAirportId={1}
        onMarkerClick={vi.fn()}
      />
    );

    expect(container.querySelector('[data-testid="map-container"]')).toBeInTheDocument();
  });
});
