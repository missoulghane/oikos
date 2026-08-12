# Architecture — vue système

## 1. Composants et communication

```text
oikos-web  ──┐
             ├──HTTP/JSON (REST, JWT Bearer)──▶  oikos-api  ──▶  PostgreSQL (Flyway)
oikos-mobile─┘
```

`oikos-api` est l'unique source de vérité métier et de données ; les deux
clients ne font que consommer son API REST (`/api/v1/...`), authentifiée
par JWT (access + refresh token, voir `oikos-api/README.md` §1). Aucune
communication directe entre `oikos-web` et `oikos-mobile`.

- **`oikos-web`** couvre le périmètre produit complet : back-office
  gérant/syndic (`property-mngt`) et espace copropriétaire
  (`property-ownership`).
- **`oikos-mobile`** ne couvre que l'espace copropriétaire/consultation
  (`identity`, `property-ownership`, `messaging`, `notifications`) — le
  back-office reste web-only pour l'instant (tableaux denses, formulaires
  complexes peu adaptés à un écran mobile).

Les deux clients sont des clones architecturaux délibérés : mêmes
bibliothèques (TanStack Query, React Hook Form + Zod, Zustand, Axios), même
découpage feature-based, mêmes conventions de nommage — voir
`oikos-web/ARCHITECTURE.md` (guide de référence) et
`oikos-mobile/ARCHITECTURE.md` (écarts spécifiques à React Native).

## 2. Organisation du repo

```text
oikos/
├── oikos-api/            # Backend Spring Boot (+ Dockerfile, .dockerignore)
├── oikos-web/             # Client web React/Vite (+ Dockerfile, .dockerignore, docker/nginx.conf)
├── oikos-mobile/           # Client mobile Expo/React Native (pas de Docker, voir §4)
├── docker-compose.yml       # Stack recette : postgres + flyway + api + web (voir §4)
├── .env.example              # Variables du docker-compose (copier en .env, jamais commité)
├── .gitignore                  # Règles pour les trois solutions (patterns non
├── .gitattributes                # ancrés = valables à toute profondeur) + racine
├── debug.sh                        # Lance api+web en dev natif (hors Docker), débogueur :5005
├── README.md                         # Vue d'ensemble — voir aussi ce fichier
└── ARCHITECTURE.md                     # Ce fichier
```

Un seul `.gitignore`/`.gitattributes` à la racine couvre les trois
solutions (sections dédiées par sous-projet) plutôt qu'un fichier dupliqué
par dossier — les patterns Git sans `/` en tête s'appliquent à toute
profondeur, donc rien n'est perdu fonctionnellement en les centralisant.

## 3. Base de données

`oikos-api/src/main/resources/db/migration/` ne contient qu'une seule
migration Flyway, `V1__baseline.sql` : le schéma complet actuel. Aucun
déploiement en production n'a eu lieu à ce jour, donc aucun historique de
migration à préserver — aux prochaines évolutions de schéma, reprendre une
numérotation `V2`, `V3`, ... normale à partir de ce baseline.

## 4. Docker / recette

```text
                     ┌──────────┐
                     │ postgres │
                     └────┬─────┘
                          │ (healthy)
                     ┌────▼────┐
                     │ flyway  │  one-shot, migre puis s'arrête (exit 0)
                     └────┬────┘
                          │ (completed)
                     ┌────▼────┐        ┌─────┐
                     │   api   │◀───────│ web │  (navigateur → localhost:8080 direct,
                     └─────────┘        └─────┘   pas de proxy réseau interne)
```

`oikos-api`/`oikos-web` sont conteneurisés (voir leurs `Dockerfile`),
`oikos-mobile` ne l'est pas (Expo Go/EAS, pas un serveur). Particularité à
connaître : la migration Flyway tourne comme **service à part** (`flyway`,
image officielle, one-shot) avant que `api` ne démarre, plutôt que via le
mécanisme automatique de Spring Boot au lancement de l'application — dans
cette combinaison Spring Boot 4 / jar packagé, l'ordonnancement automatique
« Flyway avant validation Hibernate » ne se déclenche pas de façon fiable
(constaté empiriquement, absent en `spring-boot:run`/tests Maven). Le
profil Spring `docker` désactive donc `spring.flyway.enabled` et se
contente de `ddl-auto: validate` contre le schéma déjà migré. Voir
`docker-compose.yml` et `oikos-api/src/main/resources/application-docker.yml`.

Détail des variables, commandes et comptes de test : `README.md` (racine),
section "Docker / recette".


