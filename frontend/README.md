# Frontend

React 19 TypeScript single-page application for visualizing military aircraft data on interactive maps.

## Pages

| Page | Route | Description |
|---|---|---|
| Flights | `/` | Paginated table of all tracked flights |
| Flight Detail | `/flight/:id` | Detailed view of a selected aircraft |
| Map | `/map` | Interactive Leaflet map with aircraft markers, GeoBox selection, and flight track visualization |

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| React | 19.x | UI framework |
| TypeScript | 5.x | Type safety |
| Vite | 6.x | Build tool + dev server |
| Leaflet | 1.9.x | Interactive maps |
| react-leaflet | 5.x | React bindings for Leaflet |
| react-router-dom | 7.x | Client-side routing |

## Development

```bash
cd frontend

# Install dependencies
npm install

# Start dev server (with HMR)
npm run dev

# Run tests
npm test

# Build for production
npm run build
```

The dev server proxies `/api` requests to `http://localhost:7070` (the Military Watcher API).

## Docker

```bash
# Build from project root
docker build -t military-tracker-frontend:latest -f docker/frontend/Dockerfile .
```

The production build is served by Nginx with SPA fallback routing. Nginx proxies `/api/` requests to the Military Watcher API service.

## Features

- Sortable, paginated flight table with real-time auto-refresh (15s)
- Configurable page size (25, 50, 100 rows)
- Right-click context menu on table rows to show flight on map (new tab) or view details
- Flight detail page with full aircraft metadata
- Interactive Leaflet map with fighter aircraft SVG icons rotated by heading
- Geographic bounding box filtering
- Click a flight marker on the map to view its historical track as a polyline
- Deep-link to map with a selected flight via `/map?flight={id}`
