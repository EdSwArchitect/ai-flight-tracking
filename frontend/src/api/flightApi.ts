import type { FlightSummary, FlightDetail, GeoBoxRequest } from '../types/flight';

const API_BASE = '/api';

export async function fetchFlights(
  limit: number = 50,
  offset: number = 0
): Promise<FlightSummary[]> {
  const params = new URLSearchParams({
    limit: String(limit),
    offset: String(offset),
  });
  const response = await fetch(`${API_BASE}/list-flights?${params}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch flights: ${response.status} ${response.statusText}`);
  }
  return response.json();
}

export async function fetchFlightDetail(id: number): Promise<FlightDetail> {
  const response = await fetch(`${API_BASE}/list-flight/${id}`);
  if (!response.ok) {
    if (response.status === 404) {
      throw new Error(`Flight with ID ${id} not found`);
    }
    throw new Error(`Failed to fetch flight detail: ${response.status} ${response.statusText}`);
  }
  return response.json();
}

export async function fetchFlightsByGeoBox(box: GeoBoxRequest): Promise<FlightSummary[]> {
  const response = await fetch(`${API_BASE}/geobox-list-flight`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(box),
  });
  if (!response.ok) {
    throw new Error(`Failed to fetch flights by geo box: ${response.status} ${response.statusText}`);
  }
  return response.json();
}
