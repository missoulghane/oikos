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
  (priorité fixe : login du compte, puis email de la party liée, puis
  téléphone de la party liée — voir `LoadUserByIdentifierService`).
- `user` — gestion des comptes applicatifs (`User`) : CRUD, recherche
  paginée. `User` ne porte plus d'identité propre : il référence une
  `Party` par id (`partyId`) et porte uniquement les identifiants de
  connexion (mot de passe, `login` optionnel, statut `verified`/`enabled`,
  rôles). Les parties créées via `user` sont toujours de type `INDIVIDUAL`.
- `party` (anciennement `contact`) — fiche d'identité d'un acteur juridique
  (personne physique ou société) : `fullName`, `partyType`
  (`INDIVIDUAL`/`COMPANY`), email, téléphone, indépendante d'un compte
  applicatif. CRUD de base. Contexte de base pour la structuration d'une
  copropriété (SFD "Gestion de la Structure des Copropriétés et des
  Accès") : copropriétaires et membres du syndic référencent une `Party`
  plutôt qu'un `User`.
- `copropriete` — structure physique d'une copropriété : `Copropriete`,
  `Immeuble`, `Lot` (type de lot, tantièmes), et les deux pivots de la
  SFD "Gestion de la Structure des Copropriétés et des Accès" :
  - `ProprieteLot` — rattache une `Party` (par id) à un lot avec sa part
    de propriété. Règle appliquée : la somme des parts d'un même lot ne
    peut pas dépasser 100 % (`AddProprieteLotService`). RG-LOT-01 : un lot
    sans aucun `ProprieteLot` associé est étiqueté `NON_VENDU_PROMOTEUR`
    dans `LotView` (calculé à la lecture, jamais stocké).
  - `MembreSyndic` — rattache une `Party` (par id) à une fonction de
    gestion (`RoleGestion`) sur une copropriété.

  La création d'une copropriété (`CreateCoproprieteService`) crée
  systématiquement son premier immeuble dans la même transaction, ce qui
  garantit la règle de gestion "une copropriété doit posséder au moins un
  immeuble" dès la création plutôt que de la vérifier a posteriori.
  CRUD limité à create/get/list pour Copropriete/Immeuble/Lot (pas
  d'update/delete : non requis par la SFD à ce stade) ; add/list/remove
  pour les deux pivots.

  `POST /properties/configure` (`ConfigurePropertyService`) permet de
  configurer une property complète en un seul appel : property, tous ses
  buildings, et pour chaque building, un nombre donné d'unités par type
  (ex. 50 appartements, 33 box). Les unités sont créées avec des tantièmes
  à zéro (affectés plus tard) et un `unitNumber` généré automatiquement
  (voir `docs/NOMENCLATURE.md`). L'ensemble est créé dans une seule
  transaction, avec une limite paramétrable du nombre total d'unités par
  requête (`oikos.property.configure.max-units`).
- `accounting` — grand livre et compte client : compte (`Account`, un par lot
  et un par property en miroir, RG001 révisée) et mouvements comptables
  (`Movement`, historique immuable, RG002). Le solde du compte
  (`GetAccountBalanceUseCase`) est toujours tenu à jour par
  `AccountBalanceService` (RG010), jamais recalculé à la lecture.
- `installment` — ce qui est dû : échéances (`Installment`, générées par un
  appel de cotisation manuel `POST /installment-calls` ou automatique `POST
  /properties/{id}/installment-calls`, une échéance = un débit automatique
  posté sur `accounting` via un port), et affectations (`Allocation`, lettrage
  crédit ↔ échéance, automatique en FIFO ou manuel, ne modifie jamais les
  mouvements — RG009). Le statut d'une échéance
  (`InstallmentStatusCalculator`) est toujours calculé à la lecture, jamais
  stocké (RG011). `unitId` référence un `Unit` réel (contrainte FK en base +
  validation d'existence via son propre `UnitDirectoryPort`). `accounting` et
  `installment` étaient un seul module à l'origine ; voir
  `docs/NOMENCLATURE.md` pour le détail de la séparation, RG001-RG012, et la
  correspondance FR/EN complète.
- `shared` — briques transverses : pagination, gestion des exceptions,
  audit, envoi d'email, configuration, ainsi que les VO génériques utilisées
  au-delà d'un seul module (`EntityId`, `Amount` — montant strictement
  positif, utilisée par `Movement`/`Installment`/`Allocation` dans `accounting`
  et `installment`).

Le couplage `user` → `party` suit le patron déjà utilisé pour
`auth` → `user` : `user.application.port.out.PartyDirectoryPort` (out-port
propre à `user`) est implémenté par
`user.infrastructure.adapter.UserPartyDirectoryAdapter`, seul point du
module autorisé à dépendre des port-in de `party`. `user.application`
reste ainsi totalement découplé de `party`. `copropriete` référence
`Party` de la même façon (par `EntityId` générique sur `ProprieteLot`/
`MembreSyndic`), sans dépendre du type `PartyId` propre à `party`.
`accounting` et `installment` suivent le même patron pour leur dépendance
cross-feature vers `property` : chacun a son propre
`application.port.out.PropertyDirectoryPort`/`UnitDirectoryPort`, implémenté
par un adapter nommé distinctement (`AccountingPropertyDirectoryAdapter`/
`AccountingUnitDirectoryAdapter` côté `accounting`,
`InstallmentPropertyDirectoryAdapter`/`InstallmentUnitDirectoryAdapter` côté
`installment`) pour éviter toute collision de bean Spring. Entre eux,
`accounting` et `installment` sont couplés dans les deux sens (`Allocation`
doit lire et écrire à la fois le mouvement et l'échéance qu'elle rapproche,
RG009) : chacun expose ses propres ports (`accounting.application.port.out.AutoAllocationPort`
vers `installment` ; `installment.application.port.out.AccountLedgerPort`,
`AccountMovementsPort`, `MovementLookupPort` vers `accounting`), implémentés par
des adapters qui ne dépendent que des port-in publics de l'autre module,
jamais de son modèle de domaine ni de ses repositories — vérifié par
`DependencyRulesArchTest`.

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
