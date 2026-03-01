import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import type { FlightSummary } from '../types/flight';

type SortField = keyof FlightSummary;
type SortDirection = 'asc' | 'desc';

interface FlightTableProps {
  flights: FlightSummary[];
}

interface ContextMenuState {
  visible: boolean;
  x: number;
  y: number;
  flightId: number | null;
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
  const [contextMenu, setContextMenu] = useState<ContextMenuState>({
    visible: false, x: 0, y: 0, flightId: null,
  });

  const closeContextMenu = useCallback(() => {
    setContextMenu((prev) => ({ ...prev, visible: false }));
  }, []);

  useEffect(() => {
    if (contextMenu.visible) {
      const handler = () => closeContextMenu();
      document.addEventListener('click', handler);
      document.addEventListener('scroll', handler, true);
      return () => {
        document.removeEventListener('click', handler);
        document.removeEventListener('scroll', handler, true);
      };
    }
  }, [contextMenu.visible, closeContextMenu]);

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const handleContextMenu = (e: React.MouseEvent, flightId: number) => {
    e.preventDefault();
    setContextMenu({ visible: true, x: e.clientX, y: e.clientY, flightId });
  };

  const handleShowOnMap = () => {
    if (contextMenu.flightId != null) {
      window.open(`/map?flight=${contextMenu.flightId}`, '_blank');
    }
    closeContextMenu();
  };

  const handleViewDetails = () => {
    if (contextMenu.flightId != null) {
      navigate(`/flight/${contextMenu.flightId}`);
    }
    closeContextMenu();
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
              onContextMenu={(e) => handleContextMenu(e, flight.id)}
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

      {contextMenu.visible && (
        <div
          className="context-menu"
          style={{ top: contextMenu.y, left: contextMenu.x }}
        >
          <button className="context-menu-item" onClick={handleShowOnMap}>
            Show on Map
          </button>
          <button className="context-menu-item" onClick={handleViewDetails}>
            View Details
          </button>
        </div>
      )}
    </div>
  );
}

export default FlightTable;
