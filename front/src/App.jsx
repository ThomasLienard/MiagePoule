import { BrowserRouter as Router, Route, Routes } from "react-router-dom";

import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './components/auth/LoginPage';
import RegisterPage from './components/auth/RegisterPage';
import ActivateAccountPage from './components/auth/ActivateAccountPage';
import ChangePasswordPage from './components/auth/ChangePasswordPage';
import AdminPage from './components/admin/AdminPage';
import UserManagement from './components/admin/UserManagement';

import ListeCompetitions from "./components/ListeCompetitions.jsx";
import Competition from "./components/Competition.jsx";
import ListChampionships from "./components/ListChampionships.jsx";
import PublicMapPage from './components/map/PublicMapPage';
import ListeEvenementsParCompetition from './components/ListeEvenementsParCompetition';
import EventDetails from './components/EventDetails';
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
                            <Route path="/activate" element={<ActivateAccountPage />} />
                            <Route path="/change-password" element={<ChangePasswordPage />} />
                            <Route path="/public/events/:id" element={<EventDetails />} />
                            <Route path="/public/trials/:id" element={<EventDetails />} />

                            {/* Routes protégées Admin */}
                            <Route path="/admin" element={
                                <ProtectedRoute allowedRoles={['ADMIN']}>
                                    <AdminPage />
                                </ProtectedRoute>
                            } />
                            <Route path="/admin/users" element={
                                <ProtectedRoute allowedRoles={['ADMIN']}>
                                    <UserManagement />
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
                            <Route path="/" element={<PublicMapPage />} />
                        </Route>
                    </Routes>
                </div>
            </Router>
        </AuthProvider>
    )
}

export default App
