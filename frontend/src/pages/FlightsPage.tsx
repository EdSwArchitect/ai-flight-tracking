import { useFlights } from '../hooks/useFlights';
import FlightTable from '../components/FlightTable';

function FlightsPage() {
  const { flights, loading, error } = useFlights();

  return (
    <div>
      <h1 className="page-title">Active Military Flights</h1>

      {loading && (
        <div className="loading">
          <div className="loading-spinner" />
          Loading flights...
        </div>
      )}

      {error && (
        <div className="error">
          Error: {error}
        </div>
      )}

      {!loading && !error && <FlightTable flights={flights} />}
    </div>
  );
}

export default FlightsPage;
