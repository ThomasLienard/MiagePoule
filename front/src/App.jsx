import { BrowserRouter as Router, Route, Routes } from "react-router-dom";

import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './components/auth/LoginPage';
import RegisterPage from './components/auth/RegisterPage';
import AdminPage from './components/admin/AdminPage';

import Competition from "./components/Competition.jsx";
import ListChampionships from "./components/ListChampionships.jsx";
import PublicMapPage from './components/map/PublicMapPage';
import TrialsAndEventsDetails from './components/TrialsAndEventsDetails.jsx';
import Layout from "./components/layout/Layout.jsx";

function App() {

  return (
        <AuthProvider>
            <Router>
                <div className="app-container">
                    <Routes>
                        <Route path="/" element={<Layout />}>
                            {/* Routes publiques */}
                            <Route path="/login" element={<LoginPage />} />
                            <Route path="/register" element={<RegisterPage />} />
                            <Route path="/public/events/:id" element={<TrialsAndEventsDetails />} />
                            <Route path="/public/trials/:id" element={<TrialsAndEventsDetails />} />

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
                            <Route path="/public/championship/:id/comp/:idComp" element={<Competition />}/>

                            {/* Route par défaut */}
                            <Route path="/" element={<PublicMapPage />} />
                        </Route>
                    </Routes>
                </div>
            </Router>
        </AuthProvider>
    )
}

export default App
