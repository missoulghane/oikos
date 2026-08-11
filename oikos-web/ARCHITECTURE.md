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
│   ├── privateRoutes.tsx # sous AppLayout, préfixé par module :
│   │                     #   /property-mngt/*      (gérant/syndic - RequireAccess
│   │                     #                          canManageProperties sur tout le sous-arbre)
│   │                     #   /property-ownership/*  (copropriétaire - ouvert à tout
│   │                     #                          utilisateur authentifié)
│   │                     #   /parties/:propertyId/:partyId, /dashboard, /profile
│   │                     #   restent neutres (dual-purpose, hors des deux préfixes)
│   ├── RequireAccess.tsx # Garde générique pilotée par un prédicat (voir access.ts)
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
│   │   ├── register/      # Inscription, activation de compte
│   │   │   ├── api/ / components/ / hooks/ / pages/ / schemas/ / types/
│   │   │   └── index.ts
│   │   │
│   │   └── me/            # Le compte courant : lecture (CurrentUser, rôles,
│   │       │               # règles d'accès `access.ts`) et édition de son
│   │       │               # propre profil (fullName/email) — transverse à
│   │       │               # tous les rôles, indépendant de property-ownership.
│   │       ├── api/       # getCurrentUser, updateProfile
│   │       ├── components/ # EditProfileForm
│   │       ├── hooks/     # useCurrentUser, useUpdateProfile
│   │       ├── pages/     # ProfilePage (/my/profile)
│   │       ├── schemas/
│   │       ├── types/     # CurrentUser, PropertyRoleName
│   │       ├── utils/     # access.ts (isAdmin, canManageProperties, ...)
│   │       └── index.ts
│   │
│   ├── property-mngt/      # Coeur métier copropriété (vue gérant/syndic)
│   │   ├── properties/     # Structure : copropriétés, immeubles, lots, copropriétaires
│   │   │   ├── api/        # getProperties, createProperty, getUnits, ...
│   │   │   ├── components/ # PropertyCard, PropertyList, CreatePropertyForm, ...
│   │   │   ├── hooks/      # useProperties, useCreateProperty, ...
│   │   │   ├── pages/      # PropertiesPage, CreatePropertyPage, ...
│   │   │   ├── schemas/
│   │   │   ├── types/
│   │   │   └── index.ts
│   │   ├── parties/        # Contacts (copropriétaires, tiers) rattachés à une property
│   │   │   ├── api/ / components/ / hooks/ / pages/ / schemas/ / types/
│   │   │   └── index.ts
│   │   ├── installments/   # Échéances, appels de fonds (vue gérant, par lot/property)
│   │   ├── pricing/        # Paramétrage des prix par type de lot
│   │   └── accounting/     # Gestion financière (comptes, mouvements, lettrage)
│   │
│   └── property-ownership/ # Espace self-service du copropriétaire (vue "mes lots",
│       │                   # pas la vue gérant) — accès en lecture sur ses propres
│       │                   # lots, gated côté API par `ownsUnit`/`ownsParty`.
│       ├── units/          # "Mes lots" : liste + détail en lecture seule, réutilise
│       │   │                # les sections partagées de `property-mngt` (comptes,
│       │   │                # échéances, propriétaires) plutôt que de les dupliquer.
│       │   ├── api/        # getMyUnits
│       │   ├── hooks/      # useMyUnits
│       │   ├── pages/      # MyUnitsPage (/my/units), MyUnitDetailPage
│       │   ├── types/      # OwnedUnit
│       │   └── index.ts
│       └── installments/   # "Mes échéances" : vue consolidée tous lots (agrégée côté
│           │                # API par `GET /users/me/installments`, jointe côté front
│           │                # à `useMyUnits()` pour l'étiquette property/lot).
│           ├── api/        # getMyInstallments
│           ├── hooks/      # useMyInstallments
│           ├── pages/      # MyInstallmentsPage (/my/installments)
│           └── index.ts
│
├── shared/
│   ├── api/httpClient.ts       # Instance Axios + intercepteurs JWT/refresh
│   ├── components/             # Button, Input, Card, Loader, EmptyState,
│   │                           # Alert, Pagination, ErrorBoundary
│   ├── constants/queryKeys.ts  # Clés TanStack Query centralisées
│   ├── context/                # SidebarContext, ThemeContext (clair/sombre)
│   ├── layouts/                # AppLayout (zone privée), AuthLayout
│   ├── pages/                  # NotFoundPage (404), ServerErrorPage (500)
│   ├── types/                  # ApiErrorBody (forme des erreurs de l'API)
│   └── utils/getErrorMessage.ts
│
├── config/env.ts       # Lecture/validation des variables d'environnement
├── config/theme.ts     # Couleur d'accent de l'app (paramétrage code, cf. §8)
├── test/setup.ts        # Setup Vitest (jest-dom)
└── main.tsx
```

Les features sont regroupées par domaine métier : `identity` (`auth`,
`register`, `me`) pour les comptes et l'accès, `property-mngt` (`properties`,
`parties`, `installments`, `pricing`, `accounting`) pour le coeur métier
copropriété côté gérant/syndic, `property-ownership` (`units`) pour l'espace
self-service du copropriétaire. Cette dernière distinction suit le
découpage d'autorisation de l'API (`managesX` vs `ownsX`, voir
`PropertyAccessEvaluator` côté `oikos-api`) : `property-mngt` regroupe les
écrans où l'action nécessite un rôle gérant/syndic sur la property,
`property-ownership` les écrans qu'un simple copropriétaire peut utiliser sur
ses propres lots — quitte à réutiliser sans duplication les sections déjà
écrites dans `property-mngt` (ex. `UnitAccountSection`, `UnitInstallmentsSection`).

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

## 8. Thème (clair/sombre et couleur d'accent)

Deux axes indépendants, inspirés de `oikos-template` (TailAdmin) :

- **Clair / sombre — choix de l'utilisateur.** `ThemeProvider`
  (`shared/context/ThemeContext.tsx`) pose la classe `.dark` sur `<html>` et
  mémorise le choix dans `localStorage` (`oikos-theme-mode`). La préférence
  système (`prefers-color-scheme`) ne sert que d'amorce à la première visite.
  Le variant Tailwind est déclaré dans `index.css`
  (`@custom-variant dark (&:is(.dark *))`), donc tout se style avec le préfixe
  `dark:`. Un script inline dans `index.html` applique la classe avant le
  premier rendu pour éviter le flash blanc — il doit rester aligné avec
  `resolveInitialMode()`. Le bouton `ThemeToggleButton` est présent dans
  `AppHeader` et dans `AuthLayout` (les écrans de connexion sont hors app
  shell).
- **Couleur d'accent — choix du développeur.** `THEME_COLOR` dans
  `config/theme.ts` (`blue` par défaut, plus `emerald`, `violet`, `amber`).
  `ThemeProvider` la pose en `<html data-theme="…">` une fois pour toutes ;
  elle n'est **pas** exposée à l'utilisateur. Chaque palette est un bloc
  `:root[data-theme='…']` d'`index.css` qui redéfinit l'échelle
  `--color-brand-25…950` : comme les utilitaires Tailwind v4 compilent vers
  `var(--color-brand-…)`, aucune classe n'est dupliquée. Ajouter une palette =
  un bloc CSS + une entrée dans `THEME_COLORS`.

Conventions de surfaces en sombre (identiques au template) : page et chrome
(`AppHeader`, `AppSidebar`) en `dark:bg-gray-900`, cartes et panneaux en
`dark:bg-white/[0.03]`, éléments flottants (`Dropdown`) en `dark:bg-gray-dark`,
bordures en `dark:border-gray-800`, texte principal en `dark:text-white/90` et
secondaire en `dark:text-gray-400`.

## 9. Inscription du syndic bénévole (wizard)

`/register/board-admin/*` est un wizard en 7 étapes
(`features/identity/onboarding`), une URL par étape — d'où le lien profond, le
bouton « Modifier » du récapitulatif et le retour navigateur gratuits.

Deux écritures serveur seulement, et c'est ce qui structure tout le reste :

- **Fin de l'étape 1** — `POST /users/onboarding-leads` enregistre l'email
  seul. Le compte ne peut pas encore exister : une `Party` est portée par une
  propriété (`party.property_id NOT NULL`), qui n'a pas de nom avant l'étape 2.
  Sans cette capture, tout visiteur abandonnant entre les étapes 1 et 2 serait
  perdu.
- **Fin de l'étape 2** — `POST /users/register-property-board-admin` crée
  compte + copropriété + party + rôle d'un bloc (couplage conservé) et renvoie
  un **jeton d'onboarding** : le compte n'étant pas vérifié, il ne peut pas se
  connecter, et ce jeton de portée réduite (un seul endpoint, une seule
  propriété, ~2 h) est sa seule autorisation. À partir d'ici, un abandon laisse
  derrière lui un compte utilisable.
- **Étapes 3 à 6** — bufferisées côté client (`OnboardingProvider`,
  `localStorage`). Le mot de passe, lui, reste en mémoire uniquement.
- **Étape 7** — `POST /properties/{id}/configuration` commet le tout en une
  transaction (mode, types + prix, bâtiments, lots, comptes bancaires). Un seul
  commit rend les allers-retours depuis le récapitulatif sans effet de bord ;
  l'API refuse en 409 une copropriété qui a déjà des bâtiments, ce qui rend le
  double envoi inoffensif.

Le jeton expiré n'est pas un cul-de-sac : l'endpoint accepte aussi une session
de syndic ordinaire, et `ResumeOnboardingBanner` (tableau de bord) propose de
reprendre à un syndic dont la copropriété n'a encore aucun bâtiment.

L'étape 4 est conditionnelle : montants par type au forfait, budget
prévisionnel en tantièmes (les tantièmes eux-mêmes se saisissent lot par lot
ensuite). Les quatre champs d'adresse sont recomposés en une seule chaîne, seul
format que l'API stocke (250 caractères, validés sur la concaténation).

## 10. Limites connues de cette v1 (à traiter dans une itération suivante)

- Pas de page 401/403 dédiée : une session invalide redirige silencieusement
  vers `/login`.
- Pas de i18n : tous les textes sont en français, codés en dur dans les
  composants.
- Pas de CI/CD configurée dans ce dépôt (scripts `lint`/`test`/`build`
  prêts à être branchés).
