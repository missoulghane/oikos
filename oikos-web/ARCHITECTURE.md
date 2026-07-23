# Architecture

Application organisée **par fonctionnalité métier** (feature-based), suivant
[../react_best_practices.md](../react_best_practices.md). Ce document décrit
comment ces règles sont appliquées concrètement dans `oikos-web`.

## 1. Arborescence

```text
src/
├── app/                  # Bootstrap de l'application
│   ├── App.tsx           # ErrorBoundary + Providers + Router
│   ├── providers.tsx     # QueryClientProvider (TanStack Query)
│   └── store.ts          # Store global (session utilisateur — Zustand)
│
├── router/               # Routage, séparé public/privé
│   ├── publicRoutes.tsx  # /login
│   ├── privateRoutes.tsx # /properties, /properties/new (sous AppLayout)
│   ├── ProtectedRoute.tsx
│   └── index.tsx
│
├── features/
│   ├── identity/          # Comptes et accès, transverse au métier property
│   │   ├── auth/          # Connexion
│   │   │   ├── api/       # Appels HTTP (login)
│   │   │   ├── components/ # LoginForm (présentation)
│   │   │   ├── hooks/     # useLogin (mutation + orchestration)
│   │   │   ├── pages/     # LoginPage (composition d'écran)
│   │   │   ├── schemas/   # Validation Zod
│   │   │   ├── types/
│   │   │   └── index.ts   # Point d'entrée public de la feature
│   │   │
│   │   └── register/      # Inscription, activation de compte
│   │       ├── api/ / components/ / hooks/ / pages/ / schemas/ / types/
│   │       └── index.ts
│   │
│   └── property-mngt/      # Coeur métier copropriété
│       ├── properties/     # Structure : copropriétés, immeubles, lots, copropriétaires
│       │   ├── api/        # getProperties, createProperty, getUnits, ...
│       │   ├── components/ # PropertyCard, PropertyList, CreatePropertyForm, ...
│       │   ├── hooks/      # useProperties, useCreateProperty, ...
│       │   ├── pages/      # PropertiesPage, CreatePropertyPage, ...
│       │   ├── schemas/
│       │   ├── types/
│       │   └── index.ts
│       └── accounting/        # Gestion financière (comptes, échéances, appels de
│                            # cotisation) — pas encore créé : ce qui existe
│                            # aujourd'hui (échéances d'un lot) vit encore dans
│                            # `properties/`, à extraire dans une passe dédiée.
│
├── shared/
│   ├── api/httpClient.ts       # Instance Axios + intercepteurs JWT/refresh
│   ├── components/             # Button, Input, Card, Loader, EmptyState,
│   │                           # Alert, Pagination, ErrorBoundary
│   ├── constants/queryKeys.ts  # Clés TanStack Query centralisées
│   ├── layouts/                # AppLayout (zone privée), AuthLayout
│   ├── pages/                  # NotFoundPage (404), ServerErrorPage (500)
│   ├── types/                  # ApiErrorBody (forme des erreurs de l'API)
│   └── utils/getErrorMessage.ts
│
├── config/env.ts       # Lecture/validation des variables d'environnement
├── test/setup.ts        # Setup Vitest (jest-dom)
└── main.tsx
```

Les features sont regroupées par domaine métier : `identity` (`auth`,
`register`) pour les comptes et l'accès, `property-mngt` (`properties`,
`accounting`) pour le coeur métier copropriété. Le périmètre réduit de cette
version ne justifie pas encore d'autres groupes (`dashboard`, etc.)
évoqués dans le guide.

## 2. Flux de dépendances

`Page → Container/Component → Hook → API → shared/api/httpClient`, comme
préconisé par le guide (§2). Concrètement pour la création d'une
copropriété :

```text
CreatePropertyPage
    ↓ (passe onSubmit + isSubmitting)
CreatePropertyForm            (présentation, React Hook Form + Zod)
    ↓ (au submit)
useCreateProperty()           (hook métier, useMutation)
    ↓
createProperty()              (features/property-mngt/properties/api, appel HTTP)
    ↓
httpClient.post('/properties')
```

- Les composants de `components/` ne connaissent ni Axios ni TanStack Query.
- Toute logique métier (mutations, invalidation de cache) vit dans
  `hooks/`, jamais dans les composants de présentation.
- Aucun `fetch`/`axios` direct dans un composant : tout passe par
  `features/<feature>/api/*.ts`, qui utilise l'instance partagée
  `shared/api/httpClient.ts`.

## 3. Authentification et sécurité des appels API

