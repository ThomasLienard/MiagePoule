import { useState } from 'react'
import './App.css'
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import ListeCompetitions from "./components/ListeCompetitions.jsx";

function App() {

  return (
    <>
        <Router>
            <Routes>
                <Route path="/public/championships/:id/competitions" element={<ListeCompetitions />} />
            </Routes>
        </Router>
    </>
  )
}

export default App
