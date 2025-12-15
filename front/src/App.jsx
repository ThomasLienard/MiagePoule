import './App.css'
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import ListeCompetitions from "./components/ListeCompetitions.jsx";
import Competition from "./components/Competition.jsx";
import ListChampionships from "./components/ListChampionships.jsx";

function App() {

  return (
    <>
        <Router>
            <Routes>
                <Route path="/public/championship" element={<ListChampionships />} />
                <Route path="/public/championship/:id/comp" element={<ListeCompetitions />} />
                <Route path="/public/championship/:id/comp/:idComp" element={<Competition />}
                />
            </Routes>
        </Router>
    </>
  )
}

export default App
