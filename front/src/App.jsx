// App.jsx
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import PublicMapPage from './PublicMapPage';

function App() {
    return (
        <BrowserRouter>
            <header>
                <nav>
                    <Link to="/">Accueil</Link> |{" "}
                    <Link to="/public/map">Carte des événements</Link>
                </nav>
            </header>

            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/public/map" element={<PublicMapPage />} />
            </Routes>
        </BrowserRouter>
    );
}

function Home() {
    return <h1>Bienvenue sur l’application</h1>;
}

export default App;
