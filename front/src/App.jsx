import './App.css'
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import ListeCompetitions from "./components/ListeCompetitions.jsx";
import Competition from "./components/Competition.jsx";

function App() {

  return (
    <>
        <Router>
            <Routes>
                <Route path="/public/championship/:id/comp" element={<ListeCompetitions />} />
                <Route path="/championship/:id/comp/:idComp" element={<Competition />}
                />
            </Routes>
        </Router>
    </>
  )
}

export default App