`shared/api/httpClient.ts` centralise :

- l'ajout du header `Authorization: Bearer <accessToken>` sur chaque requête
  (token lu depuis `app/store.ts`) ;
- le rafraîchissement automatique du token sur une réponse `401` (appel
  `POST /auth/refresh-token` via un client Axios dédié, sans intercepteur,
  pour éviter une boucle si le refresh échoue lui-même) ; la requête
  d'origine est rejouée une fois le nouveau token obtenu ;
  si le refresh échoue, la session est effacée et l'utilisateur redirigé
  vers `/login`.

La session (`accessToken`, `refreshToken`, `isAuthenticated`) est le seul
état global de l'application (§6 du guide : "utilisateur connecté" est un
des rares cas légitimes de store global) et vit dans `app/store.ts`
(Zustand + persist en `localStorage`). `router/ProtectedRoute.tsx` s'appuie
dessus pour protéger `/properties` et `/properties/new`.

## 4. Validation et alignement avec l'API

Les schémas Zod de chaque feature (`schemas/*.ts`) reflètent volontairement
les contraintes Bean Validation du contrôleur Spring correspondant, pour
donner un retour immédiat côté client avant l'aller-retour réseau :

| Champ (API `CreatePropertyRequest`) | Contrainte backend        | Contrainte front (`createPropertySchema`) |
|--------------------------------------|---------------------------|--------------------------------------------|
| `name`                               | `@NotBlank @Size(max=100)` | requis, max 100                            |
| `address`                             | `@NotBlank @Size(max=250)` | requis, max 250                            |
| `firstBuildingName`                   | `@NotBlank @Size(max=100)` | requis, max 100                            |
| `firstBuildingFloorCount`              | `@NotNull @Min(0)`         | entier, ≥ 0                                |

Ce n'est qu'une validation de confort : l'API reste la seule source de
vérité, et `GlobalExceptionHandler` (oikos-api) renvoie un
`ErrorResponse { status, error, message, path, timestamp }` en cas de
validation serveur échouée — c'est ce format que
`shared/utils/getErrorMessage.ts` sait extraire pour l'afficher via le
composant `Alert`.

## 5. Gestion des erreurs

- `shared/components/ErrorBoundary` — filet de sécurité pour toute erreur de
  rendu React non rattrapée.
- `router/index.tsx` — route `*` vers `NotFoundPage` (404).
- `shared/pages/ServerErrorPage` — 500 générique (non branchée sur une route
  dédiée dans ce périmètre réduit ; prête à être utilisée si un besoin de
  page 500 dédiée apparaît).
- Erreurs API — chaque hook de mutation expose `error`, traduit en message
  utilisateur via `getErrorMessage()` et affiché avec `Alert`.

## 6. Tests

Trois niveaux couverts pour cette version (voir §14 du guide) :

- **Utilitaires** — `shared/utils/getErrorMessage.test.ts`.
- **Logique métier / schémas** — `features/property-mngt/properties/schemas/createPropertySchema.test.ts`.
- **Composants** — `features/property-mngt/properties/components/CreatePropertyForm.test.tsx`
  (rendu, validation, soumission), avec Testing Library + `user-event`.

`npm run test` exécute Vitest en mode CI (une passe, pas de watch) — c'est
la commande à intégrer dans une pipeline CI/CD.

## 7. Conventions

- Nommage en anglais dans le code (voir §17 du guide), français réservé aux
  textes affichés à l'utilisateur (labels, messages d'erreur) — cohérent
  avec la règle 2 d'`oikos-api/docs/ARCHITECTURE.md`.
- Alias d'import `@/*` → `src/*` (voir `tsconfig.app.json`, `vite.config.ts`).
- ESLint (flat config, `typescript-eslint` + `react-hooks` +
  `react-refresh`) et Prettier, avec `eslint-config-prettier` pour éviter
  les conflits de règles de formatage.
- Mobile first : composants et layouts stylés d'abord pour mobile
  (Tailwind, sans variant = mobile), enrichis avec les préfixes `sm:`/`md:`
  pour les écrans plus grands ; zones interactives ≥ 44px (`min-h-11`
  sur `Button`/`Input`).

## 8. Limites connues de cette v1 (à traiter dans une itération suivante)

- Pas de page 401/403 dédiée : une session invalide redirige silencieusement
  vers `/login`.
- Pas de i18n : tous les textes sont en français, codés en dur dans les
  composants.
- Pas de CI/CD configurée dans ce dépôt (scripts `lint`/`test`/`build`
  prêts à être branchés).
