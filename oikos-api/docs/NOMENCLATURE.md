# Nomenclature FR → EN

Référencé par [ARCHITECTURE.md, règle 2](ARCHITECTURE.md#règle-2--nomenclature-en-anglais).
Table de correspondance entre le vocabulaire métier français (SFD, échanges
fonctionnels) et les noms utilisés dans le code. À compléter à chaque
nouvelle SFD, **avant** d'écrire le code correspondant.

## Module structure de copropriété (`copropriete` → `property`)

| Terme FR (SFD)                    | Nom dans le code (EN)         | Notes |
|------------------------------------|--------------------------------|-------|
| Copropriété                        | `Property`                     | Le domaine produit reste "property management" (déjà dans le README) ; l'agrégat est `Property`. |
| Immeuble                           | `Building`                     | |
| Lot                                | `Unit`                         | Terme standard en gestion immobilière anglophone. |
| Type de lot | `UnitTypeDefinition` | Propre à chaque property (plus un enum global fixe) : chaque property définit ses propres types (nom libre, ex. "Appartement", "Duplex"). Une ligne `"OTHERS"` est systématiquement créée à la création de la property (voir `CreatePropertyService`/`ConfigurePropertyService`), non protégée (retirable comme n'importe quel autre type tant qu'aucun `Unit` ne la référence). |
| Tantièmes                          | `Shares`                       | Quote-part des charges communes attachée au lot lui-même — à ne pas confondre avec `OwnershipShare`. |
| Copropriétaire / rattachement copropriétaire-lot (`ProprieteLot`) | `UnitOwnership` | Pivot Party ↔ Unit. |
| Part de propriété (`PartPropriete`)| `OwnershipShare`                | % de détention du lot par un copropriétaire donné (distinct de `Shares`). |
| Statut d'occupation (RG-LOT-01)    | `OwnershipStatus` (`AFFECTED` / `NOT_AFFECTED`) | Calculé à la lecture, jamais stocké. |
| Membre du syndic (`MembreSyndic`)  | `BoardMember`                   | Vocabulaire HOA (Homeowners Association) anglophone. |
| Rôle de gestion (`RoleGestion`)    | `BoardRole` (`PRESIDENT`/`TREASURER`/`SECRETARY`/`MEMBER`/`VOLUNTEER_MANAGER`) | "Syndic bénévole" → `VOLUNTEER_MANAGER`. |
| nom                                 | `name`                          | |
| adresse                             | `address`                       | |
| nombre d'étages                     | `floorCount`                    | |
| numéro de lot                       | `unitNumber`                    | |
| premier immeuble (création composite)| `firstBuilding...` (ex. `firstBuildingName`, `firstBuildingFloorCount`) | |

## Module identité (`contact` → `party`)

Renommage complet du module `contact` en `party` : un `Party` représente un
acteur juridique (personne physique ou société), pas seulement une personne
physique. `nom`/`prénom` sont fusionnés en un seul champ `fullName`, et un
nouveau discriminant `partyType` distingue les deux natures.

| Terme FR (SFD)                    | Nom dans le code (EN)         | Notes |
|------------------------------------|--------------------------------|-------|
| Partie / acteur juridique           | `Party`                        | Anciennement `Contact` (module `contact` → `party`, table `contact` → `party`). |
| Nom + prénom                       | `fullName`                     | Un seul champ, remplace `lastName`/`firstName`. |
| Type de partie (particulier/société)| `PartyType` (`INDIVIDUAL`/`COMPANY`) | Nouveau champ obligatoire, colonne `party_type`. |

## Libellés de numérotation automatique des unités (`POST /properties/configure`)

Ce endpoint crée une property avec ses buildings et un nombre donné d'unités
par type, sans numéro de lot individuel en entrée : le `unitNumber` est donc
généré automatiquement sous la forme `"{unitTypeName} {n}"`, la séquence `n`
redémarrant à 1 pour chaque couple (building, type de lot). Cette convention
n'est valable que pour ce flux de création en masse (building vide) ; elle
ne coordonne pas avec des unités ajoutées ultérieurement via
`POST /buildings/{id}/units`.

Depuis le passage de `UnitType` (enum global fixe) à `UnitTypeDefinition`
(par property), il n'y a plus de table de traduction figée FR→EN pour les
libellés de numérotation : chaque entrée `UnitTypeConfiguration` de la
requête porte directement le nom du type choisi par l'appelant (ex.
"Appartement", "Box"), *find-or-create* par `(property, name)` puisque la
property n'existe pas encore avant cet appel — ses types ne peuvent donc pas
préexister, sauf `"OTHERS"` qui est toujours seedé en tout début d'appel.

