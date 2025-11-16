import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { useWorldWebSocket } from '../hooks/useWebSocket';
import '../components/LeafletIconFix';
import 'leaflet/dist/leaflet.css';

/*
 * IMPORTANT: This project uses OpenStreetMap exclusively.
 * Google Maps APIs must NOT be used or integrated.
 * 
 * OSM Tile Server: https://tile.openstreetmap.org/{z}/{x}/{y}.png
 * License: © OpenStreetMap contributors (ODbL)
 */

export default function World() {
  const { lastMessage, isConnected } = useWorldWebSocket();

  // Placeholder airport location (e.g., London Heathrow)
  const placeholderAirport = {
    position: [51.4700, -0.4543] as [number, number],
    name: 'London Heathrow (LHR)',
  };

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
      <div style={{ padding: '1rem', background: '#f5f5f5', borderBottom: '1px solid #ddd' }}>
        <h1 style={{ margin: 0 }}>World Map</h1>
        <p style={{ margin: '0.5rem 0 0 0' }}>
          WebSocket: {isConnected ? '✅ Connected' : '❌ Disconnected'}
          {lastMessage && ` | Last update: ${new Date(lastMessage.ts).toLocaleTimeString()}`}
        </p>
      </div>
      
      <div style={{ flex: 1 }}>
        <MapContainer
          center={[20, 0]}
          zoom={2}
          style={{ height: '100%', width: '100%' }}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <Marker position={placeholderAirport.position}>
            <Popup>
              <strong>{placeholderAirport.name}</strong>
              <br />
              Placeholder marker for testing
            </Popup>
          </Marker>
        </MapContainer>
      </div>
    </div>
  );
}
