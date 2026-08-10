# ADR 0001 — Refonte de la comptabilité en partie double conforme PCM

Statut : Accepté (cadrage — Phase 1/8, voir feuille de route ci-dessous)
Date : 2026-08-07

## Contexte

Le module `accounting`/`installment` actuel (`V3__accounting.sql`,
`V4__unit_account_lettrage.sql`, `V5__installment_settlement.sql`) implémente
une comptabilité simplifiée à deux grands livres ad hoc :

- `FinancialAccount`/`FinancialJournalEntry` — trésorerie (caisse/banque)
  d'une property, écriture simple IN/OUT sur un seul compte.
- `UnitAccount`/`UnitAccountMovement` — solde par lot (créance/avance
  compensées dans un seul solde), avec lettrage FIFO
  (`LettrageProposalCalculator`/`UnitAccountAllocation`).

Ce modèle n'a ni numéro de compte, ni partie double (aucune écriture ne
mouvemente deux comptes en équilibre débit/crédit), ni journal, ni exercice
à périodes granulaires, et ne se rattache à aucun plan comptable. Il ne
permet pas de produire un grand livre, une balance, un CPC ou un bilan au
sens du CGNC/PCGE marocain, et ne peut pas servir de base à une reddition de
comptes en assemblée générale conforme à la loi 18-00/106-12.

Objectif : remplacer ce modèle par un moteur comptable en partie double
conforme au Plan Comptable Marocain (PCM), avec plan de comptes
paramétrable par rôle fonctionnel, journaux, écritures équilibrées, appels
de fonds, règlements/imputation, clôture d'exercice, et reporting
(journal, grand livre, balance, CPC, bilan, trésorerie, impayés).

Le chantier est trop vaste pour une session — voir la feuille de route en
fin de document. Ce document couvre uniquement les décisions de cadrage
(Phase 1), sans code.

## Décisions

### 1. Isolation multi-syndic : `property_id`, pas de RLS générique

La spécification source demande une isolation multi-tenant par Postgres RLS
(`USING (tenant_id = current_setting('app.tenant_id'))`). Aucune table
d'oikos n'utilise RLS aujourd'hui ; l'isolation se fait partout par FK
`property_id` + contrôle d'accès Spring Security au niveau méthode
(`@propertyAccess.canReadAccounting`/`canWriteAccounting`, etc.).

**Décision** : `property_id` joue le rôle de `tenant_id` partout où la
spec l'exige. Pas de RLS Postgres, pas de mécanisme de session
`app.tenant_id` — cohérence avec l'ensemble du code existant plutôt
qu'introduire une deuxième doctrine de sécurité pour un seul module.

### 2. Frontière de modules : `accounting` et `installment` restent séparés

La spec (§4.1) dessine un bloc de domaine unique (écritures, appels de
fonds, règlements, lettrage). L'existant sépare volontairement `accounting`
(grand livre) d'`installment` (dette), séparation documentée dans
`NOMENCLATURE.md` et protégée par une règle ArchUnit dédiée
(`accounting_must_not_depend_on_other_modules_internals`).

**Décision** : conserver les deux modules.
- `accounting` : référentiel de comptes, rôles comptables, journaux,
  écritures, grand livre, reporting, clôture.
- `installment` : appels de fonds, ventilation par tantièmes, règlements,
  imputation/lettrage — génère des écritures via les use cases publics
  d'`accounting`, jamais via son modèle de domaine interne.

La règle ArchUnit existante est conservée et sa portée réévaluée en Phase 6
pour couvrir les nouveaux échanges entre les deux modules.

### 3. Pas de nouvel agrégat `Tiers` — réutilisation de `Party`/`Unit`

La spec (§4.1) introduit un agrégat `Tiers` (Copropriétaire/Fournisseur/
Salarié) porteur de l'auxiliaire comptable. oikos a déjà `Party` (identité
juridique, personne physique ou société) et `Unit` (le lot, déjà porteur
d'un compte auxiliaire unique et stable via l'actuel `UnitAccount`).

**Décision** : pas de nouvel agrégat. L'auxiliaire comptable d'une ligne
d'écriture est :
- un `UnitId` pour les rôles copropriétaire (`UNIT_RECEIVABLE`,
  `UNIT_ADVANCE`) ;
- un `PartyId` pour les rôles fournisseur/personnel (`SUPPLIER`,
  `STAFF_PAYABLE`).

Cohérent avec le principe déjà appliqué ailleurs dans oikos (`EntityId`
générique cross-module plutôt que dupliquer une identité).

### 4. Un seul référentiel de plan de comptes pour la v1

La spec (§1, §3.2) signale explicitement que le choix entre plan « usage
syndic » (comptes 3415/4415) et « PCGE strict » (postes 342/442) est un
point d'arbitrage produit, pas une décision technique unilatérale.

**Décision** : livrer uniquement le référentiel « usage syndic » (§3.2) en
v1. Toute règle métier référence un `AccountRole` fonctionnel résolu vers
un `LedgerAccount` via `AccountRoleMapping` (property, role) — jamais un
numéro de compte en dur — ce qui permet d'ajouter un second référentiel
plus tard sans modifier le moteur. Validation experte-comptable à obtenir
avant mise en production, quel que soit le référentiel retenu au final.

### 5. Un compte dédié par lot, plutôt qu'un compte collectif unique avec auxiliaire

Tension entre deux passages du besoin exprimé :
- §3.2 (plan de comptes de référence) décrit un compte collectif unique
  `34150000` (« Copropriétaires — créances sur appels de fonds ») avec
  auxiliaire par lot — modèle PCM/PCGE standard (un compte de contrôle +
  des sous-comptes auxiliaires).
- L'exigence supplémentaire demande explicitement qu'**un compte soit créé
  à la création de chaque lot**, numéroté `3411500XX` (`XX` incrémental) —
  un compte par lot, pas un auxiliaire sous un compte collectif.

**Décision** : chaque `Unit` reçoit son propre `LedgerAccount` dédié
(`collective = false`, numéro `341150` + incrément par property), qui
assume le rôle `UNIT_RECEIVABLE` pour ce lot précisément. L'auxiliaire réel
des écritures reste néanmoins le `UnitId`, stocké comme identifiant de
première classe sur `JournalEntryLine` — **jamais dérivé du numéro de
compte**, ce qui est précisément le défaut reproché à la maquette source
(auxiliaire = dernier caractère du numéro de compte, cassant au-delà de 9
lots — voir Annexe B de la spec). Cette décision satisfait littéralement
l'exigence supplémentaire (un compte par lot, numérotation incrémentale)
sans reproduire le vice de conception qu'elle corrige par ailleurs.

