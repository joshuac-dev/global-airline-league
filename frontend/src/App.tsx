import { useState, useEffect } from 'react';
import { MapView } from './components/MapView';
import { SearchBox } from './components/SearchBox';
import { fetchAirports } from './api/airports';
import type { Airport } from './types/airport';
import './App.css';

/**
 * Main application component.
 * 
 * Features:
 * - Initial load of airports (200 limit for performance before clustering)
 * - Search box for finding airports
 * - Map with markers and fly-to functionality
 * - Load more pagination for additional airports
 * 
 * @future
 * - Marker clustering (react-leaflet-markercluster)
 * - Viewport-based lazy loading
 * - Route visualization
 * - Real-time updates via WebSocket
 * - User authentication and airline management
 */
function App() {
  const [airports, setAirports] = useState<Airport[]>([]);
  const [selectedAirportId, setSelectedAirportId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const [offset, setOffset] = useState(0);

  useEffect(() => {
    loadAirports();
  }, []);

  const loadAirports = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const newAirports = await fetchAirports({ offset, limit: 200 });
      
      if (newAirports.length === 0) {
        setHasMore(false);
      } else {
        setAirports((prev) => [...prev, ...newAirports]);
        setOffset((prev) => prev + newAirports.length);
      }
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelectAirport = (airport: Airport) => {
    setSelectedAirportId(airport.id);
  };

  const handleMarkerClick = (airport: Airport) => {
    setSelectedAirportId(airport.id);
  };

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-content">
          <h1 className="app-title">Global Airline League</h1>
          <SearchBox onSelectAirport={handleSelectAirport} />
        </div>
      </header>

      <main className="app-main">
        {error && (
          <div className="error-banner" role="alert">
            <p>{error}</p>
            <button onClick={loadAirports} className="retry-button">
              Retry
            </button>
          </div>
        )}

        {!error && (
          <>
            <MapView
              airports={airports}
              selectedAirportId={selectedAirportId}
              onMarkerClick={handleMarkerClick}
            />
            
            {hasMore && (
              <div className="load-more-container">
                <button
                  onClick={loadAirports}
                  disabled={isLoading}
                  className="load-more-button"
                >
                  {isLoading ? 'Loading...' : `Load More Airports (${airports.length} loaded)`}
                </button>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}

export default App;
