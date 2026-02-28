import { useState } from 'react';
import type { GeoBoxRequest } from '../types/flight';

interface GeoBoxSelectorProps {
  onSearch: (box: GeoBoxRequest) => void;
}

function GeoBoxSelector({ onSearch }: GeoBoxSelectorProps) {
  const [north, setNorth] = useState<number>(50);
  const [south, setSouth] = useState<number>(25);
  const [east, setEast] = useState<number>(-65);
  const [west, setWest] = useState<number>(-125);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSearch({ north, south, east, west });
  };

  return (
    <div className="card">
      <div className="card-title">Geo Box Filter</div>
      <form className="geobox-form" onSubmit={handleSubmit}>
        <div className="geobox-field">
          <label htmlFor="north">North</label>
          <input
            id="north"
            type="number"
            step="any"
            value={north}
            onChange={(e) => setNorth(Number(e.target.value))}
          />
        </div>
        <div className="geobox-field">
          <label htmlFor="south">South</label>
          <input
            id="south"
            type="number"
            step="any"
            value={south}
            onChange={(e) => setSouth(Number(e.target.value))}
          />
        </div>
        <div className="geobox-field">
          <label htmlFor="east">East</label>
          <input
            id="east"
            type="number"
            step="any"
            value={east}
            onChange={(e) => setEast(Number(e.target.value))}
          />
        </div>
        <div className="geobox-field">
          <label htmlFor="west">West</label>
          <input
            id="west"
            type="number"
            step="any"
            value={west}
            onChange={(e) => setWest(Number(e.target.value))}
          />
        </div>
        <button type="submit" className="btn btn-primary">
          Search
        </button>
      </form>
    </div>
  );
}

export default GeoBoxSelector;
