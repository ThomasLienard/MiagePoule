import React from "react";
import "../styles/Competition.css";

const Competition = ({ competition }) => {
    const { nom, dateDebut, dateFin, lieu, description, championnat } = competition;
    return (
        <div className="competition">
            <h3>{nom}</h3>
            <h4> {description}</h4>
            <p><strong>Date de début:</strong> {formatDate(dateDebut)}</p>
            <p><strong>Date de fin:</strong> {formatDate(dateFin)}</p>
            <p><strong>Lieu:</strong> {lieu}</p>
            <p><strong>Championnat associé :</strong> {championnat}</p>
        </div>
    );
};

function formatDate(dateStr) {
    const [year, month, day] = dateStr.split("-");
    return `${day}/${month}/${year}`;
}


export default Competition;
