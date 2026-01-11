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
  docker compose up
```
ou
```bash
  docker-compose up
```

**Note** : Le téléchargement et le démarrage peuvent prendre un certain temps.

Pour démarrer uniquement le service postgres : 
```bash
  docker compose -f 'docker-compose.yml' up -d --build 'postgres'
```
(Cela nécessite de lancer les autres services manuellement)

### Accès aux services

- Frontend : http://localhost:3000/


- Base de données PostgreSQL :
```bash
  psql -h localhost -p 5433 -U miageuser -d miagepoule
```
Mot de passe : miagepassword

### État actuel du projet

- Base de données PostgreSQL opérationnelle.
- Carte accessible avec les points de rendez-vous.
- Liste des compétitions visibles.
- Liste des épreuves visibles.
- Détails des épreuves visibles.

## Maintenance
### Nettoyage Docker

Après utilisation, vous pouvez libérer de l'espace en supprimant tous les conteneurs, images et volumes avec :
bash

```bash
  docker system prune -a --volumes -f
```

Attention : Cette commande supprimera toutes les données non persistantes.
