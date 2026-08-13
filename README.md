# Oikos

Monorepo de gestion de copropriété : une API et deux clients.

| Solution | Rôle | Doc |
|---|---|---|
| [`oikos-api`](oikos-api/) | Backend Spring Boot (Java 25) — source de vérité métier, REST + PostgreSQL/Flyway | [README](oikos-api/README.md) · [ARCHITECTURE](oikos-api/ARCHITECTURE.md) |
| [`oikos-web`](oikos-web/) | Client web (React/Vite) — back-office gérant/syndic (`property-mngt`) + espace copropriétaire | [README](oikos-web/README.md) · [ARCHITECTURE](oikos-web/ARCHITECTURE.md) |
| [`oikos-mobile`](oikos-mobile/) | Client mobile (Expo/React Native) — espace copropriétaire/consultation uniquement | [README](oikos-mobile/README.md) · [ARCHITECTURE](oikos-mobile/ARCHITECTURE.md) |

Voir [ARCHITECTURE.md](ARCHITECTURE.md) pour la vue système (comment les
trois communiquent) et le détail de l'organisation du repo.

## Démarrage rapide (dev local)

```bash
# Terminal 1 — API (H2 en mémoire, profil dev, pas besoin de Postgres)
cd oikos-api && ./mvnw spring-boot:run

# Terminal 2 — Web
cd oikos-web && npm install && npm run dev

# Terminal 3 — Mobile (Expo Go, voir oikos-mobile/README.md)
cd oikos-mobile && npm install && npm start
```

## Docker / recette

Environnement prod-like (images buildées, pas de hot-reload) pour la
première mise en recette : `oikos-api` + `oikos-web` conteneurisés +
PostgreSQL réel. `oikos-mobile` reste hors Docker (Expo Go/EAS, voir
`oikos-mobile/README.md`).

```bash
cp .env.example .env                    # identifiants Postgres + URL web
cp oikos-api/.env.example oikos-api/.env  # secrets applicatifs (JWT_SECRET,
                                            # MAIL_USERNAME/MAIL_PASSWORD, ...)
# éditer les deux .env - voir les commentaires de chaque fichier

docker compose up --build
```

Ce même `docker-compose.yml` reste le flux local (build en direct) — pas de
nouveau profil Spring ni de nouveaux Dockerfiles.

Sur le vrai VPS de recette (`oikos-staging.tech`), les images ne sont **pas**
buildées sur place (RAM limitée du serveur) : `docker-compose.staging.yml`
surcharge `api`/`web` avec les images pré-buildées par GitHub Actions
(`.github/workflows/deploy-staging.yml`, poussées sur GHCR) et ajoute un
reverse proxy Caddy (HTTPS automatique). Déploiement déclenché à chaque push
sur `main` :

```bash
docker compose -f docker-compose.yml -f docker-compose.staging.yml up -d
```

Les `.env` du VPS sont créés une fois manuellement sur le serveur (jamais
via CI) et contiennent les vraies valeurs (secrets, `https://oikos-staging.tech/...`
à la place de `localhost`).

- Web : http://localhost:8082
- API : http://localhost:8080 (Swagger : http://localhost:8080/swagger-ui.html)
- Comptes de test disponibles dès le premier démarrage (mêmes que
  `oikos-api/src/main/resources/db/dev/dev.sql`) : `admin@oikos.com` et
  `user1@oikos.com` à `user8@oikos.com`, mot de passe `Oikos@2026` pour
  tous.
- Repartir d'une base vide : `docker compose down -v` puis `docker compose
  up --build` (le seed ne se réinsère proprement que sur un volume vierge —
  un redémarrage sans `-v` redémarre sans planter mais ne réinsère pas les
  doublons).

## Documentation par solution

Chaque solution porte son propre `README.md` (périmètre, démarrage) et
`ARCHITECTURE.md` (organisation du code, conventions). `oikos-mobile`
porte en plus `AGENTS.md`/`CLAUDE.md` : instructions opérationnelles pour
un agent IA (ex. contraintes de version Expo), un canal séparé de la
documentation humaine.
