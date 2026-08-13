# Oikos Mobile

Application mobile (Expo / React Native) pour Oikos, API de gestion de
copropriété. Clone architectural délibéré d'`oikos-web` : mêmes
bibliothèques (TanStack Query, React Hook Form + Zod, Zustand, Axios), même
organisation feature-based.

## 1. Périmètre de cette version

Espace copropriétaire/consultation, porté depuis `oikos-web` :

- `identity` — inscription, connexion, mot de passe oublié, wizard
  onboarding bureau/gérant, invitations, profil.
- `property-ownership` — mes lots, mes échéances, mes paiements, mes
  demandes d'adhésion.
- `messaging` — conversations, messagerie `BOARD_PRIVATE`.
- `notifications` — écran, badge, notifications push (Expo).

Hors périmètre pour cette version : `property-mngt` (back-office
gérant/syndic — accounting, board-members, documents, pricing, etc.), qui
reste sur `oikos-web`. Voir [ARCHITECTURE.md](ARCHITECTURE.md) pour le
détail de l'organisation du code.

## 2. Stack technique

Expo (SDK 57), React Native, TypeScript, React Navigation, TanStack Query,
Zustand, Axios, React Hook Form, Zod, `expo-secure-store` (tokens),
`expo-notifications` (push), `expo-image-picker`.

## 3. Démarrage rapide

Prérequis : `oikos-api` doit tourner sur `http://localhost:8080` (profil
`dev`, voir `oikos-api/README.md`).

```bash
npm install
npm run start   # ouvre le QR code Expo Go, ou npm run ios / npm run android
```

Tester sur un iPhone physique : installer **Expo Go** depuis l'App Store,
scanner le QR code affiché par `npm start` (même réseau Wi-Fi que le Mac).
Pour tester de vraies notifications push (limitées dans Expo Go depuis le
SDK 53) ou du code natif custom, utiliser un **development build** :

```bash
npx expo install expo-dev-client
npx eas-cli build --profile development --platform ios
```

(`eas-cli` doit être appelé explicitement — `npx eas ...` seul résout un
paquet npm sans rapport nommé `eas`, pas `eas-cli`.)

Variable d'environnement : `EXPO_PUBLIC_API_URL` — `.env.development` (dev),
`.env.staging` (profil EAS `preview`, URL réelle `https://oikos-staging.tech`),
`.env.production` (profil EAS `production`, voir `eas.json`). Ni l'un ni
l'autre n'est encore câblé dans `eas.json` (aucune valeur EAS_ENV/env
spécifique par profil pour l'instant) — `.env.production` garde en plus une
URL placeholder (TODO), le domaine de prod n'étant pas encore connu (voir
`.env.example`).

## 4. Fichiers agent

`AGENTS.md`/`CLAUDE.md` à la racine contiennent des instructions
opérationnelles pour un agent IA (ex. version d'Expo à vérifier avant
d'écrire du code) — un canal séparé de ce README, non destiné à un lecteur
humain.

## 5. Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) — organisation du code, conventions
  partagées avec `oikos-web`.
- Ce README — périmètre et démarrage rapide.