Le rôle `UNIT_ADVANCE` (avances, compte `44150000`), lui, reste un compte
collectif unique par property, avec le `UnitId` porté comme auxiliaire sur
la ligne d'écriture — l'exigence supplémentaire ne demande pas de compte
d'avance par lot. **Point à confirmer par un expert-comptable marocain**
avant mise en production (voir Questions ouvertes).

### 6. Provisioning automatique des comptes (exigence supplémentaire)

- **Création d'une `Property`** → provisionne un `LedgerAccount` de rôle
  `CASH`, numéro `516100` + incrément propre à la property (ex. `51610001`
  pour la première caisse de cette property). Les comptes `BANK` restent
  créés manuellement par la suite (numéro `514100` + incrément) — non
  provisionnés à la création de la property.
- **Création d'un `Unit`** → provisionne un `LedgerAccount` de rôle
  `UNIT_RECEIVABLE` dédié à ce lot, numéro `341150` + incrément propre à la
  property.
- L'identifiant technique (clé primaire, référencé partout dans le code et
  les FK) est toujours un GUID (`EntityId`/UUID), comme pour toute entité
  oikos. Le numéro de compte PCM est une colonne métier texte séparée,
  jamais réutilisée comme clé technique.
- L'incrément est alloué sous verrou (même technique que la séquence de pièce
  I7 de la spec), pour éviter toute collision en création concurrente de
  lots/properties.

## Nomenclature FR → EN

Voir la table ajoutée à `docs/NOMENCLATURE.md` (section « Refonte
comptabilité PCM »).

## Conséquences

- Suppression complète des tables/entités `FinancialAccount`,
  `FinancialJournalEntry`, `Expense` (ancien modèle), `UnitAccount`,
  `UnitAccountMovement`, `UnitAccountAllocation` et de leurs use
  cases/contrôleurs associés (Phase 2).
- `InstallmentCall`/`Installment` sont conservés et étendus (déjà le bon
  modèle d'appel de fonds/ligne d'appel), plutôt que recréés.
- L'IHM `oikos-web` (`features/property-mngt/accounting`) devra être
  intégralement reprise (Phase 8) — le modèle de types TS actuel (soldes
  compensés, pas de partie double) ne peut pas être adapté incrémentalement.
- Le README produit devra porter une section « Questions ouvertes » listant
  les points à valider par un expert-comptable marocain avant mise en
  production (référentiel comptable, compte d'avance collectif vs par lot).

## Feuille de route (8 phases)

1. Cadrage (ce document) — **fait**.
2. Suppression de l'existant + migrations Flyway (nouveau schéma complet,
   seed du plan de comptes et des rôles) — **fait** (V11-V14, voir
   « Affinements de schéma » ci-dessous ; ancien modèle
   `FinancialAccount`/`UnitAccount`/lettrage entièrement supprimé côté Java
   et SQL, 620 tests toujours au vert).
3. Domaine référentiel + écritures (`LedgerAccount`, `Journal`, `Period`,
   `JournalEntry`/`JournalEntryLine`, invariants I1-I7) + tests unitaires
   domaine — **fait** (28 tests, voir « Périmètre réel de la Phase 3 »
   ci-dessous pour la nuance sur le provisioning).
4. Domaine appels de fonds / règlements / lettrage (répartition au plus
   grand reste, imputation créance/avance FIFO) + tests unitaires domaine —
   **fait** (19 tests, voir « Périmètre réel de la Phase 4 » ci-dessous).
