import { useState, useEffect } from 'react';
import { fetchRoutes, createRoute, deleteRoute } from '../api/routes';
import { fetchAirports } from '../api/airports';
import type { Route } from '../types/route';
import type { Airport } from '../types/airport';
import './RoutesPage.css';

/**
 * Routes management page.
 * Allows listing, creating, and deleting routes for an airline.
 */
export function RoutesPage() {
  const [routes, setRoutes] = useState<Route[]>([]);
  const [airports, setAirports] = useState<Airport[]>([]);
  const [airlineId, setAirlineId] = useState<number>(1);
  const [originAirportId, setOriginAirportId] = useState<number>(0);
  const [destinationAirportId, setDestinationAirportId] = useState<number>(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Load airports on mount
  useEffect(() => {
    loadAirports();
  }, []);

  // Load routes when airline changes
  useEffect(() => {
    if (airlineId > 0) {
      loadRoutes();
    }
  }, [airlineId]);

  const loadAirports = async () => {
    try {
      const data = await fetchAirports({ limit: 1000 });
      setAirports(data);
    } catch (err) {
      setError(`Failed to load airports: ${(err as Error).message}`);
    }
  };

  const loadRoutes = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await fetchRoutes({ airlineId });
      setRoutes(data);
    } catch (err) {
      setError(`Failed to load routes: ${(err as Error).message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateRoute = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!originAirportId || !destinationAirportId) {
      setError('Please select both origin and destination airports');
      return;
    }

    if (originAirportId === destinationAirportId) {
      setError('Origin and destination airports must be different');
      return;
    }

    try {
      setIsLoading(true);
      setError(null);
      setSuccessMessage(null);
      
      const newRoute = await createRoute({
        airlineId,
        originAirportId,
        destinationAirportId,
      });
      
      setRoutes([...routes, newRoute]);
      setSuccessMessage(`Route created successfully! Distance: ${newRoute.distanceKm} km`);
      
      // Reset form
      setOriginAirportId(0);
      setDestinationAirportId(0);
    } catch (err) {
      setError(`Failed to create route: ${(err as Error).message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteRoute = async (routeId: number) => {
    if (!confirm('Are you sure you want to delete this route?')) {
      return;
    }

    try {
      setIsLoading(true);
      setError(null);
      setSuccessMessage(null);
      
      await deleteRoute(routeId);
      setRoutes(routes.filter((r) => r.id !== routeId));
      setSuccessMessage('Route deleted successfully');
    } catch (err) {
      setError(`Failed to delete route: ${(err as Error).message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const getAirportName = (airportId: number): string => {
    const airport = airports.find((a) => a.id === airportId);
    return airport ? `${airport.name} (${airport.iata || airport.icao})` : `Airport ${airportId}`;
  };

  return (
    <div className="routes-page">
      <h1>Route Management</h1>

      {/* Airline Selection */}
      <div className="airline-selector">
        <label htmlFor="airlineId">Airline ID:</label>
        <input
          type="number"
          id="airlineId"
          value={airlineId}
          onChange={(e) => setAirlineId(parseInt(e.target.value) || 1)}
          min="1"
        />
      </div>

      {/* Status Messages */}
      {error && (
        <div className="message error" role="alert">
          {error}
        </div>
      )}
      {successMessage && (
        <div className="message success" role="status">
          {successMessage}
        </div>
      )}

      {/* Create Route Form */}
      <section className="create-route-section">
        <h2>Create New Route</h2>
        <form onSubmit={handleCreateRoute} className="create-route-form">
          <div className="form-group">
            <label htmlFor="originAirportId">Origin Airport:</label>
            <select
              id="originAirportId"
              value={originAirportId}
              onChange={(e) => setOriginAirportId(parseInt(e.target.value))}
              required
            >
              <option value="0">Select Origin Airport</option>
              {airports.map((airport) => (
                <option key={airport.id} value={airport.id}>
                  {airport.name} ({airport.iata || airport.icao}) - {airport.city}, {airport.countryCode}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="destinationAirportId">Destination Airport:</label>
            <select
              id="destinationAirportId"
              value={destinationAirportId}
              onChange={(e) => setDestinationAirportId(parseInt(e.target.value))}
              required
            >
              <option value="0">Select Destination Airport</option>
              {airports.map((airport) => (
                <option key={airport.id} value={airport.id}>
                  {airport.name} ({airport.iata || airport.icao}) - {airport.city}, {airport.countryCode}
                </option>
              ))}
            </select>
          </div>

          <button type="submit" disabled={isLoading}>
            {isLoading ? 'Creating...' : 'Create Route'}
          </button>
        </form>
      </section>

      {/* Routes List */}
      <section className="routes-list-section">
        <h2>Routes for Airline {airlineId}</h2>
        
        {isLoading && <p>Loading routes...</p>}
        
        {!isLoading && routes.length === 0 && (
          <p className="no-routes">No routes found. Create one above!</p>
        )}

        {!isLoading && routes.length > 0 && (
          <table className="routes-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Origin</th>
                <th>Destination</th>
                <th>Distance (km)</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {routes.map((route) => (
                <tr key={route.id}>
                  <td>{route.id}</td>
                  <td>{getAirportName(route.originAirportId)}</td>
                  <td>{getAirportName(route.destinationAirportId)}</td>
                  <td>{route.distanceKm.toLocaleString()}</td>
                  <td>{new Date(route.createdAt * 1000).toLocaleDateString()}</td>
                  <td>
                    <button
                      onClick={() => handleDeleteRoute(route.id)}
                      disabled={isLoading}
                      className="delete-button"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
