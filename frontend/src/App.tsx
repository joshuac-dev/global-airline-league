import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import Home from './pages/Home';
import World from './pages/World';
import Airports from './pages/Airports';
import Airline from './pages/Airline';
import RoutesPage from './pages/Routes';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <div className="app">
        <nav className="nav">
          <Link to="/">Home</Link>
          <Link to="/world">World</Link>
          <Link to="/airports">Airports</Link>
          <Link to="/routes">Routes</Link>
        </nav>
        <main className="main">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/world" element={<World />} />
            <Route path="/airports" element={<Airports />} />
            <Route path="/airline/:id" element={<Airline />} />
            <Route path="/routes" element={<RoutesPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;

