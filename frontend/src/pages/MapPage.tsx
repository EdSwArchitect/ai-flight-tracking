import { useState, useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useFlights } from '../hooks/useFlights';
import FlightMap from '../components/FlightMap';
import GeoBoxSelector from '../components/GeoBoxSelector';
import { fetchFlightsByGeoBox, fetchFlightTrack } from '../api/flightApi';
import type { FlightSummary, TrackPoint, GeoBoxRequest } from '../types/flight';

function MapPage() {
  const [searchParams] = useSearchParams();
  const { flights: allFlights, loading, error } = useFlights();
  const [filteredFlights, setFilteredFlights] = useState<FlightSummary[] | null>(null);
  const [geoBoxError, setGeoBoxError] = useState<string | null>(null);
  const [geoBoxLoading, setGeoBoxLoading] = useState(false);
  const [selectedFlightId, setSelectedFlightId] = useState<number | null>(null);
  const [trackPoints, setTrackPoints] = useState<TrackPoint[]>([]);
  const [trackLoading, setTrackLoading] = useState(false);
  const autoSelectDone = useRef(false);

  // Auto-select flight from query parameter (e.g., /map?flight=123)
  useEffect(() => {
    if (autoSelectDone.current || loading || allFlights.length === 0) return;

    const flightParam = searchParams.get('flight');
    if (flightParam) {
      const flightId = parseInt(flightParam, 10);
      if (!isNaN(flightId)) {
        autoSelectDone.current = true;
        handleFlightSelect(flightId);
      }
    }
  }, [loading, allFlights, searchParams]); // eslint-disable-line react-hooks/exhaustive-deps

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

  const handleFlightSelect = async (flightId: number) => {
    if (flightId === selectedFlightId) {
      setSelectedFlightId(null);
      setTrackPoints([]);
      return;
    }

    setSelectedFlightId(flightId);
    setTrackLoading(true);
    try {
      const points = await fetchFlightTrack(flightId);
      setTrackPoints(points);
    } catch {
      setTrackPoints([]);
    } finally {
      setTrackLoading(false);
    }
  };

  const handleClearSelection = () => {
    setSelectedFlightId(null);
    setTrackPoints([]);
  };

  const displayFlights = filteredFlights ?? allFlights;
  const selectedFlight = displayFlights.find((f) => f.id === selectedFlightId);

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

          {selectedFlight && (
            <div className="track-info" style={{
              display: 'flex',
              alignItems: 'center',
              gap: 12,
              marginBottom: 12,
              padding: '8px 16px',
              backgroundColor: '#16213e',
              borderRadius: 8,
              border: '1px solid #e94560',
            }}>
              <span style={{ color: '#e94560', fontWeight: 600 }}>
                Track: {selectedFlight.flight || selectedFlight.hexIcao || `#${selectedFlight.id}`}
              </span>
              {trackLoading && (
                <span style={{ color: '#8888aa', fontSize: '0.85rem' }}>Loading track...</span>
              )}
              {!trackLoading && trackPoints.length > 0 && (
                <span style={{ color: '#8888aa', fontSize: '0.85rem' }}>
                  {trackPoints.length} position{trackPoints.length !== 1 ? 's' : ''}
                </span>
              )}
              <button
                className="btn btn-secondary"
                style={{ padding: '4px 12px', fontSize: '0.8rem', marginLeft: 'auto' }}
                onClick={handleClearSelection}
              >
                Clear track
              </button>
            </div>
          )}

          <FlightMap
            flights={displayFlights}
            selectedFlightId={selectedFlightId}
            trackPoints={trackPoints}
            onFlightSelect={handleFlightSelect}
          />
        </div>
      )}
    </div>
  );
}

export default MapPage;
