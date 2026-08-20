# Nomenclature FR → EN

Référencé par [ARCHITECTURE.md, règle 2](../ARCHITECTURE.md#règle-2--nomenclature-en-anglais).
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

Cette règle vaut pour toute personne du produit, pas seulement pour une
`Party` : `app_user.full_name`, `party_invitation_token.full_name`, et depuis
la migration `V2`, `onboarding_lead.full_name` — cette dernière table était la
seule à couper le nom en deux, parce que le formulaire de l'étape 1 du wizard
demandait « Prénom » et « Nom ». Côté clients, tout écran de saisie d'un nom
affiche donc un unique champ « Nom complet », borné à 200 caractères.

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

## Refonte comptabilité PCM (`accounting`/`installment`, ADR 0001)

Remplacement du modèle simplifié (`FinancialAccount`/`UnitAccount`) par un
moteur en partie double conforme au Plan Comptable Marocain. Voir
[docs/adr/0001-comptabilite-pcm-cadrage.md](adr/0001-comptabilite-pcm-cadrage.md)
pour le détail des décisions (frontières de modules, isolation multi-syndic,
référentiel unique v1, compte par lot vs collectif). Chantier phasé — cette
table sera complétée au fil des phases 2 à 8.

| Terme FR (spec) | Nom dans le code (EN) | Module | Notes |
|---|---|---|---|
| Compte (du plan de comptes) | `LedgerAccount` | `accounting` | Remplace `FinancialAccount`/`UnitAccount` (supprimés). Id = GUID (`EntityId`) ; numéro de compte PCM = colonne métier texte séparée (`accountNumber`), jamais réutilisée comme clé technique. |
| Numéro de compte | `accountNumber` (String) | `accounting` | Jamais codé en dur hors seed du plan de comptes et adapters de provisioning (property → caisse, unit → créance). |
| Compte collectif / compte mouvementable directement | `collective` (boolean) | `accounting` | Un seul booléen : `!collective` ⇔ directement mouvementable. Jamais les deux vrais à la fois. |
| Sens normal / sens d'une ligne d'écriture | `AccountSide` (nature du compte) / `EntryDirection` (`DEBIT`/`CREDIT`, ligne d'écriture) | `accounting` | Enum de direction unique et partagée, destinée à remplacer les enums de direction dupliquées de l'ancien modèle (`FinancialEntryDirection`, `UnitAccountMovementDirection`). |
| Nature du compte | `AccountNature` (`BALANCE_ASSET`/`BALANCE_LIABILITY`/`EXPENSE`/`INCOME`) | `accounting` | Bilan actif/passif, charge, produit. |
| Rôle fonctionnel (`ROLE_CREANCE_COPRO`, etc.) | `AccountRole` (`UNIT_RECEIVABLE`/`UNIT_ADVANCE`/`DUES_INCOME`/`BANK`/`CASH`/`SUPPLIER`/`STAFF_PAYABLE`) | `accounting` | Porté directement par `LedgerAccount` (colonnes `role`/`unitId`, pas de table de jointure séparée — voir ADR 0001, section « Affinements de schéma »). Aucune règle métier ne référence un numéro de compte en dur. |
| Journal (VT/BQ/CA/AC/OD/AN) | `Journal` (catalogue global, non scopé par property) / `JournalType` | `accounting` | `TREASURY` pour BQ et CA ; le compte de trésorerie précis mouvementé est porté par `JournalEntry.treasuryAccountId`, choisi à la saisie (pas de "journal instance" par property/par compte bancaire). |
| Écriture comptable / ligne d'écriture | `JournalEntry` / `JournalEntryLine` | `accounting` | Remplace l'ancien `FinancialJournalEntry` (qui n'était pas en partie double — une seule direction sur un seul compte). |
| Pièce / séquence de pièce | `pieceNumber` / `PieceSequence` | `accounting` | Allocation sous verrou, unicité par (property, exercise, journal) — même principe que le numéro de compte incrémental provisionné à la création property/unit. |
| Exercice / période | `AccountingExercise` (existant, étendu) / `Period` | `accounting` | Ajout d'un statut par période mensuelle (`OPEN`/`CLOSED`) en plus du statut d'exercice existant. |
| Contre-passation (P10) | `JournalEntry.mirrorLinesForReversal(...)` + `markReversed()` / `originalEntryId` | `accounting` | Écriture miroir liée à l'originale (`draft(..., originalEntryId)`), qui passe en `REVERSED` sans que ses lignes soient modifiées. |
| Facture fournisseur (P4) | `Expense` | `accounting` | Créée avec sa `JournalEntryId` (type fort, même module) ; `supplierPartyId` reste un `EntityId` générique (module `party`). Câblée bout en bout via `RecordExpenseUseCase` (ADR 0001 Phase 6 suite 4) : compte de charge choisi directement par l'appelant (pas de rôle unique pour "la charge"), compte `ROLE_SUPPLIER` résolu par rôle. Persistée (JPA). |
| Contrôles de clôture de période (P8) | `PeriodClosingValidator` / `ClosePeriodUseCase` | `accounting` | Fonction pure retournant la liste des violations (`EXERCICE_NON_CLOTURABLE`), pas une exception à la première erreur. Câblée bout en bout (ADR 0001 Phase 6 suite 6) : `allocatedPieceNumbers` est un `Map<JournalCode,List<Integer>>` (une séquence I7 par journal, pas une liste globale) ; règle inter-périodes (la précédente doit déjà être fermée) portée par le use case, pas par `Period` lui-même. Vérification de trésorerie toujours désactivée (pas de rapprochement bancaire). |
| Clôture d'exercice, résultat et à-nouveaux (P9) | `ExerciseClosingCalculator` | `accounting` | `closeIncomeStatement` (classes 6/7 → résultat) et `generateOpeningBalances` (à-nouveaux classes 1-5), fonctions pures sur des soldes déjà résolus. |
| Provisioning des comptes PCM (property → accounting) | `LedgerAccountProvisioningPort` (property, out) → `ProvisionPropertyCashAccountUseCase` / `ProvisionUnitReceivableAccountUseCase` (accounting, in) | `property` / `accounting` | Même patron que l'ancien `UnitAccountProvisioningPort` ↔ `CreateUnitAccountUseCase`. Câblé dans `CreatePropertyService`/`ConfigurePropertyService` (compte caisse) et `AddUnitService`/`ConfigurePropertyService` (compte créance du lot) — vérifié de bout en bout (ADR 0001, Phase 6). |
| Appel de fonds / ligne d'appel | `InstallmentCall` / `Installment` (existants, étendus) | `installment` | Déjà le bon modèle (un `Installment` par lot = déjà une "ligne d'appel"). Extension : cycle de vie `DRAFT`/`ISSUED`/`POSTED`/`CANCELLED` et lien vers la `JournalEntry` générée à la comptabilisation. |
| Règlement / mode de règlement | `Payment` / `PaymentMode` (`BANK_TRANSFER`/`CASH`/`CHECK`/`DIRECT_DEBIT`) | `installment` | VIREMENT→`BANK_TRANSFER`, ESPECES→`CASH`, CHEQUE→`CHECK`, PRELEVEMENT→`DIRECT_DEBIT`. `journalEntryId` requis (créé avec son écriture, même principe que l'ancien `Expense.journalEntryId`). Persisté (JPA) et câblé bout en bout via `RecordOwnerPaymentUseCase` (P2/P3, ADR 0001 Phase 6 suite 3). |
| Imputation FIFO créance/avance (P2/P3) | `PaymentAllocationCalculator` (calcul pur, domaine) → `RecordOwnerPaymentService` (orchestration, `installment`) | `installment` | Câblé bout en bout : ventile un règlement sur les `Installment` non soldés **et échus** (FIFO par date d'échéance ; le seuil `asOf` est appliqué par le calculateur lui-même — date de valeur pour un règlement, date de pièce pour une régularisation d'avance, cf. ADR 0001 « Correction — imputation limitée aux échéances échues »), source de vérité = `Installment.outstandingAmount` mis à jour dans la même transaction. La table générique `Allocation` (V14, piste d'audit I8 liant deux `journal_entry_line`) existe en schéma mais n'est **pas encore alimentée** — différé (ADR 0001, Phase 6 suite 3), pas de refonte du nom prévue. |
| Ventilation par tantièmes (I9) | `SharesApportionment` (méthode du plus grand reste) | `installment` | Remplace l'ancien arrondi "le dernier lot absorbe le reste" dans `GenerateInstallmentCallService`. |
| Consommation d'avance à l'émission (§4.2) | `AdvanceConsumptionCalculator` | `installment` | `min(avance disponible, montant appelé)`. |
| Position nette du lot | `UnitPositionStatus` (`OVERDUE`/`UP_TO_DATE`/`IN_ADVANCE`) | `installment` | Calculé par `UnitPositionStatusCalculator` à partir de `créance − avance`, jamais stocké — même principe que `InstallmentStatus`. |
| Règlement fournisseur (P5) | `RecordSupplierPaymentUseCase` / `TreasuryMethod` (`CASH`/`BANK`) | `accounting` | Câblé bout en bout (ADR 0001 Phase 6 suite 5). Débite `ROLE_SUPPLIER` (auxiliaire = supplierPartyId), crédite la trésorerie. **Pas d'entité persistée** (décision actée dès V14) : l'écriture est le seul registre, pas de `outstandingAmount` par facture comme pour `Installment`. `TreasuryMethod` réutilisé tel quel pour P7. |
| Personnel (P6) | `RecordPayrollExpenseUseCase` | `accounting` | Câblé bout en bout (ADR 0001 Phase 6 suite 7). Débite une charge classe 6 choisie par l'appelant, crédite `ROLE_STAFF_PAYABLE` (compte collectif mais **sans auxiliaire** - `collective:false` en V11 : passif de paie global par property, pas de solde par employé). Journal `OD`. Pas d'entité persistée (même décision que P5). |
| Frais bancaires (P7) | `RecordBankChargeUseCase` | `accounting` | Câblé bout en bout (ADR 0001 Phase 6 suite 7). Débite une charge classe 6 choisie par l'appelant, crédite directement le compte `BANK` de la property (résolu par rôle). Journal `BQ` (mouvement de trésorerie réel). Pas d'entité persistée. |
| Réponse minimale "écriture seule" (P5/P6/P7) | `JournalEntryReferenceResponse` (`{journalEntryId}`) | `accounting` (web) | Réponse partagée par les endpoints sans entité dédiée - remplace `RecordSupplierPaymentResponse` (P5, supprimé sans changement de comportement). |

## Module messagerie interne (`messaging`)

Nouveau contexte métier conçu directement en anglais (aucun renommage a
posteriori), table de correspondance avec le vocabulaire fonctionnel de la
SFD d'origine (en français) fournie ci-dessous par cohérence avec le reste
de ce document. `messaging` couple `property` (appartenance à une
copropriété), `party`/`user` (résolution `partyId → userId` du destinataire)
sans jamais dépendre de leur modèle de domaine/repository directement (règle
4/6, vérifiée par `DependencyRulesArchTest`) — voir
`messaging.application.port.out.{UserAccessPort,PropertyMemberDirectoryPort,
PartyAccountDirectoryPort}` et leurs adapters `Messaging...` (patron déjà
utilisé par `AccountingPropertyDirectoryAdapter`/`InvitationAccountDirectoryAdapter`
pour éviter toute collision de bean Spring).

| Terme FR (spec)                              | Nom dans le code (EN)                                    | Notes |
|-----------------------------------------------|-----------------------------------------------------------|-------|
| Conversation / fil de discussion               | `Conversation`                                             | Agrégat : `GROUP` (2..N `participantUserIds` choisis applicativement par l'émetteur — composition façon Outlook "À : A, B, C" — jamais réutilisée : composer un nouveau message vers le même ensemble de destinataires crée toujours une nouvelle conversation) ou `BROADCAST` (canal d'annonces persistant, unique par property, membres résolus dynamiquement — jamais stockés). |
| Type de conversation                           | `ConversationType` (`GROUP`/`BROADCAST`)                    | |
| Message                                        | `Message`                                                   | Immuable (patron `Movement`) : jamais modifié ni supprimé. |
| Corps du message                               | `MessageBody` (VO)                                          | Non vide, max 4000 caractères — mirroré côté web par `@NotBlank @Size(max = 4000)` sur `SendMessageRequest`. |
| Diffusion du bureau de syndic                  | canal `BROADCAST` (pas de type dédié)                        | Un canal persistant par property, jamais un message ponctuel isolé — `SendBroadcastMessageService` fait un find-or-create avant de poster. |
| Marqueur de lecture                            | `ConversationReadMarker`                                     | Upsert paresseux : créé au premier accès (y compris pour un `BROADCAST` jamais ouvert), `(conversationId, userId)` composite key. |
| Compteur de non-lus                            | `unreadCount` (`ConversationSummaryView`)                     | Calculé (jamais stocké) en comparant `message.created_date` au message pointé par le marqueur — pas de colonne de séquence dédiée (volumes faibles par conversation). |
| Destinataire potentiel                         | `RecipientCandidateView` (`userId`, `fullName`, `roleLabel`)  | `roleLabel` : libellé court FR ("Copropriétaire" / "Bureau de syndic"), résolu côté adapter à partir de `PropertyContactView`/`BoardMemberView`. |
| Permission de diffusion                        | `Permission.MESSAGING_BROADCAST` (`messaging:broadcast`)      | Accordée à `PROPERTY_BOARD_ADMIN`/`PROPERTY_BOARD_MEMBER`/`PROPERTY_MANAGER_ADMIN`/`PROPERTY_MANAGER_MEMBER` (même mirroring bureau/gérant que les permissions V2). Une conversation `GROUP` n'est gérée que par appartenance à la property (`isPropertyMember`), pas par permission fine. |
| Résolution parti → compte (changement additif `user`) | `FindUsersByPartyIdsUseCase`/`FindUsersByPartyIdsService`, `UserRepository.findByLinkedPartyIds` | Seul ajout hors module `messaging` (voir plan d'implémentation §1.3) : patron exact de `FindLinkedPartyIdsUseCase`/`FindLinkedPartyIdsService` déjà existant, mais retournant l'agrégat `User` (donc son id) plutôt qu'un simple test d'existence. |

## Module assemblée générale (`meeting`)

Nouveau contexte métier issu de la SFD « Module Assemblée Générale »
(français). Le vocabulaire anglais n'est pas libre : `NotificationType.GENERAL_MEETING_CALLED`
existait déjà avant le module et fixe « general meeting » comme traduction
d'« assemblée générale ». Les arbitrages fonctionnels qui expliquent
plusieurs des noms ci-dessous (le lot vote, pas le copropriétaire ; les
résultats ne sont pas stockés) sont dans
[docs/adr/0002-assemblee-generale-cadrage.md](adr/0002-assemblee-generale-cadrage.md).

Couplé à `property` (lots, tantièmes, mode de calcul), `party` (identité des
destinataires), `user` (résolution `partyId → userId`), `document` (pièces
jointes, PDF de convocation et de PV) et `notification` exclusivement via ses
propres `application.port.out`, implémentés par des adapters préfixés
`Meeting…` — jamais leur modèle de domaine, repository ou infrastructure
(règle 4/6, vérifiée par `DependencyRulesArchTest`).

| Terme FR (SFD)                       | Nom dans le code (EN)                            | Notes |
|---------------------------------------|---------------------------------------------------|-------|
| Assemblée générale (AG)               | `GeneralMeeting` (agrégat racine), `GeneralMeetingId` | Aligné sur `GENERAL_MEETING_CALLED`, antérieur au module. Module `meeting`, jamais `assembly` ni `ag`. |
| Ordinaire / extraordinaire            | `MeetingType` (`ORDINARY`/`EXTRAORDINARY`)         | Détermine aussi le seuil de quorum applicable (voir `MeetingQuorumSetting`). |
| Statut de l'AG                        | `MeetingStatus` (`DRAFT`/`SCHEDULED`/`CONVENED`/`IN_PROGRESS`/`CLOSED`/`MINUTES_PUBLISHED`) | Statut réel persisté (contrairement à `ConvocationStatus`/`InstallmentStatus`, dérivés) : les six statuts de la SFD, transitions portées par l'agrégat. |
| Date et heure de la séance            | `scheduledAt`                                      | Nullable tant que l'AG est en `DRAFT`. |
| Lieu (physique / visio / hybride)     | `MeetingVenue` (VO) + `VenueType` (`PHYSICAL`/`VIDEOCONFERENCE`/`HYBRID`) | Invariant : adresse requise si `PHYSICAL`/`HYBRID`, lien requis si `VIDEOCONFERENCE`/`HYBRID`. |
| Point de l'ordre du jour              | `AgendaItem`, `AgendaItemId`                       | `label`, `description`, `position`. L'ordre du jour n'est **pas** figé au passage en `SCHEDULED` — verrou levé, voir ADR 0002 §8. |
| Pièces jointes d'un point             | `DocumentOwnerType.AGENDA_ITEM`                    | Réutilisation du module `document` (stockage, checksum, téléchargement) — pas de stockage propre au module, patron `GeneratePaymentReceiptService`. |
| Pièces jointes de l'AG                | `DocumentOwnerType.GENERAL_MEETING`                | Les documents qui éclairent l'assemblée entière et non un point : budget, rapport, convocation type. Même mécanique que `AGENDA_ITEM`, un `ownerType` de plus — pas de table propre au module (ADR 0002 §12). |
| Commentaire global de l'AG            | `GeneralMeeting.comment` (HTML), `PUT /general-meetings/{id}/comment` | Note d'intention du syndic, en texte riche comme un message (`QuillEditor`). Verbe et endpoint dédiés plutôt qu'un champ de plus sur `update()` : il s'édite sur son propre écran, et le faire transiter par la mise à jour complète de l'AG rendrait possible d'écraser la date en enregistrant un commentaire. **Lu par les copropriétaires** (espace copropriétaire, mobile, et repris dans le PDF de convocation), donc jamais réinjecté brut : `sanitizeRichText` côté clients, texte brut côté PDF (§12). |
| Résultat de vote (`resultat_vote`)    | `VoteTally` (VO) + `VoteOutcome` (`ADOPTED`/`REJECTED`) | **Jamais stockés** : calculés à la lecture par `VoteTallyCalculator`/`MajorityRuleEvaluator` (convention `InstallmentStatus`/`OwnershipStatus`), figés une seule fois dans le contenu du PV à sa génération. |
| Règle de majorité                     | `MajorityRule` (`SIMPLE`/`ABSOLUTE`/`UNANIMITY`)    | Obligatoire, choisie point par point. Dénominateurs respectifs : voix exprimées, voix de la copropriété entière, voix présentes (voir ADR 0002 §4). |
| Quorum                                | `MeetingQuorumSetting` (`quorumPercentage` par `(property, meetingType)`) | Snapshoté sur `GeneralMeeting.quorumPercentage` à la création. `open()` refuse sans quorum, forçable explicitement (`openedWithoutQuorum`). |
| Pondération des voix                  | `VotingWeightMode` (`PER_UNIT`/`SHARES`)            | Copie locale au module de `property.domain.valueobject.DuesCalculationMode` (`FLAT_RATE`→`PER_UNIT`, `SHARES`→`SHARES`), traduite dans `MeetingPropertyDirectoryAdapter` — même patron que la copie déjà faite par `installment` (règle 4). |
| Convocation                           | `Convocation`, `ConvocationId`                      | Mot anglais juridiquement correct, sans collision avec le module `invitation` (onboarding), de sens totalement différent. Objet unique fusionnant envoi + confirmation + émargement, comme dans la SFD. **Une par lot**, pas par copropriétaire : unique `(generalMeetingId, unitId)`, FK réelle vers `unit` (patron `Installment.unitId`). |
| Canal d'envoi                         | `ConvocationChannel` (modèle de domaine, table de référence `convocation_channel`), `ChannelCode` (VO) | **Catalogue, plus un enum** : ajouter un canal doit être un `INSERT`, pas un déploiement (même bascule que `UnitType` → `UnitTypeDefinition`). Catalogue global, non scopé par `property`. `code`, `label`, `automated`, `position`, `active`. |
| Canal automatisé vs manuel            | `ConvocationChannel.automated`                      | `true` = l'application sait envoyer elle-même. Un canal manuel s'ajoute par un `INSERT` seul ; un canal automatisé exige **en plus** un émetteur dans le code — `ChannelCode.EMAIL`/`ChannelCode.APP` sont les seules constantes nommées côté Java, précisément parce qu'un émetteur existe pour elles. Une ligne `automated` sans émetteur est refusée par `ConvocationNotSendableException`. |
| Envoi effectif d'une convocation      | `ConvocationDelivery`, `ConvocationDeliveryId`       | **1—N** : la même convocation peut partir par plusieurs canaux (email puis recommandé), et chaque tentative — réussie ou non — reste lisible. Remplace le triplet `(channel, sentAt, deliveryStatus)` porté par la convocation, qui s'écrasait à chaque envoi. |
| Référence d'envoi                     | `reference`                                          | N° de suivi d'un recommandé — le seul canal qui en produit un. |
| Qui a envoyé / constaté la remise     | `recordedByUserId`                                   | |
| Date d'envoi / statut d'envoi         | `sentAt` / `DeliveryStatus` (`TO_SEND`/`SENT`/`FAILED`) | **Dérivés** des `ConvocationDelivery`, plus jamais stockés sur la convocation : aucune ligne → `TO_SEND`, au moins une `SENT` → `SENT`, des lignes toutes `FAILED` → `FAILED`. `sentAt` est la date du **premier** envoi abouti — celle qu'invoque une AG contestée. Une ligne de livraison ne porte, elle, que `SENT` ou `FAILED` (`TO_SEND` n'est l'état d'aucune ligne, c'est leur absence). |
| Comment la confirmation a été obtenue | `ReplySource` (`OWNER_APP`/`OWNER_LINK`/`OTHER`) | Enum de code et non catalogue, contrairement aux canaux : chaque valeur est un chemin de code distinct, pas une donnée. **Déduite de l'appelant côté serveur, jamais envoyée par le client** — sinon n'importe qui pourrait écrire « le copropriétaire a confirmé depuis l'app ». `OWNER_APP` = répondu depuis son espace authentifié, `OWNER_LINK` = via le lien reçu, sans compte, `OTHER` = ni l'un ni l'autre. Nommée `OTHER` et non d'après le bureau où la réponse arrive : ce que le serveur établit est seulement qu'aucun des deux chemins applicatifs n'a été emprunté. |
| Par quel moyen la réponse est arrivée | `ReplyMediumCode` + catalogue `reply_medium` | Catalogue et non enum, contrairement à `ReplySource` — et le contraste est le sujet. Rien ne branche dessus côté code, donc en ajouter un est un `INSERT` (même bascule que `ConvocationChannel`). Surtout, c'est une **déclaration du syndic** et non un fait que le serveur a constaté : seule la personne qui a pris l'appel sait qu'il s'agissait d'un appel. Les garder dans deux champs distincts empêche une déclaration de se faire passer pour une observation. N'a de sens que si la source vaut `OTHER`. |
| Historique des réponses               | `ConvocationReply` (1—N), `receivedAt` + `createdDate` | Une ligne par réponse donnée, ajoutée et jamais remplacée, **retraits compris**. La réponse qui fait foi reste recopiée sur la convocation (dénormalisation assumée, contrairement à `DeliveryStatus` qui est dérivé) : le quorum, le statut et les relances la lisent, et la dériver changerait tout cela pour économiser des colonnes. `Convocation.reply()` est le **point d'écriture unique** qui ajoute la ligne et reprojette les colonnes — c'est lui, et rien d'autre, qui rend la duplication sûre. Deux dates : `receivedAt` déclarée et antidatable, `createdDate` la saisie ; la règle est « la plus récemment reçue, à égalité la plus récemment saisie ». |
| Lien de confirmation reçu             | `confirmationToken` + `ConvocationTokenGenerator` | Jeton opaque de 32 octets, un par convocation, créé à la génération — le lien doit figurer sur la lettre, y compris celle qu'on imprime pour la poster. Même algorithme que `InvitationTokenGenerator` (le projet assume plusieurs générateurs quasi identiques côté à côte plutôt qu'une dépendance croisée). Jamais exposé dans les réponses JSON du back-office : il ne circule que dans l'email et le PDF. |
| Code de confirmation (lettre)         | `ConfirmationCode` (VO), colonne `confirmation_code`  | Six caractères `[0-9a-z]`, ex. `w754a1` : ce qu'on recopie d'une lettre papier quand on n'a pas scanné le QR. **Ne remplace pas le jeton**, il le double — un secret de 34^6 ne peut pas porter seul ce qu'un secret de 2^256 portait (ADR 0002 §13). Unique **par AG**, pas globalement : c'est le couplage à l'AG qui borne l'espace de recherche. `l` et `o` sont exclus de l'alphabet (confusion avec `1` et `0` sur du papier), les chiffres sont gardés. |
| Référence publique de l'AG            | `PublicReference` (VO), colonne `public_reference`   | Six caractères du même alphabet, unique globalement. Ce qui rend le couplage utilisable sur papier : sans elle, « coupler le code à l'AG » voudrait dire recopier un UUID. Publique par construction — elle ne protège rien, elle adresse. |
| QR code de la convocation             | `ConvocationQrCodeRenderer` (port out `QrCodeRendererPort`) | Encode le **lien à jeton**, pas le code : le scan n'a aucune raison de se rabattre sur le secret faible. PNG en `data:` URI dans le PDF — openhtmltopdf n'atteint aucune ressource externe (patron du reçu de paiement). |
| Plafond de tentatives                 | `ConfirmationAttemptLimiter`                        | En mémoire, par (AG, IP) : le code court n'est défendable que si on ne peut pas l'essayer en boucle. Portée d'une instance et remise à zéro au redémarrage — assumé et documenté, ce n'est pas un rate limiter distribué. |
| Confirmation sans compte              | `ConfirmConvocationByTokenUseCase` (`PUT /convocations/by-token/{token}/reply`), `GetConvocationByTokenUseCase` (`GET /convocations/by-token/{token}`) | Les deux seuls points d'entrée anonymes du module. Ils fixent `reply_source = OWNER_LINK` et laissent `repliedByPartyId` à `null` : le jeton prouve qu'on a reçu la convocation du lot, pas lequel des indivisaires clique. |
| Fermeture du lien                     | `ConfirmationClosedException`                       | Le lien n'a pas de durée de vie propre : il cesse d'accepter une réponse à l'ouverture de la séance. Au-delà, la présence est l'émargement et non plus une déclaration (ADR 0002 §5) — une date d'expiration arbitraire dirait moins et se désynchroniserait. |
| Auteur de la réponse                  | `repliedByPartyId` (nullable)                        | Renseigné quand la réponse vient d'un copropriétaire identifié ; `null` quand le syndic saisit sans savoir lequel des indivisaires a répondu. |
| Précision sur la réponse              | `replyNote` (nullable)                               | Le « message aux autres » de la demande : par quel biais la réponse est parvenue au bureau. |
| Document de convocation généré        | `DocumentOwnerType.CONVOCATION`                     | Porte aussi la signature d'émargement — même owner, deux fichiers. |
| Réponse de confirmation / date        | `AttendanceReply` (`ATTENDING`/`NOT_ATTENDING`/`NO_REPLY`) / `repliedAt` | `NO_REPLY` est la valeur par défaut, pas `null` : « n'a pas répondu » est un état, pas une absence de donnée. |
| Présence effective (émargement)       | `checkedIn` (boolean), `checkedInAt`                | Condition nécessaire au vote du lot. |
| Mode de présence                      | `AttendanceMode` (`ON_SITE`/`REMOTE`)               | |
| Représentant du lot en séance         | `checkedInPartyId` (nullable)                       | Seule trace nominative de l'émargement : lève l'ambiguïté quand le lot est en indivision. Ce n'est pas un mandat (procurations hors périmètre v1, ADR 0002 §7). |
| Statut global de la convocation       | `ConvocationStatus` (`TO_SEND`/`SENT`/`CONFIRMED`/`CHECKED_IN`) | Dérivé, calculé à la lecture, jamais stocké — c'est le `statut_global` de la SFD. |
| Poids de vote du lot                  | `VotingWeight` (VO), colonne `votingWeight`          | Snapshoté sur la `Convocation` à la génération : un résultat reste reproductible après une vente de lot, une correction de tantièmes ou une bascule de mode (même principe que RG002). |
| Décompte de présence (envoyées / confirmées / présentes, poids présent vs total) | `AttendanceTally` (VO du domaine) → `AttendanceSummaryView` | Calculé à chaque lecture depuis les convocations, jamais stocké. Porte les deux poids dont dépendent le quorum (présent/total) et la majorité absolue (total). |
| Marquage manuel de remise (courrier, recommandé, remise en main propre) | `RecordConvocationDeliveryUseCase` (`PUT /convocations/{id}/delivery-status`) | Verbe distinct de `SendConvocationUseCase` (`POST /convocations/{id}/send`) : l'un constate qu'une personne a agi, l'autre agit. Les confondre rendrait le tableau de suivi inexploitable. |
| Vote                                  | `Vote`, `VoteId`                                    | **Émis par le lot** : unique `(agendaItemId, unitId)`. `castByUserId` (nullable) ne trace que qui a saisi, jamais qui « possède » la voix. |
| Choix (pour / contre / abstention)    | `VoteChoice` (`FOR`/`AGAINST`/`ABSTENTION`)          | |
| Date du vote                          | `castAt`                                            | |
| Session de vote sur un point          | `VoteSessionStatus` (`NOT_OPENED`/`OPEN`/`CLOSED`)   | Vrai état de séance (non dérivable), porté par `AgendaItem` — à ne pas confondre avec le résultat, lui calculé. |
| Procès-verbal (PV)                    | `MeetingMinutes`, `MeetingMinutesId`                 | `MeetingMinutes` et non `Minutes` seul : table `meeting_minutes` et entité `MeetingMinutesEntity` restent lisibles. Relation 1—1 avec l'AG (unicité en base). |
| Statut du PV                          | `MinutesStatus` (`DRAFT`/`UNDER_REVIEW`/`PUBLISHED`) | `UNDER_REVIEW` = « en_validation » de la SFD, mais signifie « validé, texte gelé, en attente de diffusion » : valider est précisément ce qui arrête l'édition. Aucun retour en arrière. |
| Contenu du PV (`contenu`)             | `MeetingMinutes.content` (HTML) + `MinutesComposer` | **Le seul endroit du module où le calculé est figé.** Présences, dépouillement et résultats sont recalculés à la lecture partout ailleurs ; le composer les rend en texte une fois, et ce texte devient la vérité — une vente de lot ou une correction de tantièmes ultérieure ne réécrit pas une AG déjà actée. HTML parce que le PV s'édite en éditeur riche et s'imprime en PDF. |
| Document final du PV                  | `DocumentOwnerType.MEETING_MINUTES`                  | PDF rendu par Thymeleaf + openhtmltopdf, patron `ThymeleafPaymentReceiptRenderer`. |
| Notification in-app « best effort »   | `MeetingNotificationDispatcher`                     | Composant dédié dont les méthodes sont `REQUIRES_NEW`. Le nom dit l'intention, mais c'est la propagation qui fait le travail : sur la transaction de l'appelant, les services de notification (`REQUIRED`) la marquent `rollback-only` en échouant et le `try`/`catch` arrive trop tard (ADR 0002 §11). Un bean à part est indispensable — annoter une méthode privée ne passerait pas par le proxy. |
| Permissions                           | `MEETING_READ` (`meeting:read`), `MEETING_MANAGE` (`meeting:manage`), `MEETING_MINUTES_PUBLISH` (`meeting:minutes:publish`) | Même mirroring bureau/gérant que `MESSAGING_BROADCAST`. `meeting:read` est la seule accordée à `PROPERTY_OWNER` ; la publication du PV est séparée de la gestion parce qu'elle est irréversible et diffusée. |

## Comment utiliser cette table

- Avant d'implémenter une nouvelle SFD, traduire chaque terme métier ici
  s'il n'y est pas déjà, en vérifiant qu'il ne collisionne pas avec un
  terme déjà utilisé ailleurs dans le code pour un concept différent.
- Un terme FR peut apparaître dans plusieurs SFD avec des sens différents :
  vérifier le contexte avant de réutiliser un nom EN existant.
