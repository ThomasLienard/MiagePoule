import './App.css'
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";

import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Header from './components/layout/Header';
import LoginPage from './components/auth/LoginPage';
import RegisterPage from './components/auth/RegisterPage';
import AdminPage from './components/admin/AdminPage';

import ListeCompetitions from "./components/ListeCompetitions.jsx";
import Competition from "./components/Competition.jsx";
import ListChampionships from "./components/ListChampionships.jsx";
import PublicMapPage from './components/map/PublicMapPage';
import ListeEvenements from './components/ListeEvenements';
import ListeEvenementsParCompetition from './components/ListeEvenementsParCompetition';
import EventDetails from './components/EventDetails';
import Layout from "./components/Layout.jsx";
import EventsMapView from "./components/EventsMapView.jsx";

function App() {

  return (
        <AuthProvider>
            <Router>
                <Header />
                <div className="app-container">
                    <Routes>
                        <Route path="/" element={<Layout />}>
                            {/* Routes publiques */}
                            <Route path="/login" element={<LoginPage />} />
                            <Route path="/register" element={<RegisterPage />} />
                            <Route path="/public/events/:id" element={<EventDetails />} />
                            <Route path="/public/trials/:id" element={<EventDetails />} />

                            {/* Routes protégées */}
                            <Route path="/admin" element={
                                <ProtectedRoute allowedRoles={['ADMIN']}>
                                    <AdminPage />
                                </ProtectedRoute>
                            } />

                            {/* Routes qui nécessitent une connexion */}
                            <Route path="/profile" element={
                                <ProtectedRoute>
                                    {/* Composant Profile à créer */}
                                    <div>Profil utilisateur</div>
                                </ProtectedRoute>
                            } />

                            {/* Routes publiques existantes */}
                            <Route path="/public/championship" element={<ListChampionships />} />
                            <Route path="/public/championship/:id/comp" element={<ListeCompetitions />} />
                            <Route path="/public/championship/:id/comp/:idComp" element={<Competition />}/>
                            <Route path="/public/championship/:championshipId/comp/:competitionId/events"
                                   element={<ListeEvenementsParCompetition />} />

                            {/* Route par défaut */}
                            <Route path="/" element={<EventsMapView />} />
                        </Route>
                    </Routes>
                </div>
            </Router>
        </AuthProvider>
    )
}

export default App