5. Domaine restant (facture fournisseur P4, contre-passation P10, contrôles
   de clôture de période P8, clôture d'exercice P9) + tests unitaires
   domaine — **fait** (15 tests, voir « Périmètre réel de la Phase 5 »
   ci-dessous : les use cases `@Component` pour P4-P10 restent en Phase 6,
   même raisonnement qu'en Phase 3/4).
6. Adapters (contrôleurs REST, DTOs, `problem+json`, JPA, sécurité,
   idempotency-key) — **partiellement fait**, en trois passes : provisioning
   PCM property/unit (voir « Périmètre réel de la Phase 6 »), le moteur
   générique d'écritures (« Phase 6 (suite) »), puis **P1 — émission
   comptabilisée d'un appel de fonds** (« Phase 6 (suite 2) » ci-dessous),
   vérifié de bout en bout sur l'app réellement lancée. JPA `Expense`/
   `Payment`, P2-P10 restants, la sécurité fine et l'idempotency-key restent
   à faire.
7. Tests d'intégration (golden dataset), ArchUnit, OpenAPI.
8. IHM oikos-web + finalisation documentation (NOMENCLATURE/ARCHITECTURE/
   data-model, README, ADRs par décision structurante).

## Affinements de schéma (décidés en Phase 2, à la rédaction des migrations)

Ces points affinent les décisions ci-dessus sans les contredire ; ils sont
apparus nécessaires en écrivant le DDL réel (`V11`-`V14`) et sont documentés
ici plutôt que laissés implicites dans le SQL :

- **Pas de table `account_role_mapping` séparée.** Le rôle fonctionnel
  (`AccountRole`) et l'auxiliaire (`unit_id`) sont portés directement par
  `ledger_account` (colonnes `role`/`unit_id`), plutôt que par une table de
  jointure dédiée : un compte ne joue jamais qu'un seul rôle pour son
  périmètre, donc la jointure séparée n'ajoutait rien. `ledger_account.
  property_id`/`unit_id` sont `NULL` pour les comptes partagés (le plan de
  comptes "usage syndic" commun) et renseignés uniquement pour les instances
  propres à une property/un lot (caisse, banque, créance par lot — décision
  5/6). L'unicité du numéro de compte est garantie séparément pour les
  comptes globaux et pour les comptes scopés par property (deux index
  uniques partiels).
- **`journal` est un catalogue global et statique** des 6 codes (VT/BQ/CA/
  AC/OD/AN), pas une table property-scopée : quel compte de trésorerie
  précis (caisse ou quelle banque) une écriture BQ/CA mouvemente est porté
  par `journal_entry.treasury_account_id`, choisi à la saisie — pas
  pré-lié à un "journal instance" par property. Une property avec deux
  comptes bancaires n'a donc pas besoin de deux journaux BQ distincts.
- **Immuabilité (I4)** implémentée dès la migration via triggers Postgres
  (`trg_journal_entry_immutable`, `trg_journal_entry_line_immutable`),
  testés manuellement contre un Postgres réel : DELETE toujours refusé,
  UPDATE limité aux transitions `DRAFT→POSTED` et `POSTED→REVERSED`, lignes
  jamais insérées/modifiées/supprimées hors `DRAFT`.
- **`allocation` est générique** (relie deux `journal_entry_line`, sans
  connaître `installment`) plutôt que spécifique aux échéances, pour servir
  aussi bien le lettrage créance-copropriétaire/règlement (P2/P3) que le
  lettrage facture-fournisseur/règlement (P5) avec un seul mécanisme —
  propriété de la table à date, l'appartenance module Java (`accounting` vs
  `installment`) reste à trancher en Phase 4.
- **Pas de table dédiée pour le règlement fournisseur (P5) ni la paie
  (P6/P7)** : ces flux postent directement des `journal_entry`/
  `journal_entry_line`, sans métadonnées supplémentaires à persister (à la
  différence de `payment`, qui porte le mode/la date de valeur/le lot pour
  les règlements copropriétaire, et d'`expense`, qui porte
  catégorie/fournisseur/justificatif pour les factures).

## Périmètre réel de la Phase 3 : domaine pur, provisioning différé à l'adapter JPA

La feuille de route initiale groupait « provisioning PCM (compte caisse à la
property, compte lot à l'unit) » dans la Phase 3. En écrivant le code, ce
provisioning s'est avéré être un cas à cheval sur deux couches :

- Le calcul du numéro de compte (`AccountNumber.forSequence(prefix,
  increment)`) et la construction d'un `LedgerAccount` valide sont de la
  logique domaine pure, sans aucune dépendance à un repository — livrés
  dans cette phase.
- L'allocation réelle de l'incrément (lecture/verrou de
  `ledger_account_number_sequence`) et l'appel effectif depuis
  `CreatePropertyService`/`AddUnitService`/`ConfigurePropertyService`
  nécessitent une implémentation JPA de `LedgerAccountRepository` — encore
  inexistante à ce stade.

Écrire ce câblage maintenant (use case + port property→accounting, sans
adapter JPA derrière) aurait cassé le démarrage de l'application au premier
test `@SpringBootTest` (bean Spring dépendant d'un port sans implémentation).
Pour garder le projet toujours compilable et démarrable à chaque étape
(comme en Phase 2 : 620 puis 648 tests verts sans interruption), le
câblage de bout en bout (property/unit créent réellement leur(s) compte(s)
PCM) est reporté à la Phase 6 (adapters), qui introduira l'adapter JPA de
`LedgerAccountRepository` en même temps que le use case de provisioning et
son branchement dans `property`. Jusque-là, la création d'une property ou
d'un lot ne provisionne encore aucun `LedgerAccount` (même état que fin de
Phase 2).

## Périmètre réel de la Phase 4

Comme en Phase 3, extension du domaine `installment` uniquement — pas de
nouveau bean Spring sans implémentation, pas de câblage cross-module
supplémentaire :

- **`SharesApportionment`** (I9, méthode du plus grand reste) remplace la
  logique « le dernier lot absorbe l'arrondi » de
  `GenerateInstallmentCallService` — c'est un remplacement direct dans une
  use case déjà branchée (aucun nouveau port), donc sans risque pour le
  démarrage de l'app. I10 (somme des tantièmes d'une clé = total déclaré)
  n'a pas de champ « total déclaré » séparé dans oikos aujourd'hui
  (`Unit.shares` n'a pas de clé de répartition dédiée) : l'invariant est
  satisfait par construction (le dénominateur est toujours la somme réelle
  des parts passées à l'algorithme), pas vérifié contre une valeur stockée
  séparément — à revisiter si un jour une vraie entité « clé de
  répartition » est introduite.
- **`InstallmentCall`** porte désormais un statut de cycle de vie
  (`DRAFT`/`ISSUED`/`POSTED`/`CANCELLED`) et un `journalEntryId` optionnel
  (entité JPA + mapper mis à jour en même temps, puisqu'il s'agit d'un
  agrégat déjà persisté et câblé — contrairement à `LedgerAccount` en
  Phase 3, l'étendre ne casse rien). `create(...)` reste `POSTED` par
  défaut pour ne pas changer le comportement actuel de
  `GenerateInstallmentCallService`/`RecordInstallmentCallService` ; un
  nouveau facteur `draft(...)` prépare le vrai flux P1
  (émission/comptabilisation) qu'une phase ultérieure câblera.
- **`Payment`/`PaymentMode`/`PaymentRepository`** et
  **`PaymentAllocationCalculator`** (FIFO créance/avance, I8),
  **`AdvanceConsumptionCalculator`** et **`UnitPositionStatus`**/
  **`UnitPositionStatusCalculator`** sont du domaine pur, testés isolément.
  Aucun use case ne les appelle encore : la comptabilisation réelle d'un
  règlement (écriture + `Allocation` persistée liant des
  `journal_entry_line`) est différée à la Phase 5/6, une fois l'adapter
  JPA de `JournalEntryRepository` disponible — même raisonnement qu'en
  Phase 3.

## Périmètre réel de la Phase 5

Même politique qu'en Phase 3/4 : uniquement du domaine pur, aucun nouveau
bean Spring sans implémentation derrière.

- **P10 (contre-passation)** : `JournalEntry.mirrorLinesForReversal(...)`
  (construit les lignes miroir, sens inversé, libellé préfixé « Extourne: »)
  et `JournalEntry.markReversed()` (transition POSTED→REVERSED, lignes
  intactes). La factory `draft(...)` accepte désormais un `originalEntryId`
  optionnel (surcharge rétrocompatible) pour construire l'écriture miroir.
  Construire réellement la nouvelle écriture (nouvel id, nouvelle période,
  numéro de pièce) et enchaîner les deux opérations dans une transaction
  reste un use case de Phase 6.
- **P4 (facture fournisseur)** : nouvel agrégat `Expense`, créé avec sa
  `JournalEntryId` (même module qu'`accounting`, donc type fort — contexte
  différent de `Payment.journalEntryId` qui reste un `EntityId` générique
  puisque `Payment` vit dans `installment`).
- **P8 (clôture de période)** : `PeriodClosingValidator`, fonction pure
  retournant la liste des violations (`EXERCICE_NON_CLOTURABLE`, spec
  §12) plutôt que de lever une exception à la première erreur trouvée —
  correspond au besoin de renvoyer le détail complet des contrôles en
  échec en une seule réponse.
- **P9 (clôture d'exercice)** : `ExerciseClosingCalculator`, deux fonctions
  pures — `closeIncomeStatement` (solde les classes 6/7, calcule le
  résultat, retrouve exactement les chiffres du jeu de données de
  référence : produits 3000, charges 2710, résultat +290) et
  `generateOpeningBalances` (à-nouveaux des classes de bilan, gère
  correctement un solde anormal comme la banque à découvert du §11).
  Persister effectivement la nouvelle écriture AN sur l'exercice suivant
  reste un use case de Phase 6.
- **P5/P6/P7 (règlement fournisseur, charges de personnel, frais
  bancaires)** : volontairement non traités en Phase 5. Ce sont des
  orchestrations de mécanismes déjà en place (résoudre un `LedgerAccount`
  par rôle, construire des `JournalEntryLine`, poster via `JournalEntry`)
  sans nouveau concept de domaine à modéliser — les écrire maintenant sans
  les adapters pour les exécuter réellement (même contre un test
  d'intégration) risquerait de devoir les reprendre en Phase 6. Reportés
  et fusionnés avec le câblage de cette phase.

## Périmètre réel de la Phase 6 (livré ce tour-ci)

Chantier volontairement recentré sur le cœur de l'« exigence
supplémentaire » plutôt que sur l'intégralité du périmètre P1-P10 de la
spec, pour livrer quelque chose de réellement câblé et vérifié plutôt que
d'étaler un travail à moitié fonctionnel sur davantage de use cases :

- **JPA `LedgerAccount`** (entité, repository Spring Data, mapper
  MapStruct, adapter) — `account_class` corrigé en `INTEGER` dans `V11`
  (au lieu de `SMALLINT`) après que
  `FlywayMigrationPostgresIntegrationTest` (validation de schéma Hibernate
  contre un vrai Postgres) a détecté l'écart type Java/colonne.
- **`LedgerAccountNumberSequenceRepository`** implémenté par verrou
  pessimiste JPA (`@Lock(PESSIMISTIC_WRITE)`), portable H2/Postgres, avec
  gestion de la course sur la toute première allocation d'un (property,
  prefix) par retry sur violation de contrainte unique. Rejoint la
  transaction appelante (pas de `REQUIRES_NEW`) : un rollback de la
  création property/unit annule aussi l'incrément alloué — pas de trou de
  numérotation orphelin.
- **Provisioning réellement câblé** : `CreatePropertyService` et
  `ConfigurePropertyService` provisionnent le compte caisse
  (`ROLE_CASH`, préfixe `516100`) ; `AddUnitService` et
  `ConfigurePropertyService` provisionnent le compte créance dédié du lot
  (`ROLE_UNIT_RECEIVABLE`, préfixe `341150`), via un nouveau port
  `LedgerAccountProvisioningPort` (property → accounting, même patron que
  l'ancien `UnitAccountProvisioningPort`).
- **Endpoint de lecture** `GET /properties/{propertyId}/accounting/ledger-accounts`
  (spec §7.1, `GET /referentiel/comptes`, scopé à une property) — retourne
  les comptes partagés (à seeder via `V11` en profil non-dev) et ceux
  propres à la property/ses lots.
- **Vérifié de bout en bout sur l'app réellement lancée** (profil dev, pas
  seulement des tests) : `POST /users/register-property-manager-admin` →
  vérification email → login → `POST /properties/{id}/buildings` →
  `POST /buildings/{id}/units` (deux lots) → `GET .../ledger-accounts`
  confirme `51610001` (CASH, sans lot) puis `34115001` et `34115002`
  (UNIT_RECEIVABLE, un par lot, incrément séquentiel correct).
- **699 tests, 0 échec**, y compris `FlywayMigrationPostgresIntegrationTest`
  (schéma V11-V14 validé contre un vrai Postgres via Hibernate `validate`).

**Reste à faire pour clore la Phase 6** : JPA pour `JournalEntry`/`Period`/
`Expense`/`Payment` ; use cases `@Component` pour P1-P10 (émission/
comptabilisation d'appel de fonds, règlement copropriétaire/fournisseur,
personnel, frais bancaires, clôtures, contre-passation) et leurs
contrôleurs REST ; `@RestControllerAdvice` mappant les exceptions du
domaine vers les codes `problem+json` du §12 ; rôles de sécurité fins
(§9) ; idempotency-key. Le référentiel `V11` (plan de comptes partagé,
journaux) n'est pas encore semé en profil dev (H2 `create-drop` ignore
les migrations Flyway) — seuls les comptes provisionnés dynamiquement
apparaissent tant qu'aucun seed dev n'est ajouté.

## Périmètre réel de la Phase 6 (suite) — le moteur générique d'écritures

Deuxième passe de la Phase 6, une fois le provisioning livré : rendre le
cœur du moteur (référentiel + écritures, posé en Phase 3) réellement
utilisable via l'API, pas seulement testé en domaine pur.

- **JPA `Period`** (entité/repository/mapper/adapter) et **`OpenAccountingExerciseService`
  étendu** : ouvrir un exercice génère désormais une `Period` `OPEN` pour
  chaque mois couvert (spec §4.1) — il n'y a pas d'endpoint "ouvrir une
  période" séparé dans la spec, donc génération éager plutôt que
  paresseuse.
- **`EnforceExerciseOpenService.requireOpenPeriod(exercise, pieceDate)`**
  ajouté (I5) : résout la période couvrant la date de pièce, rejette si
  absente ou clôturée (`PeriodNotOpenException`, 409).
- **JPA `JournalEntry`/`JournalEntryLine`** : les lignes ne sont écrites
  qu'une seule fois à la création (`newEntity()`) ; toute sauvegarde
  ultérieure (transition de statut) passe par `updateScalarFields()` qui ne
  touche jamais la collection de lignes déjà persistée — cohérent avec I4
  et les triggers SQL de la V12. Colonne `line_order` ajoutée à la table
  `journal_entry_line` (V12) pour que `@OrderColumn` préserve l'ordre des
  lignes tel que fourni par l'appelant.
- **`sequence_piece`** (I7) alloué par le même patron de verrou pessimiste
  JPA que la séquence de numéro de compte (Phase 6, première passe) —
  rejoint la transaction appelante : une validation qui échoue (ex.
  écriture déséquilibrée) annule aussi l'allocation, pas de trou de
  numérotation. Vérifié manuellement (voir plus bas).
- **Use cases génériques** : `CreateJournalEntryDraftUseCase` (les lignes
  référencent un `LedgerAccountId` directement, choisi par l'appelant —
  contrairement aux futurs use cases métier P1-P10 qui résoudront le
  compte via un `AccountRole`), `PostJournalEntryUseCase` (alloue le
  numéro de pièce puis applique I1), `GetJournalEntryUseCase`,
  `ListJournalEntriesByPropertyUseCase` (paginé, trié par date de pièce
  décroissante — pas encore les filtres complets du §7.3).
- **Contrôleur REST** `POST/GET /properties/{id}/accounting/entries`,
  `POST .../entries/{id}/validation` — nommage nesté sous `/properties/{id}`
  comme le reste de l'app plutôt que le chemin générique `/ecritures` du
  §7.3, pour rester cohérent avec `AccountingExerciseController`/
  `InstallmentCallController`.
- **Aucun nouveau mécanisme d'erreur** : les exceptions du domaine
  (`UnbalancedJournalEntryException` etc.) héritent déjà de
  `BusinessException`/`ConflictException`/`ResourceNotFoundException`
  existants, donc le `GlobalExceptionHandler` partagé les mappe
  correctement (400/409/404) sans modification. Les codes machine exacts du
  §12 (`ECRITURE_DESEQUILIBREE`, etc.) resteraient à ajouter si le contrat
  d'erreur `problem+json` de toute l'app est un jour étendu avec un champ
  `code` — hors scope pour un seul module.
- **Vérifié de bout en bout sur l'app réellement lancée** : ouverture d'un
  exercice (périodes générées), création d'une écriture CA (caisse débit /
  créance lot crédit, 300 MAD), validation → `pieceNumber=1`,
  `status=POSTED` ; nouvelle tentative de validation → 409 (I4) ; écriture
  volontairement déséquilibrée (150 vs 100) → 400 avec l'écart exact dans
  le message (I1) ; écriture suivante correctement équilibrée → validée
  avec `pieceNumber=2` (pas de trou malgré la tentative échouée
  précédente, confirmant le rollback transactionnel de l'allocation I7).
- **724 tests, 0 échec**, `FlywayMigrationPostgresIntegrationTest` toujours
  vert (schéma `JournalEntry`/`Period`/`SequencePiece` validé contre un
  vrai Postgres, colonne `line_order` ajoutée à V12).

**Reste pour clore complètement la Phase 6** : use cases métier P1-P10
(résolution par `AccountRole`, pas par id direct — appels de fonds,
règlements, factures, clôtures, contre-passation) et leurs endpoints
dédiés ; JPA `Expense`/`Payment` ; filtres complets de `GET /ecritures`
(journal, compte, auxiliaire, statut, dates) ; rôles de sécurité fins
(§9) ; idempotency-key ; seed du plan de comptes partagé en profil dev.

## Périmètre réel de la Phase 6 (suite 2) — P1, premier use case métier bout en bout

Troisième passe : le premier use case qui résout des comptes **par rôle**
plutôt que par id direct, prouvant que le moteur générique (Phase 6 suite)
et le référentiel de rôles (Phase 3) s'assemblent correctement.

- **`accounting.PostFundCallJournalEntryUseCase`/`Service`** : résout
  `ROLE_DUES_INCOME` (compte global) et, pour chaque lot chargé,
  `ROLE_UNIT_RECEIVABLE` scopé à ce lot (`findByPropertyIdAndUnitIdAndRole`)
  — jamais de numéro de compte en dur. Compose les use cases génériques
  déjà existants (`CreateJournalEntryDraftUseCase` +
  `PostJournalEntryUseCase`) plutôt que de reparler à
  `JournalEntryRepository` directement, pour garder I1/I2/I5/I7 appliqués à
  un seul endroit. `AccountRoleNotConfiguredException` (nouvelle, spec §12
  `ROLE_COMPTABLE_NON_PARAMETRE`) hérite de `RuntimeException` nu — pas de
  `BusinessException`/`ConflictException` — pour retomber sur le handler
  générique 500 du `GlobalExceptionHandler` (bug de provisioning, pas
  erreur client).
- **Nouveau port cross-module** `installment.application.port.out.FundCallJournalEntryPort`
  + `InstallmentFundCallJournalEntryAdapter`, même patron que les ports
  cross-module existants (délègue au port-in public d'accounting, jamais à
  son modèle de domaine).
- **`GenerateInstallmentCallService` complète enfin le cycle de vie P1** :
  `InstallmentCall.draft(...).issue()` (au lieu du raccourci `create()`
  direct-à-`POSTED` d'avant), génère les `Installment`, poste l'écriture
  VT via le nouveau port, puis `call.post(journalEntryId)`. Si aucun lot
  n'est chargé (tous sans prix/tantièmes), retombe sur l'ancien `create()`
  direct — pas d'écriture à générer, pas de lignes possibles (I2). Date de
  pièce = premier jour de la période appelée (pas la date d'échéance, qui
  peut tomber sur un autre mois). `GenerateInstallmentCallCommand` porte
  désormais `createdByUserId` (le contrôleur l'extrait de
  `Authentication`).
- **Seed du plan de comptes partagé ajouté à `db/dev/dev.sql`** (les 15
  comptes globaux de `V11`, dupliqués côté H2 `create-drop` puisque ce
  profil ignore Flyway) — comble un des manques listés en fin de Phase 6
  précédente, nécessaire pour que `ROLE_DUES_INCOME` résolve en dev.
- **Vérifié de bout en bout sur l'app réellement lancée** : property créée
  (compte caisse auto-provisionné), prix de type de lot configuré à 300,
  deux lots créés (comptes créance auto-provisionnés), exercice ouvert,
  `POST /properties/{id}/installment-calls` → 201 avec les deux lots
  chargés → `GET .../accounting/entries` confirme une écriture VT
  `POSTED`, `pieceNumber=1`, deux lignes débit de 300 (une par lot, avec
  l'auxiliaire = unitId) et une ligne crédit de 600 sur le compte
  Cotisations partagé.
- **726 tests, 0 échec** (84 pour le seul module `installment`), ArchUnit
  et `FlywayMigrationPostgresIntegrationTest` toujours verts.

**Reste** : P2/P3 (règlement copropriétaire + imputation FIFO déjà prête en
domaine depuis la Phase 4, juste à câbler), P4-P9, contre-passation P10
câblée en use case, exposer `status`/`journalEntryId` sur les vues
`InstallmentCall` (actuellement invisibles dans les réponses REST bien que
présents en domaine).

## Périmètre réel de la Phase 6 (suite 3) — P2/P3, règlement copropriétaire

Quatrième passe : le règlement d'un copropriétaire et son imputation FIFO
sur les appels de fonds impayés (spec §4.1/§4.2), sur le même patron que
P1 — un use case métier qui résout des comptes par `AccountRole`.

- **`accounting.PostOwnerPaymentJournalEntryUseCase`/`Service`** : pose
  l'écriture de trésorerie (journal `CA` ou `BQ` selon `treasuryRole`) —
  une ligne débit sur le compte de trésorerie pour le montant total reçu,
  une ligne crédit sur le compte `UNIT_RECEIVABLE` **dédié au lot**
  (`findByPropertyIdAndUnitIdAndRole`) pour la part imputée, une ligne
  crédit sur le compte `UNIT_ADVANCE` pour le reliquat en avance — chaque
  ligne omise si son montant est nul (au moins 2 lignes restent garanties
  puisque imputé+avance = montant reçu > 0). Même composition
  `CreateJournalEntryDraftUseCase` + `PostJournalEntryUseCase` que P1.
  **Bug attrapé à la vérification manuelle, pas par les tests unitaires** :
  `UNIT_ADVANCE` est semé comme compte **global** partagé (`property_id
  NULL`, même patron que `DUES_INCOME`/`SUPPLIER` — V11), pas comme
  singleton par property comme `CASH`/`BANK` ; la première version du
  service appelait `findByPropertyIdAndRole` (qui ne matche que
  `property_id` non nul) et échouait systématiquement dès qu'une avance
  était générée. Corrigé en `findGlobalByRole`, tests unitaires mis à jour
  en conséquence — un rappel que le comportement des repositories mockés
  ne garantit rien sur le vrai schéma de résolution des comptes globaux
  vs scopés.
- **`installment.application.port.out.OwnerPaymentJournalEntryPort`** +
  `InstallmentOwnerPaymentJournalEntryAdapter` : même patron cross-module
  que `FundCallJournalEntryPort`. Traduit `PaymentMode` en `AccountRole`
  trésorerie côté adapter (`CASH` → `AccountRole.CASH`, tout le reste —
  virement/chèque/prélèvement — → `AccountRole.BANK`), simplification
  documentée (un seul compte banque par property pour l'instant, pas un
  compte par moyen de paiement).
- **`installment.RecordOwnerPaymentUseCase`/`Service`** (P2+P3 réunis en
  un seul use case, décision de périmètre) : calcule la ventilation FIFO
  via `PaymentAllocationCalculator` (déjà prêt depuis la Phase 4) sur les
  `Installment` non soldés du lot (`outstandingAmount > 0`), poste
  l'écriture via le port cross-module, puis met à jour
  `Installment.outstandingAmount` **directement via `InstallmentRepository`**
  — pas via le port-in `UpdateInstallmentSettlementUseCase` existant
  (celui-ci reste non câblé, réservé à un futur flux déclenché côté
  accounting, ex. P10 annulant un règlement) — puisque l'orchestration vit
  déjà dans le module qui possède `Payment` et `Installment`.
  **Simplification assumée et documentée** : la table générique
  `allocation` (V14, liant deux `journal_entry_line` pour la piste d'audit
  complète I8) n'est pas encore alimentée ; la ligne de crédit imputée
  postée est un agrégat par règlement, pas une ligne par `Installment`
  soldé. La source de vérité du lettrage aujourd'hui reste
  `Installment.outstandingAmount`, mis à jour dans la même transaction que
  l'écriture. Alimenter `allocation` demanderait de faire remonter les
  `journal_entry_line_id` de P1 jusqu'à `Installment` (colonne déjà
  présente en DB depuis V13, jamais renseignée) — refonte de P1 déjà livré
  et vérifié, différée pour ne pas rouvrir ce périmètre dans cette même
  passe.
- **Persistence JPA de `Payment`** : `PaymentEntity`/`PaymentJpaRepository`/
  `PaymentPersistenceMapper`/`PaymentRepositoryAdapter`, même patron que
  `Installment` — `Payment` n'est cependant jamais modifié après création
  (pas d'équivalent `withOutstandingAmount`), donc `save()` ne fait qu'un
  insert, jamais de find-then-merge.
- **Contrôleur REST** `POST /properties/{propertyId}/units/{unitId}/payments`
  (sécurité : `canWriteInstallmentCall`, réutilisée telle quelle — pas de
  permission dédiée aux règlements pour l'instant) et
  `GET /units/{unitId}/payments` (sécurité : `managesUnit`/`ownsUnit`,
  comme `GET /units/{id}/installments`).
- **Vérifié de bout en bout sur l'app réellement lancée**, deux scénarios :
  - Règlement CASH de 300 MAD sur un appel de fonds de 300 → écriture `CA`
    postée (`pieceNumber=1`), 2 lignes (débit caisse 300 / crédit créance
    lot 300, auxiliaire = unitId), `Installment.status` passe à `SETTLED`,
    `outstandingAmount=0`, avance = 0.
  - Règlement CASH de 500 MAD sur un appel de fonds de 300 (property
    distincte) → écriture `CA` postée, 3 lignes équilibrées (débit caisse
    500 / crédit créance lot 300 / crédit avance collective 200, les deux
    lignes crédit portent l'auxiliaire = unitId), `Installment` soldé,
    réponse API confirmant `advanceAmount=200`.
  - Règlement `BANK_TRANSFER` tenté sur une property sans compte `BANK`
    configuré (aucun endpoint de configuration manuelle n'existe encore,
    gap déjà connu de l'exigence supplémentaire d'origine) → 500
    `AccountRoleNotConfiguredException`, conforme au comportement voulu
    (spec §12, `ROLE_COMPTABLE_NON_PARAMETRE`) — pas un bug, juste
    l'absence attendue de configuration.
- **736 tests, 0 échec**, ArchUnit toujours vert.

**Reste** : P4-P9 (facture/règlement fournisseur, personnel, frais
bancaires, clôtures période/exercice), contre-passation P10 câblée en use
case, table `allocation` alimentée (piste d'audit I8 complète), endpoint de
configuration manuelle d'un compte `BANK` (gap pré-existant, bloque
aujourd'hui tout règlement par virement/chèque/prélèvement), exposer
`status`/`journalEntryId` sur les vues `InstallmentCall`, filtres complets
de `GET /ecritures`, rôles de sécurité fins (§9), idempotency-key.

## Périmètre réel de la Phase 6 (suite 4) — P4, facture fournisseur

Cinquième passe : la facture fournisseur (spec §6), premier use case métier
entièrement contenu dans `accounting` (contrairement à P1-P3, `Expense` vit
déjà dans ce module depuis la Phase 5 — pas de port cross-module à
construire côté `installment`).

- **`accounting.RecordExpenseUseCase`/`Service`** : le compte de charge
  (classe 6 — "Entretien et réparations", "Assurances", etc.) est choisi
  **directement par l'appelant** (`ledgerAccountId` porté par la commande),
  pas résolu par rôle — il n'existe pas un rôle fonctionnel unique pour
  « la charge », c'est un choix du gestionnaire à la saisie (cohérent avec
  le fait qu'`Expense.ledgerAccountId` était déjà un champ direct depuis la
  Phase 5). Le compte `ROLE_SUPPLIER`, lui, reste résolu par rôle
  (`findGlobalByRole` — compte global partagé, même famille que
  `DUES_INCOME`/`UNIT_ADVANCE`), avec `supplierPartyId` en auxiliaire
  (compte collectif, I6). Même composition
  `CreateJournalEntryDraftUseCase` + `PostJournalEntryUseCase` que P1/P2/P3,
  journal `AC`.
- **Nouveau port sortant `accounting.PartyDirectoryPort`** +
  `AccountingPartyDirectoryAdapter` (délègue à `party.GetPartyUseCase`) :
  premier point où `accounting` référence `party` — vérifie seulement que
  le `supplierPartyId` existe, pas de notion de "type fournisseur" séparée
  (le modèle `Party` ne distingue que `INDIVIDUAL`/`COMPANY`, un
  fournisseur est juste une `Party` comme une autre).
- **Persistence JPA d'`Expense`** : `ExpenseEntity`/`ExpenseJpaRepository`/
  `ExpensePersistenceMapper`/`ExpenseRepositoryAdapter`, même patron que
  `Payment` — jamais modifié après création, `save()` ne fait qu'un insert.
- **Contrôleur REST** `POST/GET /properties/{propertyId}/accounting/expenses`
  (sécurité : `canWriteAccounting`/`canReadAccounting`, réutilisées telles
  quelles). Liste non paginée (volume attendu faible, même choix que
  `GET /accounting/ledger-accounts`).
- **Vérifié de bout en bout sur l'app réellement lancée** : property créée,
  exercice ouvert, fournisseur créé (`party`), facture de 450 MAD sur le
  compte "Entretien et réparations" → écriture `AC` postée équilibrée
  (débit charge 450 / crédit fournisseur 450, auxiliaire = supplierPartyId
  sur la ligne crédit) ; fournisseur inconnu → 404 (`SupplierNotFoundException`).
- **738 tests, 0 échec**, ArchUnit toujours vert.

**Reste** : P5-P9 (règlement fournisseur, personnel, frais bancaires,
clôtures période/exercice), contre-passation P10 câblée en use case, table
`allocation` alimentée, endpoint de configuration manuelle d'un compte
`BANK`, exposer `status`/`journalEntryId` sur les vues `InstallmentCall`,
filtres complets de `GET /ecritures`, rôles de sécurité fins (§9),
idempotency-key.

## Périmètre réel de la Phase 6 (suite 5) — P5, règlement fournisseur

Sixième passe : le règlement d'un fournisseur (spec §6). Décision de
conception notable — **pas de nouvelle entité persistée**, contrairement à
P2/P3 (`Payment`).

- **`accounting.RecordSupplierPaymentUseCase`/`Service`** : débite le
  compte `ROLE_SUPPLIER` collectif (auxiliaire = `supplierPartyId`),
  crédite le compte de trésorerie (`CASH`/`BANK`, résolu par rôle,
  `findByPropertyIdAndRole`). Journal `CA`/`BQ` selon la méthode. Même
  composition `CreateJournalEntryDraftUseCase` + `PostJournalEntryUseCase`
  que P1-P4.
- **Aucune entité `SupplierPayment`/`Settlement`** : décision déjà actée
  dans le commentaire de la migration V14 elle-même ("No dedicated table
  for supplier settlements (P5)... both post directly through
  journal_entry/journal_entry_line") — l'écriture comptable **est** le
  registre, le solde d'un fournisseur se lit en sommant les lignes portant
  son auxiliaire sur le compte `SUPPLIER`. Contrairement au copropriétaire
  (P2/P3), un fournisseur n'a pas de notion d'échéance/relance par facture
  dans ce modèle, donc pas besoin d'un `outstandingAmount` équivalent à
  celui d'`Installment`. Cohérent avec le choix déjà documenté de ne pas
  encore alimenter la table `allocation` (I8) pour P2/P3 — même limite
  assumée ici : pas de lettrage facture-par-facture, juste un solde net par
  auxiliaire.
- **Nouveau `accounting.domain.valueobject.TreasuryMethod`** (`CASH`/`BANK`)
  : choix API-facing plus étroit que tout `AccountRole` (qui inclut aussi
  des rôles non-trésorerie) - se traduit vers `AccountRole`/`JournalCode`
  via deux méthodes utilitaires (`toAccountRole()`/`toJournalCode()`).
  Réutilisable tel quel si un futur P6/P7 (personnel, frais bancaires) a
  besoin du même choix trésorerie.
- **Contrôleur REST** `POST /properties/{propertyId}/accounting/supplier-payments`
  (sécurité : `canWriteAccounting`), réponse minimale
  (`{journalEntryId}` — pas de ressource à `GET` puisqu'il n'y a rien à
  persister au-delà de l'écriture elle-même, déjà consultable via
  `GET /accounting/entries/{id}`).
- **Vérifié de bout en bout sur l'app réellement lancée** : property
  créée, exercice ouvert, fournisseur créé, règlement de 250 MAD par
  `CASH` → écriture `CA` postée équilibrée (débit fournisseur 250 avec
  auxiliaire = supplierPartyId / crédit caisse 250).
- **743 tests, 0 échec**, ArchUnit toujours vert.

**Reste** : P6-P9 (personnel, frais bancaires, clôtures période/exercice),
contre-passation P10 câblée en use case, table `allocation` alimentée
(I8 complet, P2/P3 et P5), endpoint de configuration manuelle d'un compte
`BANK`, exposer `status`/`journalEntryId` sur les vues `InstallmentCall`,
filtres complets de `GET /ecritures`, rôles de sécurité fins (§9),
idempotency-key.

## Périmètre réel de la Phase 6 (suite 6) — P8, clôture de période

Septième passe : la clôture mensuelle (spec §6/§12
`EXERCICE_NON_CLOTURABLE`). Choisie avant P6/P7 (personnel/frais bancaires,
qui ne sont que des variations du patron P4/P5) et avant P9 (clôture
d'exercice, qui demande une nouvelle brique - calcul de balance générale -
et mérite sa propre passe dédiée).

- **Changement de signature de `PeriodClosingValidator.violations(...)`** :
  `allocatedPieceNumbers` passe de `List<Integer>` à
  `Map<JournalCode, List<Integer>>`. Raison trouvée en câblant le premier
  appelant réel : la numérotation de pièce (I7) est une séquence sans trou
  **par (property, exercise, journal)**, indépendante d'un journal à
  l'autre - regrouper toutes les pièces d'une période dans une seule liste
  aurait pu faire remonter un faux trou (deux journaux entrelacés) ou en
  masquer un vrai. Corrigé avant tout appelant réel n'existait encore
  (Phase 5 avait livré la fonction pure sans jamais la brancher) - test
  domaine mis à jour, plus un cas ajouté couvrant explicitement deux
  journaux chacun correctement contigu.
- **`JournalEntryRepository.findAllByPeriodId(PeriodId)`** (+ JPA + adapter)
  : nouvelle requête, tous statuts confondus - nécessaire pour rassembler
  les données que `PeriodClosingValidator` attend déjà en entrée.
- **`accounting.ClosePeriodUseCase`/`Service`** : résout l'exercice ouvert
  de la property (`EnforceExerciseOpenService`, réutilisé tel quel),
  vérifie que la période précédente (si elle existe) est déjà fermée
  (règle inter-périodes, hors du domaine pur `Period` qui ne connaît pas
  ses sœurs), regroupe les écritures de la période par `JournalCode`, puis
  délègue à `PeriodClosingValidator`. Violations non vides →
  `PeriodNotClosableException` (409, message listant chaque violation) ;
  sinon `Period.close(...)` (méthode domaine déjà prête depuis la Phase 5,
  aucune modification nécessaire) et persistance.
- **Vérification de trésorerie toujours désactivée** (`null` passé
  systématiquement) : aucune fonctionnalité de rapprochement bancaire
  n'existe encore - gap documenté, cohérent avec l'absence d'endpoint de
  configuration d'un compte `BANK`.
- **Contrôleur REST** `POST /properties/{propertyId}/accounting/periods/{yyyy-MM}/close`
  (sécurité : `canWriteAccounting`).
- **Vérifié de bout en bout sur l'app réellement lancée** : exercice 2026
  ouvert, facture fournisseur postée en janvier, clôture de janvier → 200
  `CLOSED` ; nouvelle tentative de clôture de janvier → 409 (déjà fermée) ;
  nouvelle facture datée de janvier après clôture → 409 (période fermée,
  I5) ; tentative de clôture de mars avant février → 409 ("Previous period
  is not closed yet") ; février puis mars fermés dans l'ordre → 200/200.
- **748 tests, 0 échec**, ArchUnit et
  `FlywayMigrationPostgresIntegrationTest` toujours verts.

**Reste** : P6/P7 (personnel, frais bancaires - même patron que P4/P5),
P9 (clôture d'exercice - nécessite un calcul de balance générale par
compte, la levée de la restriction `JournalCode.AN.postable()=false` pour
l'écriture d'à-nouveaux générée par le moteur lui-même, et l'ouverture de
l'exercice suivant), contre-passation P10 câblée en use case, table
`allocation` alimentée, endpoint de configuration manuelle d'un compte
`BANK`, exposer `status`/`journalEntryId` sur les vues `InstallmentCall`,
filtres complets de `GET /ecritures`, rôles de sécurité fins (§9),
idempotency-key.

## Périmètre réel de la Phase 6 (suite 7) — P6/P7, personnel et frais bancaires

Huitième passe : les deux derniers use cases "journal-entry-only" (même
décision de conception que P5 - pas de table dédiée, l'écriture est le
seul registre, cf. commentaire de la migration V14).

- **`accounting.RecordPayrollExpenseUseCase`/`Service`** (P6) : débite un
  compte de charge classe 6 choisi par l'appelant (ex. "Rémunérations du
  personnel"), crédite le compte collectif `ROLE_STAFF_PAYABLE`. Journal
  `OD` ("Opérations diverses" - aucun des 6 journaux fixes n'est dédié à
  la paie). **Pas d'auxiliaire** sur la ligne de crédit : `STAFF_PAYABLE`
  est semé `collective:false` (V11) - ce moteur suit un passif de paie
  global par property, pas un solde par employé (à la différence du
  fournisseur en P4/P5, où l'auxiliaire = `supplierPartyId` sur un compte
  collectif).
- **`accounting.RecordBankChargeUseCase`/`Service`** (P7) : débite un
  compte de charge classe 6 choisi par l'appelant (ex. "Services
  bancaires"), crédite directement le compte `BANK` de la property
  (résolu par rôle, `findByPropertyIdAndRole`). Journal `BQ` - c'est un
  mouvement de trésorerie réel (le frais est prélevé par la banque
  elle-même), contrairement à P6.
- **Petit refactor de cohérence** : les trois endpoints "sans entité
  dédiée" (P5/P6/P7) partagent maintenant une seule réponse minimale
  `JournalEntryReferenceResponse` (`{journalEntryId}`) au lieu d'un
  DTO par endpoint — `RecordSupplierPaymentResponse` (P5) supprimé et
  remplacé par ce type partagé, sans changement de comportement (aucun
  test ne référençait son nom).
- **Vérifié de bout en bout sur l'app réellement lancée** : exercice 2026
  ouvert, accrual de personnel de 3000 MAD → écriture `OD` postée et
  équilibrée (débit "Rémunérations du personnel" / crédit `STAFF_PAYABLE`,
  sans auxiliaire) ; tentative de frais bancaires sans compte `BANK`
  configuré → 500 `AccountRoleNotConfiguredException`, comportement
  attendu (même gap déjà documenté que pour BQ en P2/P3/P5) - le chemin
  heureux de P7 reste couvert par les tests unitaires uniquement tant que
  l'endpoint de configuration manuelle d'un compte `BANK` n'existe pas.
- **754 tests, 0 échec**, ArchUnit toujours vert.

**Reste** : P9 (clôture d'exercice), contre-passation P10 câblée en use
case, table `allocation` alimentée, endpoint de configuration manuelle
d'un compte `BANK` (bloque désormais la vérification manuelle complète de
P2/P3, P5 *et* P7 - devient le gap le plus rentable à combler), exposer
`status`/`journalEntryId` sur les vues `InstallmentCall`, filtres complets
de `GET /ecritures`, rôles de sécurité fins (§9), idempotency-key.

## Questions ouvertes

- Rôles de sécurité fins (`LECTEUR`/`SAISIE`/`COMPTABLE`/`ADMIN_COMPTABLE`/
  `AUDITEUR`, séparation des tâches double-validation) vs le RBAC actuel
  (property manager/board member) — à trancher en Phase 6.
- Compte d'avance par lot vs collectif (décision 5 ci-dessus) — à faire
  valider par un expert-comptable marocain.
- Second référentiel PCGE strict — différé ; le mécanisme de rôle
  fonctionnel le permet sans refonte du moteur.
- Export FEC et documents PDF (avis d'appel, états AG) — périmètre
  §7.6/§13 de la spec source, prévu en Phase 6/7, formats à affiner à ce
  moment.
