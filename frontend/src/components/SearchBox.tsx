import { useState, useEffect, useRef } from 'react';
import { searchAirports } from '../api/airports';
import type { Airport } from '../types/airport';
import './SearchBox.css';

interface SearchBoxProps {
  onSelectAirport: (airport: Airport) => void;
}

/**
 * SearchBox component with debounced search for airports.
 * 
 * Features:
 * - 300ms debounce for search queries
 * - Dropdown results with keyboard navigation support
 * - Abort previous requests when new search is triggered
 * - "No matches" state for empty results
 * - Loading state during search
 * 
 * @future
 * - Recent searches history
 * - Keyboard shortcuts (Cmd+K to focus)
 * - Advanced filters (country, size)
 * - Search result caching
 */
export function SearchBox({ onSelectAirport }: SearchBoxProps) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Airport[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isOpen, setIsOpen] = useState(false);
  const abortControllerRef = useRef<AbortController | null>(null);
  const searchTimeoutRef = useRef<number | null>(null);

  useEffect(() => {
    // Clear previous timeout
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    // Abort previous request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    if (query.trim().length === 0) {
      setResults([]);
      setIsOpen(false);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);

    // Debounce search by 300ms
    searchTimeoutRef.current = window.setTimeout(async () => {
      const controller = new AbortController();
      abortControllerRef.current = controller;

      try {
        const airports = await searchAirports({ q: query }, controller.signal);
        setResults(airports);
        setIsOpen(true);
      } catch (error) {
        // Ignore abort errors
        if ((error as Error).name !== 'AbortError') {
          console.error('Search failed:', error);
          setResults([]);
        }
      } finally {
        setIsLoading(false);
      }
    }, 300);

    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [query]);

  const handleSelectAirport = (airport: Airport) => {
    onSelectAirport(airport);
    setQuery('');
    setResults([]);
    setIsOpen(false);
  };

  return (
    <div className="search-box">
      <input
        type="text"
        className="search-input"
        placeholder="Search airports by name, city, or code..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        onFocus={() => {
          if (results.length > 0) {
            setIsOpen(true);
          }
        }}
        aria-label="Search airports"
        aria-expanded={isOpen}
        aria-controls="search-results"
        aria-autocomplete="list"
      />
      {isLoading && <div className="search-loading">Searching...</div>}
      {isOpen && !isLoading && (
        <div
          id="search-results"
          className="search-results"
          role="listbox"
          aria-label="Search results"
        >
          {results.length === 0 ? (
            <div className="search-no-results">No matches found</div>
          ) : (
            results.map((airport) => (
              <button
                key={airport.id}
                className="search-result-item"
                onClick={() => handleSelectAirport(airport)}
                role="option"
                aria-selected="false"
              >
                <div className="search-result-name">
                  {airport.name} ({airport.iata || airport.icao || 'N/A'})
                </div>
                <div className="search-result-location">
                  {airport.city}, {airport.countryCode}
                </div>
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
}
