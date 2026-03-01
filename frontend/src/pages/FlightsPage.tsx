import { useState } from 'react';
import { useFlights } from '../hooks/useFlights';
import FlightTable from '../components/FlightTable';

const PAGE_SIZE_OPTIONS = [25, 50, 100];

function FlightsPage() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(50);
  const { flights, loading, error } = useFlights(pageSize, page * pageSize);

  const hasNextPage = flights.length === pageSize;

  const handlePageSizeChange = (newSize: number) => {
    setPageSize(newSize);
    setPage(0);
  };

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

      {!loading && !error && (
        <>
          <FlightTable flights={flights} />

          <div className="pagination">
            <div className="pagination-info">
              Page {page + 1}
              {' \u00B7 '}
              {flights.length} flight{flights.length !== 1 ? 's' : ''}
            </div>

            <div className="pagination-controls">
              <button
                className="btn btn-secondary pagination-btn"
                disabled={page === 0}
                onClick={() => setPage(0)}
              >
                First
              </button>
              <button
                className="btn btn-secondary pagination-btn"
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                Prev
              </button>
              <button
                className="btn btn-secondary pagination-btn"
                disabled={!hasNextPage}
                onClick={() => setPage((p) => p + 1)}
              >
                Next
              </button>
            </div>

            <div className="pagination-size">
              <label htmlFor="page-size">Rows:</label>
              <select
                id="page-size"
                value={pageSize}
                onChange={(e) => handlePageSizeChange(Number(e.target.value))}
              >
                {PAGE_SIZE_OPTIONS.map((size) => (
                  <option key={size} value={size}>{size}</option>
                ))}
              </select>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

export default FlightsPage;
