import { Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import FlightsPage from './pages/FlightsPage';
import FlightDetailPage from './pages/FlightDetailPage';
import MapPage from './pages/MapPage';

function App() {
  return (
    <div className="app">
      <Header />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<FlightsPage />} />
          <Route path="/flight/:id" element={<FlightDetailPage />} />
          <Route path="/map" element={<MapPage />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
