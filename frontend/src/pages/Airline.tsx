import { useParams } from 'react-router-dom';

export default function Airline() {
  const { id } = useParams<{ id: string }>();

  return (
    <div style={{ padding: '2rem' }}>
      <h1>Airline Details</h1>
      <p>Airline ID: {id}</p>
      <p>Airline management interface coming soon...</p>
    </div>
  );
}
