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

### Simulateur iOS via Xcode (development build local)

Alternative à EAS pour tester en local sur le simulateur iOS de Xcode, sans
passer par le cloud EAS. Prérequis : Xcode installé (avec un simulateur iOS
téléchargé) et CocoaPods (`brew install cocoapods`).

```bash
# 1. Démarrer oikos-api en profil dev (voir oikos-api/README.md)

# 2. Build natif + installation sur le simulateur (première fois seulement,
#    ~5-15 min : génère ios/, pod install, xcodebuild)
npx expo run:ios

# 3. Lancements suivants : juste relancer Metro (le build natif est déjà
#    installé, inutile de repasser par `expo run:ios`)
NODE_OPTIONS=--dns-result-order=ipv4first npx expo start --dev-client --localhost
# puis appuyer sur `i` dans le terminal pour rouvrir l'app sur le simulateur
```

`NODE_OPTIONS=--dns-result-order=ipv4first` contourne un bug connu de
Node 18+ sur macOS : sans ça, `--localhost` fait écouter Metro uniquement en
IPv6 (`::1`), et le simulateur (qui appelle l'URL IPv4 explicite
`127.0.0.1:8081`) échoue avec *"Could not connect to the server"*. Sans
`--localhost` du tout, Metro peut aussi choisir une IP LAN incorrecte
(interface VPN/virtuelle) plutôt que le vrai Wi-Fi, d'où le choix de forcer
localhost pour le simulateur (qui partage le réseau du Mac, contrairement à
un device physique).

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

Arrût du simulateur 
xcrun simctl shutdown 8A63D5F0-7865-443D-8E2E-73415C67401E
xcrun simctl boot 8A63D5F0-7865-443D-8E2E-73415C67401E
open -a Simulator

NODE_OPTIONS=--dns-result-order=ipv4first npx expo start --dev-client --localhost
