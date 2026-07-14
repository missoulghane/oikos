# Oikos

API backend de gestion de copropriété (property management).

## 1. Vision produit

Oikos a pour objectif cible de couvrir la gestion d'une copropriété au
quotidien : biens/lots, copropriétaires, appels de fonds et charges,
budgets et comptabilité de copropriété, assemblées générales, incidents
et prestataires. C'est la vision cible du produit — l'état réellement
implémenté est décrit ci-dessous et évolue au fil des générations.

## 2. État actuel (à tenir à jour)

Contextes métier existants sous `src/main/java/com/architek/oikos/` :

- `auth` — inscription, connexion, JWT (access + refresh token),
  vérification d'email, activation de compte, réinitialisation de mot de
  passe. La connexion accepte indifféremment trois types d'identifiant
  (priorité fixe : login du compte, puis email du contact lié, puis
  téléphone du contact lié — voir `LoadUserByIdentifierService`).
- `user` — gestion des comptes applicatifs (`User`) : CRUD, recherche
  paginée. `User` ne porte plus d'identité propre : il référence un
  `Contact` par id (`contactId`) et porte uniquement les identifiants de
  connexion (mot de passe, `login` optionnel, statut `verified`/`enabled`,
  rôles).
- `contact` — fiche d'identité d'une personne physique (nom, prénom,
  email, téléphone), indépendante d'un compte applicatif. CRUD de base.
  Contexte de base pour la structuration d'une copropriété (SFD "Gestion
  de la Structure des Copropriétés et des Accès") : copropriétaires et
  membres du syndic référenceront un `Contact` plutôt qu'un `User`.
- `copropriete` — structure physique d'une copropriété : `Copropriete`,
  `Immeuble`, `Lot` (type de lot, tantièmes), et les deux pivots de la
  SFD "Gestion de la Structure des Copropriétés et des Accès" :
  - `ProprieteLot` — rattache un `Contact` (par id) à un lot avec sa part
    de propriété. Règle appliquée : la somme des parts d'un même lot ne
    peut pas dépasser 100 % (`AddProprieteLotService`). RG-LOT-01 : un lot
    sans aucun `ProprieteLot` associé est étiqueté `NON_VENDU_PROMOTEUR`
    dans `LotView` (calculé à la lecture, jamais stocké).
  - `MembreSyndic` — rattache un `Contact` (par id) à une fonction de
    gestion (`RoleGestion`) sur une copropriété.

  La création d'une copropriété (`CreateCoproprieteService`) crée
  systématiquement son premier immeuble dans la même transaction, ce qui
  garantit la règle de gestion "une copropriété doit posséder au moins un
  immeuble" dès la création plutôt que de la vérifier a posteriori.
  CRUD limité à create/get/list pour Copropriete/Immeuble/Lot (pas
  d'update/delete : non requis par la SFD à ce stade) ; add/list/remove
  pour les deux pivots.
- `shared` — briques transverses : pagination, gestion des exceptions,
  audit, envoi d'email, configuration.

Le couplage `user` → `contact` suit le patron déjà utilisé pour
`auth` → `user` : `user.application.port.out.ContactDirectoryPort` (out-port
propre à `user`) est implémenté par
`user.infrastructure.adapter.ContactDirectoryAdapter`, seul point du
module autorisé à dépendre des port-in de `contact`. `user.application`
reste ainsi totalement découplé de `contact`. `copropriete` référence
`Contact` de la même façon (par `EntityId` générique sur `ProprieteLot`/
`MembreSyndic`), sans dépendre du type `ContactId` propre à `contact`.

Reste à implémenter (SFD "Gestion de la Structure des Copropriétés et
des Accès") : résolution des droits contextuels par copropriété
(RG-ACC-02 — rôle syndic ou copropriétaire simple selon le contexte de
navigation), qui s'appuiera sur `MembreSyndic` et `ProprieteLot`.

> Cette section doit être mise à jour à chaque nouvelle génération de
> code (nouveau contexte métier, nouvelle fonctionnalité significative,
> changement de périmètre). Voir aussi [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
> pour les règles d'architecture et de dépendances entre couches.

## 3. Stack technique

- Java 25, Spring Boot 4.1 (web, data-jpa, security, validation, mail,
  actuator)
- PostgreSQL (prod) + Flyway (migrations) / H2 en profil `dev`
- JWT (jjwt) pour l'authentification
- MapStruct + Lombok (couches `infrastructure`/`web` uniquement, voir
  ARCHITECTURE.md)
- springdoc-openapi (Swagger UI)
- Tests : JUnit 5, Spring Boot Test, ArchUnit (règles d'architecture),
  Testcontainers (PostgreSQL)

## 4. Démarrage rapide

```bash
# Lancer en profil dev (H2 en mémoire, pas besoin de Postgres)
./mvnw spring-boot:run

# Lancer les tests
./mvnw test
```

- Swagger UI : http://localhost:8080/swagger-ui.html
- Console H2 (profil dev) : http://localhost:8080/h2-console

Variables d'environnement utiles (voir `application.yml` /
`application-dev.yml`) : `JWT_SECRET`, `MAIL_HOST`, `MAIL_PORT`,
`MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`,
`APP_VERIFICATION_BASE_URL`, `APP_ACCOUNT_ACTIVATION_BASE_URL`,
`APP_PASSWORD_RESET_BASE_URL`.

## 5. Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — architecture
  hexagonale, découpage en couches, règles de dépendances.
- Ce README — vision produit et état fonctionnel du projet.

Les deux fichiers sont complémentaires et doivent être consultés/mis à
jour à chaque évolution notable : ce README pour le *quoi* (périmètre
métier, fonctionnalités), ARCHITECTURE.md pour le *comment* (règles de
structuration du code).
