# Oikos Web

Interface web (mobile first) pour Oikos, API de gestion de copropriété.

## 1. Périmètre de cette première version

Livrable volontairement réduit : le parcours **création d'une copropriété**.

- Connexion (`/login`) — le endpoint `POST /properties` de l'API est réservé
  à `ROLE_ADMIN`, une authentification est donc un prérequis incompressible.
- Liste des copropriétés (`/properties`), paginée.
- Création d'une copropriété (`/properties/new`) — nom, adresse, et le
  premier immeuble (nom + nombre d'étages), conformément à la règle métier
  de l'API : une copropriété est créée avec son premier immeuble dans la
  même transaction (`CreatePropertyService`, côté `oikos-api`).

Hors périmètre pour cette version (voir `oikos-api/README.md` pour la
vision produit complète) : gestion des immeubles/lots au-delà de la
création initiale, copropriétaires, membres du syndic, inscription de
compte, mot de passe oublié.

## 2. Stack technique

React 19, TypeScript, Vite, React Router, TanStack Query, Zustand, Axios,
React Hook Form, Zod, Tailwind CSS v4, ESLint, Prettier, Vitest, Testing
Library. Voir [ARCHITECTURE.md](ARCHITECTURE.md) pour le détail de
l'organisation du code.

## 3. Démarrage rapide

Prérequis : `oikos-api` doit tourner sur `http://localhost:8080` (profil
`dev`, voir `oikos-api/README.md`).

```bash
npm install
npm run dev
```

L'application est servie sur `http://localhost:5173`. En développement, le
serveur Vite proxifie les appels `/api/*` vers `http://localhost:8080` (voir
`vite.config.ts`) : aucune configuration CORS n'est nécessaire côté API.

Pour un environnement prod-like (build buildé, servi par nginx, Postgres
réel) plutôt que `npm run dev`, voir
[../README.md](../README.md#docker--recette) à la racine du repo.

### Compte de connexion (dev)

En profil `dev`, l'API seed un compte `ROLE_ADMIN` (voir
`oikos-api/src/main/resources/db/dev/dev.sql`) rattaché au contact
`oikos@architek.com` — utiliser cet email comme identifiant de connexion.

### Autres commandes

```bash
npm run build         # build de production (tsc -b && vite build)
npm run preview       # sert le build de production en local
npm run lint          # ESLint
npm run format        # Prettier (écrit les corrections)
npm run format:check  # Prettier (vérifie sans écrire)
npm run test          # Vitest (mode CI, une seule passe)
npm run test:watch    # Vitest en mode watch
```

### Variables d'environnement

Voir `.env.example`. `VITE_API_URL`, selon le mode Vite (`npm run build --
--mode <mode>`) :

- développement (`.env.development`, mode par défaut de `npm run dev`) :
  `/api/v1`, relatif, résolu par le proxy Vite ci-dessus.
- recette (`.env.recette`) : origine réelle de l'API de recette — TODO,
  domaine pas encore connu (voir le fichier).
- production (`.env.production`) : origine réelle de l'API de prod — TODO,
  domaine pas encore connu (voir le fichier).

Pas de proxy Vite en dehors du développement : ces deux derniers modes
doivent pointer vers une origine absolue.

## 4. Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) — organisation du code, conventions,
  choix techniques.
- Ce README — périmètre fonctionnel et démarrage.
- `../oikos-api/README.md` / `../oikos-api/ARCHITECTURE.md` — architecture
  et vision produit du backend.

Les deux README (celui-ci et celui d'`oikos-api`) et leurs `ARCHITECTURE.md`
respectifs sont complémentaires et doivent être tenus à jour à chaque
évolution notable du périmètre.
