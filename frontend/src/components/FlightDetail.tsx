import type { FlightDetail as FlightDetailType } from '../types/flight';

interface FlightDetailProps {
  flight: FlightDetailType;
}

function formatDateTime(isoString: string): string {
  if (!isoString) return '--';
  try {
    const date = new Date(isoString);
    return date.toLocaleString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  } catch {
    return isoString;
  }
}

function FlightDetailComponent({ flight }: FlightDetailProps) {
  return (
    <div className="card">
      <div className="card-title">
        {flight.flight || 'Unknown Flight'}{' '}
        {flight.onGround ? (
          <span className="badge badge-on-ground">On Ground</span>
        ) : (
          <span className="badge badge-airborne">Airborne</span>
        )}
        {flight.emergencyStatus && flight.emergencyStatus !== 'none' && (
          <span className="badge badge-emergency" style={{ marginLeft: 8 }}>
            {flight.emergencyStatus}
          </span>
        )}
      </div>

      <div className="detail-grid">
        <div className="detail-item">
          <span className="detail-label">ID</span>
          <span className="detail-value">{flight.id}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Hex ICAO</span>
          <span className="detail-value">{flight.hexIcao || '--'}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Registration</span>
          <span className="detail-value">{flight.registration || '--'}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Aircraft Type</span>
          <span className="detail-value">{flight.aircraftType || '--'}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Flight</span>
          <span className="detail-value">{flight.flight || '--'}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Squawk</span>
          <span className="detail-value">{flight.squawk || '--'}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Category</span>
          <span className="detail-value">{flight.category || '--'}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Latitude</span>
          <span className="detail-value">{flight.latitude?.toFixed(6) ?? '--'}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Longitude</span>
          <span className="detail-value">{flight.longitude?.toFixed(6) ?? '--'}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Barometric Altitude</span>
          <span className="detail-value">
            {flight.altitudeBaro != null ? `${flight.altitudeBaro.toLocaleString()} ft` : '--'}
          </span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Geometric Altitude</span>
          <span className="detail-value">
            {flight.altitudeGeom != null ? `${flight.altitudeGeom.toLocaleString()} ft` : '--'}
          </span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Ground Speed</span>
          <span className="detail-value">
            {flight.groundSpeed != null ? `${flight.groundSpeed.toFixed(0)} kts` : '--'}
          </span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Track</span>
          <span className="detail-value">
            {flight.track != null ? `${flight.track.toFixed(0)}\u00B0` : '--'}
          </span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Vertical Rate</span>
          <span className="detail-value">
            {flight.verticalRate != null ? `${flight.verticalRate.toFixed(0)} ft/min` : '--'}
          </span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Emergency Status</span>
          <span className="detail-value">{flight.emergencyStatus || 'None'}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">DB Flags</span>
          <span className="detail-value">{flight.dbFlags}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">On Ground</span>
          <span className="detail-value">{flight.onGround ? 'Yes' : 'No'}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">First Seen</span>
          <span className="detail-value">{formatDateTime(flight.firstSeen)}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Last Seen</span>
          <span className="detail-value">{formatDateTime(flight.lastSeen)}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Total Positions</span>
          <span className="detail-value">{flight.totalPositions ?? '--'}</span>
        </div>
      </div>
    </div>
  );
}

export default FlightDetailComponent;
