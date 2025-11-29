## Micro-services
### KernelService
Il s’agit du cœur applicatif du système. Ce service regroupe des fonctionnalités communes que nous n’avons pas jugées utiles de spécialiser en micro-services. Nous y retrouverons notamment la gestion des événements, du compte utilisateur et des résultats. Ce service communique en lecture et en écriture à la base de données ainsi qu’avec le cache pour accélérer les lectures fréquentes.
### Micro-services
Ce sont des services que nous avons choisi d’isoler pour des raisons de criticité et/ou d’extensibilité. L’ajout de micro-services facilitera la maintenance du code. La gestion des pannes sur l’application sera également optimisée et permettra à l’application de continuer à fonctionner sans impacter tous les utilisateurs. Voici un détail des micro-services présents sur notre schéma : 
- CryptoService → chiffrement et déchiffrement de documents sensibles(passeport, certificats médicaux…).
- AuthService → gestion des comptes, rôles et tokens JWT.
- MapService → intégration Google Maps et géolocalisation des sportifs.
- PlanningService → gestion des plannings en fichiers ics(commissaire, sportif, bénévole).
- ReportService → génération de rapports et statistiques d’usage pour le responsable de déploiement.
- AlertesService → gestion des alertes et notifications temps réel.



# Endpoints
```
/login
/logout
/signup

/account/ticket → billet
/account/personal-data → visualisation des données personnelles
/account/settings → gestion globale du compte
/account/settings/notifications → gestion des notifications

/agenda -> emploi du temps (commissaire/bénévole/sportif)
```
### SPORTIF
```
/account/athlete/validation → docs pour se faire valider

/my-events
/my-events/forfeit
```
### BENEVOLE
```
/account/volunteer/validation 
```
### COMMISSAIRE
```
/account/superintendant/validation 
/championship/{id}/events/{id}/update
/championship/{id}/events/{id}/delete
/championship/{id}/events/{id}
/championship/{id}/events/{id}/results/update
/championship/{id}/events/{id}/athletes
/championship/{id}/events/{id}/athletes/{id}/update
/championship/{id}/events/{id}/athletes/add
/championship/{id}/super-map
```

### RESPONSABLE DÉPLOIEMENT
```
/championship/create
/championship/{id}/update
/championship/{id}/events/create	
/championship/{id}/events/{id}/update
/championship/{id}/events/{id}/delete
/events/{id}/notifications/create
/agenda/add
/championship/{id}/map/fanzone/add/
/championship/{id}/map/fanzone/{id}/update
/report
/championship/{id}/super-map
```

# Non connecté
```
/public/map -> accès carte
/public/events -> accès épreuves/résultats (pouvoir filtrer sur nom d’athlète/sport)
```