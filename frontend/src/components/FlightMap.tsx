import { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, CircleMarker, Tooltip, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import type { FlightSummary, TrackPoint } from '../types/flight';

// Fix default marker icons for Leaflet with bundlers
delete (L.Icon.Default.prototype as unknown as Record<string, unknown>)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

const selectedIcon = new L.Icon({
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
  className: 'selected-marker',
});

const DEFAULT_CENTER: [number, number] = [39.8283, -98.5795];
const DEFAULT_ZOOM = 4;

interface FlightMapProps {
  flights: FlightSummary[];
  selectedFlightId?: number | null;
  trackPoints?: TrackPoint[];
  onFlightSelect?: (flightId: number) => void;
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

function TrackFitter({ trackPoints }: { trackPoints: TrackPoint[] }) {
  const map = useMap();

  useEffect(() => {
    if (trackPoints.length >= 2) {
      const bounds = L.latLngBounds(
        trackPoints.map((p) => [p.latitude, p.longitude] as [number, number])
      );
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [trackPoints, map]);

  return null;
}

function FlightMap({ flights, selectedFlightId, trackPoints, onFlightSelect }: FlightMapProps) {
  const center: [number, number] =
    flights.length > 0 && flights[0].latitude && flights[0].longitude
      ? [flights[0].latitude, flights[0].longitude]
      : DEFAULT_CENTER;

  const trackLatLngs: [number, number][] =
    trackPoints?.map((p) => [p.latitude, p.longitude] as [number, number]) ?? [];

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

        {trackPoints && trackPoints.length >= 2 && (
          <TrackFitter trackPoints={trackPoints} />
        )}

        {/* Track polyline */}
        {trackLatLngs.length >= 2 && (
          <Polyline
            positions={trackLatLngs}
            pathOptions={{ color: '#e94560', weight: 3, opacity: 0.8 }}
          />
        )}

        {/* Track point dots */}
        {trackPoints?.map((point) => (
          <CircleMarker
            key={point.id}
            center={[point.latitude, point.longitude]}
            radius={4}
            pathOptions={{
              color: '#e94560',
              fillColor: '#e94560',
              fillOpacity: 0.7,
              weight: 1,
            }}
          >
            <Tooltip>
              <div>
                Alt: {point.altBaro != null ? `${point.altBaro.toLocaleString()} ft` : '--'}
                <br />
                Speed: {point.groundSpeed != null ? `${point.groundSpeed.toFixed(0)} kts` : '--'}
                <br />
                {new Date(point.timestamp).toLocaleTimeString()}
              </div>
            </Tooltip>
          </CircleMarker>
        ))}

        {/* Flight markers */}
        {flights.map((flight) => {
          if (!flight.latitude || !flight.longitude) return null;
          const isSelected = flight.id === selectedFlightId;
          return (
            <Marker
              key={flight.id}
              position={[flight.latitude, flight.longitude]}
              icon={isSelected ? selectedIcon : new L.Icon.Default()}
              eventHandlers={{
                click: () => onFlightSelect?.(flight.id),
              }}
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
