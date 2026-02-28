import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { FlightSummary } from '../types/flight';

type SortField = keyof FlightSummary;
type SortDirection = 'asc' | 'desc';

interface FlightTableProps {
  flights: FlightSummary[];
}

function formatLastSeen(isoString: string): string {
  if (!isoString) return '--';
  try {
    const date = new Date(isoString);
    return date.toLocaleString();
  } catch {
    return isoString;
  }
}

function FlightTable({ flights }: FlightTableProps) {
  const navigate = useNavigate();
  const [sortField, setSortField] = useState<SortField>('lastSeen');
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc');

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const sortedFlights = [...flights].sort((a, b) => {
    const aVal = a[sortField];
    const bVal = b[sortField];
    let comparison = 0;

    if (typeof aVal === 'string' && typeof bVal === 'string') {
      comparison = aVal.localeCompare(bVal);
    } else if (typeof aVal === 'number' && typeof bVal === 'number') {
      comparison = aVal - bVal;
    }

    return sortDirection === 'asc' ? comparison : -comparison;
  });

  const getSortClass = (field: SortField): string => {
    if (sortField !== field) return '';
    return sortDirection === 'asc' ? 'sorted-asc' : 'sorted-desc';
  };

  const columns: { field: SortField; label: string }[] = [
    { field: 'flight', label: 'Flight' },
    { field: 'hexIcao', label: 'Hex ICAO' },
    { field: 'registration', label: 'Registration' },
    { field: 'aircraftType', label: 'Type' },
    { field: 'altitude', label: 'Altitude' },
    { field: 'groundSpeed', label: 'Speed' },
    { field: 'track', label: 'Track' },
    { field: 'lastSeen', label: 'Last Seen' },
  ];

  return (
    <div style={{ overflowX: 'auto' }}>
      <table className="flight-table">
        <thead>
          <tr>
            {columns.map(({ field, label }) => (
              <th
                key={field}
                onClick={() => handleSort(field)}
                className={getSortClass(field)}
              >
                {label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {sortedFlights.map((flight) => (
            <tr
              key={flight.id}
              onClick={() => navigate(`/flight/${flight.id}`)}
            >
              <td>{flight.flight || '--'}</td>
              <td>{flight.hexIcao || '--'}</td>
              <td>{flight.registration || '--'}</td>
              <td>{flight.aircraftType || '--'}</td>
              <td>{flight.altitude != null ? `${flight.altitude.toLocaleString()} ft` : '--'}</td>
              <td>{flight.groundSpeed != null ? `${flight.groundSpeed.toFixed(0)} kts` : '--'}</td>
              <td>{flight.track != null ? `${flight.track.toFixed(0)}\u00B0` : '--'}</td>
              <td>{formatLastSeen(flight.lastSeen)}</td>
            </tr>
          ))}
          {sortedFlights.length === 0 && (
            <tr>
              <td colSpan={columns.length} style={{ textAlign: 'center', padding: '40px', color: '#8888aa' }}>
                No flights found
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

export default FlightTable;