## Modules gestion du compte client (`accounting`) et des échéances/appels à cotisation (`installment`)

Conçus directement en anglais (spécification déjà formulée avec les noms
cibles), mais la table ci-dessous documente la correspondance avec le
vocabulaire métier français de la spec d'origine.

`accounting` (le grand livre : comptes et mouvements) et `installment` (ce qui
est dû : échéances, appels à cotisation, rapprochement) étaient à l'origine
un seul module `accounting`, séparés en deux modules pour isoler la
responsabilité "dette" de la responsabilité "grand livre". Comme
`Allocation`/`AutoAllocationEngine` doivent lire et écrire des deux côtés à
la fois (RG009), la frontière entre les deux modules est **bidirectionnelle** — tout comme
`property ↔ accounting` (`accounting` vérifie l'existence de l'unité/property via
`UnitDirectoryPort`/`PropertyDirectoryPort`, `property` provisionne le compte
`PROPERTY` via `PropertyAccountProvisioningPort` à la création de la property,
cf. RG010bis) : chaque module expose
ses propres ports `application.port.in`/`application.port.out` et n'accède
jamais au modèle de domaine, au repository ou à l'infrastructure de l'autre
directement (règle 4, vérifiée par `DependencyRulesArchTest`). Les
identifiants qui traversent la frontière (`Installment.accountId`,
`Allocation.movementId`) sont des `EntityId` génériques, jamais le type fort
du module propriétaire — même principe que `Installment.unitId` vis-à-vis de
`property`.

| Terme FR (spec)                    | Nom dans le code (EN)         | Module | Notes |
|------------------------------------|--------------------------------|--------|-------|
| Compte (de lot ou de property)     | `Account`                      | `accounting` | Un par lot (RG001 révisée) et non plus par copropriétaire, plus un par property (comptabilité miroir). `holderId` est polymorphique : référence soit un `Unit` (`property.domain.valueobject.UnitId`), soit une `Property` (`property.domain.valueobject.PropertyId`), selon `accountType`. Colonne unique `holder_id` + `account_type` (`uk_account_holder_id_account_type`), sans FK (polymorphique). |
| Type de titulaire de compte        | `AccountType` (`UNIT`/`PROPERTY`) | `accounting` | Discriminant du holder. |
| Mouvement (comptable)              | `Movement`                     | `accounting` | Écriture d'historique immuable (RG002) : jamais modifiée ni supprimée. |
| Sens (débit/crédit)                | `MovementDirection` (`DEBIT`/`CREDIT`) | `accounting` | Stocké explicitement, pas dérivé du type — extensibilité (nouveaux types sans logique de signe à maintenir). |
| Type de mouvement (paiement, échéance, pénalité, frais de relance, régularisation, solde initial, remboursement, avoir) | `MovementType` (`PAYMENT`/`INSTALLMENT`/`PENALTY`/`REMINDER_FEE`/`POSITIVE_ADJUSTMENT`/`NEGATIVE_ADJUSTMENT`/`INITIAL_BALANCE`/`REFUND`/`CREDIT_NOTE`) | `accounting` | |
| Échéance                           | `Installment`                  | `installment` | `unitId` référence un `Unit` réel (`property.domain.valueobject.UnitId`), contrainte par FK (`installment.unit_id → unit.id`). `accountId` est un `EntityId` générique (le compte vit dans `accounting`), résolu via `AccountLedgerPort` à la génération. |
| Affectation / Lettrage             | `Allocation`                   | `installment` | Lien entre un mouvement créditeur et une échéance (RG009 : ne modifie jamais les mouvements). `movementId` est un `EntityId` générique pour la même raison que `Installment.accountId`. Contrairement à `Movement`, peut être physiquement supprimée (RG012 : désaffectation). |
| Appel à cotisation (manuel)        | `RecordInstallmentCallUseCase` (endpoint `POST /installment-calls`) | `installment` | Création en masse d'échéances + mouvements de débit à partir de lignes `(unitId, montant)` fournies par l'appelant. Le mouvement (+ solde/miroir) est posté via `AccountLedgerPort.recordDebit`, jamais directement sur les repositories de `accounting`. Outil bas niveau/correctif, distinct de la génération automatique ci-dessous. |
| Appel à cotisation (entité, génération automatique) | `InstallmentCall` (entité), `GenerateInstallmentCallUseCase` (endpoint `POST /properties/{id}/installment-calls`) | `installment` | Décrit une collecte de fonds sur une `period` (`YearMonth`, un mois), distincte du `dueDate` (date limite de paiement, peut différer du mois collecté). Génère une `Installment` par lot de la property, priced depuis `UnitTypePricing` (`property.application.port.in.ListUnitTypePricesByPropertyUseCase`, exposé via le port `PropertyUnitPricingPort` propre à `installment`). Un lot dont le type n'a pas de prix configuré, ou dont le compte n'est pas encore provisionné (`AccountLedgerPort.findUnitAccountId` vide), est exclu (pas une erreur) et remonté dans `skippedUnitIds`. Contrainte unique `(property_id, period)` : un deuxième appel sur la même période est refusé (`InstallmentCallAlreadyExistsException`). `Installment.installmentCallId` (FK nullable) relie chaque échéance à l'appel qui l'a générée ; reste `null` pour le flux manuel ci-dessus. |
| Reste dû                           | `remainingDue`                 | `installment` | Calculé (RG011), jamais stocké. |
| Montant payé                       | `amountPaid`                   | `installment` | Calculé (somme des affectations), jamais stocké. |
| Statut d'échéance (non payée/partiellement payée/payée/en retard) | `InstallmentStatus` (`NOT_PAID`/`PARTIALLY_PAID`/`PAID`/`OVERDUE`) | `installment` | Calculé à la lecture (RG011), jamais stocké. `OVERDUE` prioritaire sur `PARTIALLY_PAID` en cas de chevauchement. |
| Solde du compte                    | `balance` (`GetAccountBalanceUseCase`) | `accounting` | RG010 révisée : désormais **persisté** sur `Account` (tenu à jour à chaque mouvement par `AccountBalanceService`), plutôt que recalculé à la lecture — motivé par un besoin de reporting/performance. |
| Comptabilité miroir property       | `AccountBalanceService` | `accounting` | RG010bis : chaque mouvement sur un compte de lot poste le mouvement de sens inverse sur le compte de la property correspondante (résolue via `unit → building → property`), y compris pour les paiements. Le compte property est désormais auto-créé (contrairement au compte `UNIT`, jamais auto-créé) dès la création de la property, via `PropertyAccountProvisioningPort` (`property` → `CreateAccountUseCase` de `accounting`), dans `CreatePropertyService` et `ConfigurePropertyService` — une `AccountNotFoundException` à ce stade signalerait un bug, plus un oubli de provisioning manuel. |
| Rapprochement automatique (FIFO)   | `AutoAllocationEngine` | `installment` | Lit les crédits d'un compte via `AccountMovementsPort` (délégué à `accounting.ListMovementsForAllocationUseCase`) plutôt que le repository de `Movement` directement. Exposé à `accounting` (déclenchement après un paiement, `RecordPaymentService`) via le port-in `AutoAllocateUseCase`. |

