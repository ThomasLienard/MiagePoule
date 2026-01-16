import './App.css'
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import ListeCompetitions from "./components/ListeCompetitions.jsx";
import Competition from "./components/Competition.jsx";
import ListChampionships from "./components/ListChampionships.jsx";
import PublicMapPage from './components/map/PublicMapPage';
import ListeEvenements from './components/ListeEvenements';
import ListeEvenementsParCompetition from './components/ListeEvenementsParCompetition';
import EventDetails from './components/EventDetails';
import EventsMapView from './components/EventsMapView';
import './App.css';

function App() {

  return (
    <>
        <div className="app-root">
            <Router>
                <Routes>
                    <Route path="/public/championship" element={<ListChampionships />} />
                    <Route path="/public/championship/:id/comp" element={<ListeCompetitions />} />
                    <Route path="/public/championship/:id/comp/:idComp" element={<Competition />}/>
                    <Route path="/public/championship/:championshipId/comp/:competitionId/events" element={<ListeEvenementsParCompetition />} />
                    <Route path="/public/map" element={<PublicMapPage />} />
                    <Route path="/public/events" element={<EventsMapView />} />
                    <Route path="/public/events/:id" element={<EventDetails />} />
                    <Route path="/public/trials/:id" element={<EventDetails />} />
                    <Route path="/" element={<EventsMapView />} />

                </Routes>
            </Router>
        </div>
    </>
  )
}

export default App
