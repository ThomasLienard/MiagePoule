package com.miage.pouleAPI.entity;

import lombok.Getter;

@Getter
public enum DataCategory {
    CONNECTION_HISTORY("Historique de connexions", "Sécurité et détection d'intrusions", "Privé", true),
    NOTIFICATION_SUBS("Abonnements notifications", "Envoi d'alertes en temps réel", "Privé", false),
    COMPETITION_PREFS("Préférences compétitions", "Personnalisation des contenus", "Public", false);

    private final String label;
    private final String purpose;
    private final String sharingLevel;
    private final boolean isMandatory;

    DataCategory(String label, String purpose, String sharingLevel, boolean isMandatory) {
        this.label = label;
        this.purpose = purpose;
        this.sharingLevel = sharingLevel;
        this.isMandatory = isMandatory;
    }


}