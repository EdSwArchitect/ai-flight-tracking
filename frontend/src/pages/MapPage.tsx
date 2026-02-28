import { useState } from 'react';
import { useFlights } from '../hooks/useFlights';
import FlightMap from '../components/FlightMap';
import GeoBoxSelector from '../components/GeoBoxSelector';
import { fetchFlightsByGeoBox } from '../api/flightApi';
import type { FlightSummary, GeoBoxRequest } from '../types/flight';

function MapPage() {
  const { flights: allFlights, loading, error } = useFlights();
  const [filteredFlights, setFilteredFlights] = useState<FlightSummary[] | null>(null);
  const [geoBoxError, setGeoBoxError] = useState<string | null>(null);
  const [geoBoxLoading, setGeoBoxLoading] = useState(false);

  const handleGeoBoxSearch = async (box: GeoBoxRequest) => {
    try {
      setGeoBoxLoading(true);
      setGeoBoxError(null);
      const results = await fetchFlightsByGeoBox(box);
      setFilteredFlights(results);
    } catch (err) {
      setGeoBoxError(err instanceof Error ? err.message : 'Failed to search by geo box');
    } finally {
      setGeoBoxLoading(false);
    }
  };

  const displayFlights = filteredFlights ?? allFlights;

  return (
    <div>
      <h1 className="page-title">Flight Map</h1>

      <GeoBoxSelector onSearch={handleGeoBoxSearch} />

      {geoBoxError && (
        <div className="error" style={{ marginTop: 12 }}>
          Geo box error: {geoBoxError}
        </div>
      )}

      {geoBoxLoading && (
        <div className="loading">
          <div className="loading-spinner" />
          Searching geo box...
        </div>
      )}

      {loading && !filteredFlights && (
        <div className="loading">
          <div className="loading-spinner" />
          Loading flights...
        </div>
      )}

      {error && !filteredFlights && (
        <div className="error">
          Error: {error}
        </div>
      )}

      {!loading && !geoBoxLoading && (
        <div style={{ marginTop: 20 }}>
          <p style={{ color: '#8888aa', marginBottom: 12 }}>
            Showing {displayFlights.length} flight{displayFlights.length !== 1 ? 's' : ''}
            {filteredFlights !== null && (
              <>
                {' '}(filtered){' '}
                <button
                  className="btn btn-secondary"
                  style={{ padding: '4px 12px', fontSize: '0.8rem', marginLeft: 8 }}
                  onClick={() => setFilteredFlights(null)}
                >
                  Clear filter
                </button>
              </>
            )}
          </p>
          <FlightMap flights={displayFlights} />
        </div>
      )}
    </div>
  );
}

export default MapPage;
