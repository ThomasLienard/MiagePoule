import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import ListeEvenements from './components/ListeEvenements';
import EventDetails from './components/EventDetails';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/public/events" element={<ListeEvenements />} />
          <Route path="/public/events/:id" element={<EventDetails />} />
          <Route path="/public/trials/:id" element={<EventDetails />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
