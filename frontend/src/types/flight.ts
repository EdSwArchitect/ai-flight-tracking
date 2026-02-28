export interface FlightSummary {
  id: number;
  hexIcao: string;
  registration: string;
  aircraftType: string;
  flight: string;
  squawk: string;
  latitude: number;
  longitude: number;
  altitude: number;
  groundSpeed: number;
  track: number;
  lastSeen: string;
}

export interface FlightDetail {
  id: number;
  hexIcao: string;
  registration: string;
  aircraftType: string;
  flight: string;
  squawk: string;
  latitude: number;
  longitude: number;
  altitudeBaro: number;
  altitudeGeom: number;
  groundSpeed: number;
  track: number;
  verticalRate: number;
  category: string;
  emergencyStatus: string;
  dbFlags: number;
  onGround: boolean;
  lastSeen: string;
  firstSeen: string;
  totalPositions: number;
}

export interface GeoBoxRequest {
  north: number;
  south: number;
  east: number;
  west: number;
}
