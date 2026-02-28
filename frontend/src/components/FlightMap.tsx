import { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import type { FlightSummary } from '../types/flight';

// Fix default marker icons for Leaflet with bundlers
delete (L.Icon.Default.prototype as Record<string, unknown>)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

const DEFAULT_CENTER: [number, number] = [39.8283, -98.5795];
const DEFAULT_ZOOM = 4;

interface FlightMapProps {
  flights: FlightSummary[];
}

function MapCenterUpdater({ flights }: { flights: FlightSummary[] }) {
  const map = useMap();

  useEffect(() => {
    if (flights.length > 0) {
      const firstFlight = flights[0];
      if (firstFlight.latitude && firstFlight.longitude) {
        map.setView([firstFlight.latitude, firstFlight.longitude], map.getZoom());
      }
    }
  }, [flights, map]);

  return null;
}

function FlightMap({ flights }: FlightMapProps) {
  const center: [number, number] =
    flights.length > 0 && flights[0].latitude && flights[0].longitude
      ? [flights[0].latitude, flights[0].longitude]
      : DEFAULT_CENTER;

  return (
    <div className="map-container">
      <MapContainer
        center={center}
        zoom={DEFAULT_ZOOM}
        style={{ height: '100%', width: '100%' }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MapCenterUpdater flights={flights} />
        {flights.map((flight) => {
          if (!flight.latitude || !flight.longitude) return null;
          return (
            <Marker
              key={flight.id}
              position={[flight.latitude, flight.longitude]}
            >
              <Popup>
                <div>
                  <strong>{flight.flight || 'Unknown'}</strong>
                  <br />
                  Hex ICAO: {flight.hexIcao || '--'}
                  <br />
                  Type: {flight.aircraftType || '--'}
                  <br />
                  Altitude: {flight.altitude != null ? `${flight.altitude.toLocaleString()} ft` : '--'}
                  <br />
                  Speed: {flight.groundSpeed != null ? `${flight.groundSpeed.toFixed(0)} kts` : '--'}
                  <br />
                  Track: {flight.track != null ? `${flight.track.toFixed(0)}\u00B0` : '--'}
                </div>
              </Popup>
            </Marker>
          );
        })}
      </MapContainer>
    </div>
  );
}

export default FlightMap;
