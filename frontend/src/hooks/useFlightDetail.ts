import { useState, useEffect } from 'react';
import type { FlightDetail } from '../types/flight';
import { fetchFlightDetail } from '../api/flightApi';

interface UseFlightDetailResult {
  flight: FlightDetail | null;
  loading: boolean;
  error: string | null;
}

export function useFlightDetail(id: number | null): UseFlightDetailResult {
  const [flight, setFlight] = useState<FlightDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id === null) {
      setLoading(false);
      return;
    }

    let cancelled = false;

    async function loadDetail() {
      try {
        setLoading(true);
        setError(null);
        const data = await fetchFlightDetail(id!);
        if (!cancelled) {
          setFlight(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'An unknown error occurred');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadDetail();

    return () => {
      cancelled = true;
    };
  }, [id]);

  return { flight, loading, error };
}
