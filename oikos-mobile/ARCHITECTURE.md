# Architecture

Application organisée **par fonctionnalité métier** (feature-based), même
principe qu'[oikos-web/ARCHITECTURE.md](../oikos-web/ARCHITECTURE.md) —
cette dernière fait office de guide de référence partagé (flux de
dépendances, gestion des erreurs, tests, nommage) ; ce document décrit
uniquement ce qui est spécifique à Expo/React Native.

## 1. Arborescence

```text
src/
├── app/                     # Bootstrap de l'application
│   ├── App.tsx              # Providers + RootNavigator
│   ├── providers.tsx        # QueryClientProvider (TanStack Query)
│   ├── queryClient.ts
│   ├── store.ts             # Session (Zustand + persist SecureStore/localStorage)
│   └── navigation/
│       ├── RootNavigator.tsx    # Bascule AuthNavigator <-> MainNavigator
│       │                       # selon isAuthenticated ; deep linking (voir §3)
│       ├── AuthNavigator.tsx    # Login, register, forgot/reset password,
│       │                       # onboarding wizard, invitations
│       └── MainNavigator.tsx    # Espace connecté (property-ownership,
│                                # messaging, notifications, me)
│
├── features/
│   ├── identity/
│   │   ├── auth/            # Connexion, mot de passe oublié/reset
│   │   ├── register/        # Inscription copropriétaire
│   │   ├── onboarding/      # Wizard bureau/gérant (state bufferisé, voir
│   │   │                    # oikos-web/ARCHITECTURE.md §9 pour le détail
│   │   │                    # des écritures serveur, identique côté API)
│   │   ├── invitations/     # Acceptation d'invitation
│   │   └── me/              # Profil, avatar, changement de mot de passe
│   │
│   ├── property-ownership/  # "Mes lots", mes échéances, mes paiements,
│   │   │                    # mes demandes d'adhésion — même périmètre que
│   │   │                    # property-ownership côté oikos-web
│   │   ├── units/
│   │   ├── installments/
│   │   ├── payments/
│   │   └── membership-requests/
│   │
│   ├── messaging/           # Conversations, BOARD_PRIVATE
│   └── notifications/       # Écran, badge, enregistrement du push token
│
├── shared/
│   ├── api/httpClient.ts    # Instance Axios + intercepteurs JWT/refresh
│   ├── components/          # Button, Input, ControlledInput, Card, Badge,
│   │                        # Select, Stepper, EmptyState, Loader, Alert
│   ├── constants/
│   ├── layouts/
│   ├── theme/colors.ts      # Palette TailAdmin portée en hex pour StyleSheet
│   ├── types/
│   └── utils/
│
└── config/env.ts            # Lecture de EXPO_PUBLIC_API_URL
```

Chaque feature suit la même subdivision que côté web (`api/`, `components/`,
`hooks/`, `screens/` — `pages/` devient `screens/` en RN — `schemas/`,
`types/`), avec un `index.ts` comme point d'entrée public.

## 2. Différences avec oikos-web (spécifique React Native)

- **Formulaires** — React Hook Form `{...register()}` (non-contrôlé, basé
  sur des refs DOM) ne fonctionne pas sur les `TextInput` RN : chaque champ
  passe par `<Controller>`, encapsulé dans
  `shared/components/Input/ControlledInput.tsx` plutôt que répété dans
  chaque écran.
- **Navigation** — React Navigation (stacks natifs) au lieu de React
  Router. `RootNavigator` bascule entre `AuthNavigator` et `MainNavigator`
  selon `useAuthStore().isAuthenticated` ; les deux arbres sont mutuellement
  exclusifs (jamais montés simultanément).
- **Deep linking** — les liens d'email (vérification, activation,
  acceptation d'invitation, reset password) sont déclarés dans le `linking`
  config de `RootNavigator`, avec le schéma `oikos://` en plus du préfixe
  Expo. Voir le commentaire en tête de ce fichier pour la limite connue
  (un lien tapé une fois déjà connecté ne résout que contre
  `AuthNavigator`).
- **Session/tokens** — `expo-secure-store` (Keychain iOS / Keystore
  Android) au lieu de `localStorage` ; fallback `localStorage` uniquement
  sur la cible web d'Expo. Mêmes rôles décodés du JWT, même géométrie de
  store que `oikos-web/src/app/store.ts` (voir commentaires dans
  `src/app/store.ts`).
- **Style** — pas de Tailwind : la palette `oikos-web/src/index.css`
  (TailAdmin) est portée en constantes hex dans `shared/theme/colors.ts`,
  consommées via `StyleSheet.create`. Pas (encore) de dark mode côté mobile.
- **Notifications push** — `expo-notifications` ; le token de l'appareil
  est enregistré côté API après connexion (voir
  `features/notifications/api`). Limité dans Expo Go depuis le SDK 53 : un
  *development build* est nécessaire pour tester le push réel (voir
  `README.md`).

## 3. Ce qui est identique à oikos-web

- Flux de dépendances `Screen → Component → Hook → API →
  shared/api/httpClient`.
- Intercepteur Axios pour le header `Authorization` et le refresh
  automatique sur `401` (même logique, client de refresh dédié pour éviter
  la boucle).
- Schémas Zod alignés sur les contraintes Bean Validation de l'API.
- Découpage `property-ownership` vs `property-mngt` (ce dernier hors
  périmètre mobile, voir `README.md`).
- Nommage en anglais dans le code, français réservé aux textes utilisateur.
