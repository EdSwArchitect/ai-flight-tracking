import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { fetchFlights, fetchFlightDetail, fetchFlightsByGeoBox } from '../api/flightApi';
import type { FlightSummary, FlightDetail } from '../types/flight';

const mockFlightSummary: FlightSummary = {
  id: 1,
  hexIcao: 'AE1234',
  registration: '12-3456',
  aircraftType: 'C-17A',
  flight: 'RCH123',
  squawk: '1234',
  latitude: 38.8977,
  longitude: -77.0365,
  altitude: 35000,
  groundSpeed: 450.5,
  track: 270.0,
  lastSeen: '2025-01-15T12:00:00Z',
};

const mockFlightDetail: FlightDetail = {
  id: 1,
  hexIcao: 'AE1234',
  registration: '12-3456',
  aircraftType: 'C-17A',
  flight: 'RCH123',
  squawk: '1234',
  latitude: 38.8977,
  longitude: -77.0365,
  altitudeBaro: 35000,
  altitudeGeom: 35200,
  groundSpeed: 450.5,
  track: 270.0,
  verticalRate: 0,
  category: 'A5',
  emergencyStatus: 'none',
  dbFlags: 1,
  onGround: false,
  lastSeen: '2025-01-15T12:00:00Z',
  firstSeen: '2025-01-15T10:00:00Z',
  totalPositions: 120,
};

function createMockResponse(data: unknown, status = 200, ok = true): Response {
  return {
    ok,
    status,
    statusText: ok ? 'OK' : 'Error',
    json: () => Promise.resolve(data),
    headers: new Headers(),
    redirected: false,
    type: 'basic',
    url: '',
    clone: () => createMockResponse(data, status, ok),
    body: null,
    bodyUsed: false,
    arrayBuffer: () => Promise.resolve(new ArrayBuffer(0)),
    blob: () => Promise.resolve(new Blob()),
    formData: () => Promise.resolve(new FormData()),
    text: () => Promise.resolve(JSON.stringify(data)),
    bytes: () => Promise.resolve(new Uint8Array()),
  } as Response;
}

describe('flightApi', () => {
  let fetchMock: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    fetchMock = vi.fn();
    vi.stubGlobal('fetch', fetchMock);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('fetchFlights', () => {
    it('should fetch flights with default parameters', async () => {
      fetchMock.mockResolvedValueOnce(createMockResponse([mockFlightSummary]));

      const result = await fetchFlights();

      expect(fetchMock).toHaveBeenCalledWith('/api/list-flights?limit=50&offset=0');
      expect(result).toEqual([mockFlightSummary]);
    });

    it('should fetch flights with custom limit and offset', async () => {
      fetchMock.mockResolvedValueOnce(createMockResponse([mockFlightSummary]));

      const result = await fetchFlights(10, 20);

      expect(fetchMock).toHaveBeenCalledWith('/api/list-flights?limit=10&offset=20');
      expect(result).toEqual([mockFlightSummary]);
    });

    it('should throw an error on non-OK response', async () => {
      fetchMock.mockResolvedValueOnce(createMockResponse(null, 500, false));

      await expect(fetchFlights()).rejects.toThrow('Failed to fetch flights: 500');
    });
  });

  describe('fetchFlightDetail', () => {
    it('should fetch flight detail by ID', async () => {
      fetchMock.mockResolvedValueOnce(createMockResponse(mockFlightDetail));

      const result = await fetchFlightDetail(1);

      expect(fetchMock).toHaveBeenCalledWith('/api/list-flight/1');
      expect(result).toEqual(mockFlightDetail);
    });

    it('should throw a specific error on 404', async () => {
      fetchMock.mockResolvedValueOnce(createMockResponse(null, 404, false));

      await expect(fetchFlightDetail(999)).rejects.toThrow('Flight with ID 999 not found');
    });

    it('should throw a generic error on non-404 error', async () => {
      fetchMock.mockResolvedValueOnce(createMockResponse(null, 500, false));

      await expect(fetchFlightDetail(1)).rejects.toThrow('Failed to fetch flight detail: 500');
    });
  });

  describe('fetchFlightsByGeoBox', () => {
    it('should post geo box request and return flights', async () => {
      const geoBox = { north: 50, south: 25, east: -65, west: -125 };
      fetchMock.mockResolvedValueOnce(createMockResponse([mockFlightSummary]));

      const result = await fetchFlightsByGeoBox(geoBox);

      expect(fetchMock).toHaveBeenCalledWith('/api/geobox-list-flight', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(geoBox),
      });
      expect(result).toEqual([mockFlightSummary]);
    });

    it('should throw an error on non-OK response', async () => {
      const geoBox = { north: 50, south: 25, east: -65, west: -125 };
      fetchMock.mockResolvedValueOnce(createMockResponse(null, 500, false));

      await expect(fetchFlightsByGeoBox(geoBox)).rejects.toThrow(
        'Failed to fetch flights by geo box: 500'
      );
    });
  });
});
