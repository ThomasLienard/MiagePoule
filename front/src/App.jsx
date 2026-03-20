import { BrowserRouter as Router, Route, Routes } from "react-router-dom";

import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './components/auth/LoginPage';
import RegisterPage from './components/auth/RegisterPage';
import ActivateAccountPage from './components/auth/ActivateAccountPage';
import ChangePasswordPage from './components/auth/ChangePasswordPage';
import AdminPage from './components/admin/AdminPage';
import UserManagement from './components/admin/UserManagement';
import ReportingPage from './components/admin/ReportingPage';

import Competition from "./components/Competition.jsx";
import ListChampionships from "./components/ListChampionships.jsx";
import PublicMapPage from './components/map/PublicMapPage';
import TrialsAndEventsDetails from './components/TrialsAndEventsDetails.jsx';
import Layout from "./components/layout/Layout.jsx";
import TicketsPage from './components/tickets/TicketsPage.jsx';
import Profile from "./components/profile/Profile.jsx";
import PrivacySettings from "./components/profile/PrivacySettings.jsx";

// Commissaire components
import AdminEpreuves from './components/commissaire/AdminEpreuves.jsx';
import ManageParticipants from './components/commissaire/ManageParticipants.jsx';
import TeamManagement from './components/commissaire/TeamManagement.jsx';
import ManageResults from './components/commissaire/ManageResults.jsx';

//Admin
import CreateEventPage from "./components/admin/CreateEventPage.jsx";
import CreateChampionshipPage from "./components/admin/CreateChampionshipPage.jsx";
import CreateCompetitionPage from "./components/admin/CreateCompetitionPage.jsx";
import EditEventPage from "./components/admin/EditEventPage.jsx";
import EditChampionshipPage from "./components/admin/EditChampionshipPage.jsx";
import EditCompetitionPage from "./components/admin/EditCompetitionPage.jsx";
import TrialsByAthlete from "./components/TrialsByAthlete.jsx";
import Agenda from "./components/volunteer/Agenda.jsx";

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
                            <Route path="/public/events/:id" element={<TrialsAndEventsDetails />} />
                            <Route path="/public/trials/:id" element={<TrialsAndEventsDetails />} />
                            <Route path="/public/athlete-trials/:athleteId" element={<TrialsByAthlete />} />

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
                            <Route path="/admin/reporting" element={
                                <ProtectedRoute allowedRoles={['ADMIN']}>
                                    <ReportingPage />
                                </ProtectedRoute>
                            } />
                            <Route path="/admin/create-event" element={
                                <ProtectedRoute allowedRoles={['ADMIN']}>
                                    <CreateEventPage />
                                </ProtectedRoute>
                            } />
                            <Route path="/admin/update-event" element={
                                <ProtectedRoute allowedRoles={['ADMIN']}>
                                    <EditEventPage />
                                </ProtectedRoute>
                            } />
                            <Route path="/admin/create-champ" element={
                                <ProtectedRoute allowedRoles={['ADMIN']}>
                                    <CreateChampionshipPage />
                                </ProtectedRoute>
                            } />
                            <Route path="/admin/update-champ" element={
                                <ProtectedRoute allowedRoles={['ADMIN']}>
                                    <EditChampionshipPage />
                                </ProtectedRoute>
                            } />
                            <Route path="/admin/create-comp" element={
                                <ProtectedRoute allowedRoles={['ADMIN']}>
                                    <CreateCompetitionPage />
                                </ProtectedRoute>
                            } />
                            <Route path="/admin/update-comp" element={
                                <ProtectedRoute allowedRoles={['ADMIN']}>
                                    <EditCompetitionPage />
                                </ProtectedRoute>
                            } />

                            {/* Routes protégées Commissaire */}
                            <Route path="/commissaire/trials" element={
                                <ProtectedRoute allowedRoles={['COMMISSAIRE']}>
                                    <AdminEpreuves />
                                </ProtectedRoute>
                            } />
                            <Route path="/commissaire/trials/:trialId/participants" element={
                                <ProtectedRoute allowedRoles={['COMMISSAIRE']}>
                                    <ManageParticipants />
                                </ProtectedRoute>
                            } />
                            <Route path="/commissaire/teams" element={
                                <ProtectedRoute allowedRoles={['COMMISSAIRE']}>
                                    <TeamManagement />
                                </ProtectedRoute>
                            } />
                            <Route path="/commissaire/update-event" element={
                                <ProtectedRoute allowedRoles={['COMMISSAIRE']}>
                                    <EditEventPage />
                                </ProtectedRoute>
                            } />
                            <Route path="/commissaire/trials/:trialId/results" element={
                                <ProtectedRoute allowedRoles={['COMMISSAIRE']}>
                                    <ManageResults />
                                </ProtectedRoute>
                            } />

                            <Route path="/agenda" element={
                                <ProtectedRoute allowedRoles={['VOLONTAIRE']}>
                                    <Agenda />
                                </ProtectedRoute>
                            } />

                            {/* Routes qui nécessitent une connexion */}
                            <Route path="/account" element={
                                <ProtectedRoute>
                                    <Profile />
                                </ProtectedRoute>
                            } />
                            <Route path="/privacy" element={
                                <ProtectedRoute>
                                    <PrivacySettings />
                                </ProtectedRoute>
                            } />

                            <Route path="/tickets" element={
                                <ProtectedRoute>
                                    <TicketsPage />
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