## Modules déjà en anglais (aucun changement)

`user`, `auth`, `shared`, `accounting`, `installment` ont été conçus directement
en anglais et ne nécessitent pas de renommage : `User`, `RefreshToken`,
`VerificationToken`, `Account`, `Movement`, `Installment`, `InstallmentCall`,
etc. (`contact` a depuis été renommé en `party`, voir section dédiée
ci-dessus.)

## Paramétrage du prix par type de lot (`property`)

| Terme FR (SFD)                    | Nom dans le code (EN)         | Notes |
|------------------------------------|--------------------------------|-------|
| Prix par type de lot               | `UnitTypePricing` (entité), `Price` (VO) | Un prix optionnel par `unitTypeId` (FK vers `UnitTypeDefinition`, qui est déjà rattaché à une seule property) — pas de valeur pour un type non paramétré (pas d'erreur). À ne pas confondre avec `Amount` (VO partagée dans `shared`, montant strictement positif d'un mouvement/échéance/allocation, utilisée par `accounting` et `installment`) ni `Shares` (tantièmes). Pas d'historisation : un montant d'appel de cotisation déjà émis reste figé (RG002) quel que soit un changement de prix ultérieur — seule la valeur courante est utile. Supprimer un `UnitTypeDefinition` supprime en cascade (DB) son éventuelle ligne de prix. |

## Comment utiliser cette table

- Avant d'implémenter une nouvelle SFD, traduire chaque terme métier ici
  s'il n'y est pas déjà, en vérifiant qu'il ne collisionne pas avec un
  terme déjà utilisé ailleurs dans le code pour un concept différent.
- Un terme FR peut apparaître dans plusieurs SFD avec des sens différents :
  vérifier le contexte avant de réutiliser un nom EN existant.
