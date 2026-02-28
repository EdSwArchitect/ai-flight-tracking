import { NavLink } from 'react-router-dom';

function Header() {
  return (
    <header className="header">
      <div className="header-title">Military Aircraft Tracker</div>
      <nav className="header-nav">
        <NavLink
          to="/"
          className={({ isActive }) => (isActive ? 'active' : '')}
          end
        >
          Flights
        </NavLink>
        <NavLink
          to="/map"
          className={({ isActive }) => (isActive ? 'active' : '')}
        >
          Map
        </NavLink>
      </nav>
    </header>
  );
}

export default Header;
