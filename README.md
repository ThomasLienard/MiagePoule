# MiagePoule
## Description

Projet MiagePoule - Application de gestion d'evenements sportifs.

## Installation avec Docker
### Lancer l'application

Pour démarrer l'application tout les services (front,nack,BDD) avec Docker, exécutez la commande suivante :

```bash
  docker-compose up
  ou
  docker compose up
```
**Note** : Le téléchargement et le démarrage peuvent prendre un certain temps.

Pour démarer uniquement le service postrgre (ça necessite de lancer les projets manuellement)  : 
```bash
  docker compose -f 'docker-compose.yml' up -d --build 'postgres'
```

### Accès aux services

- Frontend : http://localhost:3000/
    - Pour l'instant, l'interface n'est pas encore fonctionnelle

- Base de données PostgreSQL :
```bash
  psql -h localhost -p 5433 -U miageuser -d miagepoule
```
Mot de passe : miagepassword

### État actuel du projet

- Frontend accessible (développement en cours)

- Backend non fonctionnel pour le moment

- Base de données PostgreSQL opérationnelle

## Maintenance
### Nettoyage Docker

Après utilisation, vous pouvez libérer de l'espace en supprimant tous les conteneurs, images et volumes avec :
bash

```bash
  docker system prune -a --volumes -f
```

Attention : Cette commande supprimera toutes les données non persistantes.
