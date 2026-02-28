import { useState, useEffect, useCallback } from 'react';
import type { FlightSummary } from '../types/flight';
import { fetchFlights } from '../api/flightApi';

const REFRESH_INTERVAL_MS = 15_000;

interface UseFlightsResult {
  flights: FlightSummary[];
  loading: boolean;
  error: string | null;
  refresh: () => void;
}

export function useFlights(limit: number = 50, offset: number = 0): UseFlightsResult {
  const [flights, setFlights] = useState<FlightSummary[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const loadFlights = useCallback(async () => {
    try {
      setError(null);
      const data = await fetchFlights(limit, offset);
      setFlights(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
    } finally {
      setLoading(false);
    }
  }, [limit, offset]);

  useEffect(() => {
    setLoading(true);
    loadFlights();

    const interval = setInterval(loadFlights, REFRESH_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [loadFlights]);

  return { flights, loading, error, refresh: loadFlights };
}
