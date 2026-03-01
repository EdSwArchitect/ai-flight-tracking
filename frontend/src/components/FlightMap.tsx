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

// Fighter aircraft SVG silhouette (top-down view, pointing up)
const AIRCRAFT_SVG = `
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" width="28" height="28">
  <path d="
    M16 1
    L17.5 10
    L26 14
    L26 15.5
    L17.5 14
    L17.5 22
    L21 25
    L21 26.5
    L16 24
    L11 26.5
    L11 25
    L14.5 22
    L14.5 14
    L6 15.5
    L6 14
    L14.5 10
    Z"
    fill="FILL_COLOR"
    stroke="STROKE_COLOR"
    stroke-width="0.6"
    stroke-linejoin="round"
  />
</svg>`;

function createAircraftIcon(heading: number | null, isSelected: boolean): L.DivIcon {
  const fill = isSelected ? '#e94560' : '#c0c0e0';
  const stroke = isSelected ? '#a0001a' : '#444466';
  const rotation = heading ?? 0;
  const svg = AIRCRAFT_SVG
    .replace('FILL_COLOR', fill)
    .replace('STROKE_COLOR', stroke);

  return L.divIcon({
    html: `<div style="transform: rotate(${rotation}deg); width: 28px; height: 28px;">${svg}</div>`,
    className: 'aircraft-icon',
    iconSize: [28, 28],
    iconAnchor: [14, 14],
    popupAnchor: [0, -14],
  });
}

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

        {/* Flight markers with aircraft icons */}
        {flights.map((flight) => {
          if (!flight.latitude || !flight.longitude) return null;
          const isSelected = flight.id === selectedFlightId;
          return (
            <Marker
              key={flight.id}
              position={[flight.latitude, flight.longitude]}
              icon={createAircraftIcon(flight.track, isSelected)}
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
