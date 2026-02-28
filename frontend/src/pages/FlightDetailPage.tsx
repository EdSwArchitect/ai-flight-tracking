import { useParams, useNavigate } from 'react-router-dom';
import { useFlightDetail } from '../hooks/useFlightDetail';
import FlightDetailComponent from '../components/FlightDetail';

function FlightDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const flightId = id ? parseInt(id, 10) : null;
  const { flight, loading, error } = useFlightDetail(flightId);

  return (
    <div>
      <div className="back-row">
        <button className="btn btn-secondary" onClick={() => navigate('/')}>
          &larr; Back to Flights
        </button>
      </div>

      {loading && (
        <div className="loading">
          <div className="loading-spinner" />
          Loading flight details...
        </div>
      )}

      {error && (
        <div className="error">
          Error: {error}
        </div>
      )}

      {!loading && !error && flight && <FlightDetailComponent flight={flight} />}

      {!loading && !error && !flight && (
        <div className="error">
          Flight not found.
        </div>
      )}
    </div>
  );
}

export default FlightDetailPage;
