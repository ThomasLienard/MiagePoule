import React from 'react';
import { useAuth } from '../../contexts/AuthContext';

const AdminPage = () => {
    const { user } = useAuth();

    return (
        <div className="admin-container">
            <h1>Administration</h1>
            <p>Bienvenue, {user?.email}</p>
            <div className="admin-content">
                {/* Contenu de l'administration */}
                <p>Cette page est accessible uniquement aux administrateurs.</p>
            </div>
        </div>
    );
};

export default AdminPage;