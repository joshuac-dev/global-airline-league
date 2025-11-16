import { useEffect, useState } from 'react';
import { apiClient, HealthResponse } from '../api/client';
import { useWorldWebSocket } from '../hooks/useWebSocket';

export default function Home() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { lastMessage, isConnected } = useWorldWebSocket();

  useEffect(() => {
    apiClient.getHealth()
      .then(setHealth)
      .catch((err) => setError(err.message));
  }, []);

  return (
    <div style={{ padding: '2rem' }}>
      <h1>Global Airline League</h1>
      
      <section style={{ marginTop: '2rem' }}>
        <h2>System Status</h2>
        {error && <p style={{ color: 'red' }}>Error: {error}</p>}
        {health && (
          <div>
            <p><strong>Status:</strong> {health.status}</p>
            <p><strong>Version:</strong> {health.version}</p>
          </div>
        )}
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>WebSocket Status</h2>
        <p><strong>Connected:</strong> {isConnected ? 'Yes ✅' : 'No ❌'}</p>
        {lastMessage && (
          <div>
            <p><strong>Last Message Type:</strong> {lastMessage.type}</p>
            <p><strong>Timestamp:</strong> {new Date(lastMessage.ts).toLocaleTimeString()}</p>
            {'cycle' in lastMessage && <p><strong>Cycle:</strong> {lastMessage.cycle}</p>}
          </div>
        )}
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>About</h2>
        <p>
          This is a complete rewrite of Global Airline League in Kotlin + Ktor + React + TypeScript.
        </p>
        <p>
          <strong>Note:</strong> This project uses OpenStreetMap exclusively. Google Maps APIs are not used
          and must not be integrated into this project.
        </p>
      </section>
    </div>
  );
}
