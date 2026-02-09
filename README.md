# MiagePoule
## Description

Projet MiagePoule - Application de gestion d'evenements sportifs.

## Liens utiles
- Google drive : https://drive.google.com/drive/u/0/folders/1j6wGk7S6pKEYNrGJsizqw5gH3CUtYePK
- Suivi des heures de chacun : https://docs.google.com/spreadsheets/d/12Pm5g05zaR8LFwijVBRfq72DI7AqkXp7ooYLMCOAcBw/edit?gid=0#gid=0

## Installation avec Docker

### Prérequis

#### Windows/MacOS
- Posséder Docker Desktop
- Lancer Docker Desktop

#### Linux
- Posséder Docker

### Lancer l'application

Pour démarrer l'application avec Docker, exécutez la commande suivante à la racine du projet :

```bash
  docker compose up --build
```
ou
```bash
  docker-compose up --build
```

**Note** : Le téléchargement et le démarrage peuvent prendre un certain temps.

### Accès aux services

- Frontend : http://localhost:3001/


### Eteindre l'application
```bash
  docker compose down -v
```
ou
```bash
  docker-compose down -v
```

## Accès pour tester les différents rôles 

- Commissaire :
  - Mail : commissaire@test.com
  - Mot de passe : test123
- Responsable déploiement :
  - Mail : anna@smith.com
  - Mot de passe : test123
- Sportif
  - Mail : athlete@test.com
  - Mot de passe : test123



## État actuel du projet
✅ = Fini
⌛ = En cours dans ce sprint
❌ = Pas encore commencé

- ✅ Fonctionnalité de carte de visualisation des évènements.
- ✅ Création de compte / Connexion.
- ✅ Visualisation des compétitions
- ✅ Visualisation des évènements 
- ✅ Page d'administration des comptes (Responsable déploiement)
- ✅ Gestion du profil
- ✅ Visualisation des résultats d'une épreuve
- ✅ Stockage du billet dans le profil utilisateur
- ⌛ Gestion des notifications
- ⌛ Visualisation des épreuves assignées (Sportif)
- ⌛ Visualisation des résultats des épreuves passées (Sportif)
- ⌛ Visualisation des performances de ses concurrents (Sportif)
- ⌛ Signature de la charte (Sportif)
- ⌛ Création d'un évènement (Responsable déploiement)
- ⌛ Inscription d'un participant à une épreuve (Commissaire)
- ⌛ Forfait à une épreuve (Commissaire)
- ⌛ Dépôt de documents (Bénévole, Commissaire, Sportif)
- ❌ Accès à l'agenda (Bénévole, Commissaire, Sportif)
- ❌ Saisie des résultats (Commissaire)
- ❌ Déclarer forfait (Sportif)
- ❌ Modifier un évènement (Responsable déploiement, Commissaire)
- ❌ Valider le compte d'un sportif (Commissaire)
- ❌ Valider le compte d'un commissaire ou d'un bénévole (Responsable déploiement)
- ❌ Téléverser un agenda (Responsable déploiement)
- ❌ Tracking du sportif
- ❌ Carte agrégée
- ❌ Métrics

## Cas spécifique

Pour démarrer uniquement le service postgres :
```bash
  docker compose -f 'docker-compose.yml' up -d --build 'postgres'
```
(Cela nécessite de lancer les autres services manuellement)

- Base de données PostgreSQL :
```bash
  psql -h localhost -p 5433 -U miageuser -d miagepoule
```
Mot de passe : miagepassword
