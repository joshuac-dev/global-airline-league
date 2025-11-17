import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import type { Airport } from '../types/airport';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './MapView.css';

// Fix Leaflet default marker icon paths in bundled environment
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
});

// Default marker icon
const defaultIcon = new L.Icon.Default();

// Highlighted marker icon (blue marker)
const highlightedIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

interface MapViewProps {
  airports: Airport[];
  selectedAirportId: number | null;
  onMarkerClick?: (airport: Airport) => void;
}

/**
 * Component to fly to selected airport when selectedAirportId changes.
 */
function FlyToAirport({ airport }: { airport: Airport | null }) {
  const map = useMap();

  if (airport) {
    map.flyTo([airport.latitude, airport.longitude], 10, {
      duration: 1.5,
    });
  }

  return null;
}

/**
 * MapView component displays an OpenStreetMap with airport markers.
 * 
 * Features:
 * - OSM raster tiles with attribution
 * - Airport markers with popups showing name, IATA/ICAO, city, country
 * - Selected marker highlighting (blue icon)
 * - Click handlers for markers
 * - Fly-to animation when airport is selected
 * 
 * @future
 * - Marker clustering for performance (use react-leaflet-markercluster)
 * - Viewport-based lazy loading
 * - Custom marker sprites for different airport sizes
 * - Route polylines overlay
 * - Demand heatmap layer
 */
export function MapView({ airports, selectedAirportId, onMarkerClick }: MapViewProps) {
  const selectedAirport = airports.find((a) => a.id === selectedAirportId) || null;

  return (
    <MapContainer
      center={[20, 0]}
      zoom={2}
      className="map-container"
      scrollWheelZoom={true}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {airports.map((airport) => {
        const isSelected = airport.id === selectedAirportId;
        const icon = isSelected ? highlightedIcon : defaultIcon;

        return (
          <Marker
            key={airport.id}
            position={[airport.latitude, airport.longitude]}
            icon={icon}
            eventHandlers={{
              click: () => {
                if (onMarkerClick) {
                  onMarkerClick(airport);
                }
              },
            }}
          >
            <Popup>
              <div className="airport-popup">
                <h3>{airport.name}</h3>
                <p>
                  <strong>IATA:</strong> {airport.iata || 'N/A'} |{' '}
                  <strong>ICAO:</strong> {airport.icao || 'N/A'}
                </p>
                <p>
                  <strong>City:</strong> {airport.city}
                </p>
                <p>
                  <strong>Country:</strong> {airport.countryCode}
                </p>
              </div>
            </Popup>
          </Marker>
        );
      })}
      <FlyToAirport airport={selectedAirport} />
    </MapContainer>
  );
}
