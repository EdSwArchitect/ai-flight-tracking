# Model

Shared data transfer objects (DTOs) and API models used across all services.

## Contents

| Class | Description |
|---|---|
| `V2Response` | Root response from ADS-B API (`/v2/mil`) |
| `AcItem` | Aircraft item with ICAO hex, position, altitude, speed, track |
| `FlightSummaryDto` | Summary view for list endpoints |
| `FlightDetailDto` | Full flight detail (20+ fields) |
| `TrackPointDto` | Lightweight position point for flight track history |
| `GeoBoxRequest` | Geographic bounding box query request |

## Usage

Add as a dependency in other modules:

```xml
<dependency>
    <groupId>com.militarytracker</groupId>
    <artifactId>military-tracker-model</artifactId>
</dependency>
```

## Build

```bash
mvn compile -pl model
```
