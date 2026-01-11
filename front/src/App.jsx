import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import ListeCompetitions from "./components/ListeCompetitions.jsx";
import Competition from "./components/Competition.jsx";
import ListChampionships from "./components/ListChampionships.jsx";
import PublicMapPage from './components/map/PublicMapPage';
import ListeEvenements from './components/ListeEvenements';
import EventDetails from './components/EventDetails';
import Layout from "./components/Layout.jsx";

function App() {

  return (
      <>
          <div className="app-root">
              <Router>
                  <Routes>
                      <Route path="/" element={<Layout />}>
                          <Route path="/public/championship" element={<ListChampionships/>}/>
                          <Route path="/public/championship/:id/comp" element={<ListeCompetitions/>}/>
                          <Route path="/public/championship/:id/comp/:idComp" element={<Competition/>}/>
                          <Route path="/public/map" element={<PublicMapPage/>}/>
                          <Route path="/public/events" element={<ListeEvenements/>}/>
                          <Route path="/public/events/:id" element={<EventDetails/>}/>
                          <Route path="/public/trials/:id" element={<EventDetails/>}/>
                          <Route path="/" element={<PublicMapPage/>}/>
                      </Route>
                  </Routes>
              </Router>
          </div>
      </>
  )
}

export default App
