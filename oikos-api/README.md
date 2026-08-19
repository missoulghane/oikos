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
- `messaging` — messagerie interne : conversations `GROUP` (composées par
  l'émetteur vers un ou plusieurs destinataires choisis parmi les membres
  d'une même copropriété — façon Outlook "Nouveau message" — stockées dans
  la table de jointure `conversation_participant`, jamais réutilisées :
  composer vers le même ensemble de destinataires crée toujours une nouvelle
  conversation, `StartGroupConversationService` ne fait jamais de
  find-or-create pour ce type) et un canal `BROADCAST`
  persistant par copropriété (diffusion du bureau de syndic vers tous les
  membres courants, jamais un message ponctuel isolé — trouvé-ou-créé par
  `SendBroadcastMessageService`). Les `Message` sont un historique immuable
  (jamais modifiés/supprimés), le non-lu (`ConversationReadMarker`, upsert
  paresseux) est calculé à la lecture en comparant les dates de création.
  Couplé à `property` (appartenance), `user` (résolution `partyId → userId`
  du destinataire, seul changement additif hors module — voir
  `docs/NOMENCLATURE.md`) et `party` (identité affichée) exclusivement via
  ses propres ports `application.port.out` (`UserAccessPort`,
  `PropertyMemberDirectoryPort`, `PartyAccountDirectoryPort`), jamais leur
  modèle de domaine ou repository directement (règle 4/6). Autorisation via
  `PropertyAccessEvaluator.{isPropertyMember,canBroadcastOnProperty,
  isConversationParticipant}` (`Permission.MESSAGING_BROADCAST` pour la
  diffusion ; une conversation `GROUP` n'est gérée que par appartenance à
  la property, pas par permission fine).
- `meeting` — assemblées générales. **Implémenté à ce stade** : l'AG
  (`GeneralMeeting`, cycle de vie en six statuts porté par l'agrégat, de
  `DRAFT` à `CLOSED`), son ordre du jour (`AgendaItem`, règle de majorité
  choisie point par point, réordonnancement), le paramétrage du quorum par
  nature d'AG (`MeetingQuorumSetting`) et les `Convocation` : génération pour
  **tous** les lots de la copropriété, envoi email + PDF (Thymeleaf/openhtmltopdf,
  stocké par le module `document`), relance des non-répondants, réponse
  présent/absent, émargement, tableau de suivi et calcul du quorum
  (`AttendanceTally`, recalculé à la lecture). Chaque point de l'ordre du jour porte ses **pièces
  jointes** (`DocumentOwnerType.AGENDA_ITEM`), l'AG les siennes
  (`GENERAL_MEETING`) plus un **commentaire global en texte riche**
  (`PUT /general-meetings/{id}/comment`) lu par les copropriétaires et repris
  dans la convocation — en texte brut dans le PDF, dont le moteur de rendu exige
  du XML bien formé (ADR 0002 §12). L'ordre du jour, la date et le lieu
  restent **modifiables à tout statut** : le produit ne veut pas de règle de
  gestion bloquante à ce stade (ADR 0002 §8, qui liste ce qui reste contraint
  et comment réintroduire le gel). **Générer et envoyer sont deux actions
  distinctes** : générer crée une convocation par lot et convoque l'AG sans
  rien expédier, envoyer expédie ensuite ce qui est encore en attente
  (`POST /general-meetings/{id}/convocations` puis
  `.../convocations/send`). Chaque envoi tourne dans sa propre transaction
  (`REQUIRES_NEW`) : un lot injoignable est marqué en échec et les autres
  partent quand même. Une convocation porte **plusieurs envois**
  (`ConvocationDelivery`, 1—N) : email puis recommandé pour le lot resté muet,
  chaque tentative conservée, aucun statut d'envoi stocké — il est dérivé de
  ces lignes. Les canaux sont un **catalogue en base**
  (`GET /convocation-channels`), pas un enum : en ajouter un est un `INSERT`,
  mais un canal marqué `automated` exige en plus un émetteur dans le code.
  Ceux que l'application ne sait pas effectuer ne sont jamais « envoyés » par
  elle, leur remise est saisie à la main
  (`PUT /convocations/{id}/delivery-status`). La confirmation de présence
  enregistre **comment** elle a été obtenue (`ReplySource`), déduite de
  l'appelant côté serveur et jamais envoyée par le client (ADR 0002 §9).
  Chaque convocation porte enfin un **lien de confirmation personnel**
  (`GET`/`PUT /convocations/by-token/{token}`, les deux seuls points d'entrée
  anonymes du module, écriture comprise) : les copropriétaires sans compte —
  et il y en a beaucoup — pouvaient jusqu'ici recevoir une convocation sans
  avoir aucun moyen d'y répondre. Le lien figure dans l'email **et** sur le PDF
  imprimé, cesse d'accepter une réponse à l'ouverture de la séance, et n'expose
  qu'une convocation : ni identifiant, ni autre lot, ni nom (ADR 0002 §10). La
  lettre porte trois voies vers la même page : un **QR code** (qui encode le
  lien à jeton, jamais le code court), l'adresse écrite, et un **code à six
  caractères** couplé à la référence publique de l'AG. Ce dernier est un secret
  faible assumé — d'où l'unicité par AG et le plafond de tentatives qui
  l'encadrent (ADR 0002 §13). Enfin les
  `Vote` : ouverture et clôture de scrutin par point, saisie nominative ou à
  main levée (un choix pour la salle, avec des exceptions nommées), et
  dépouillement (`VoteTally` + `MajorityRuleEvaluator`). Un lot ne vote que
  s'il a émargé — c'est le fait sur lequel le quorum a été calculé — et son
  poids est celui figé sur sa convocation, jamais relu. Le résultat porte les
  **trois** dénominateurs (voix exprimées, présentes, totales) pour que la
  règle de majorité reste une comparaison pure : la majorité `ABSOLUTE` se
  mesure sur l'ensemble de la copropriété, présents ou non. Rien n'est
  stocké : le dépouillement est recalculé à chaque lecture. Enfin le
  procès-verbal (`MeetingMinutes`) : généré depuis la séance close
  (`MinutesComposer`), complété par le syndic, validé — ce qui **gèle le
  texte** — puis publié (PDF classé par le module `document`, AG en
  `MINUTES_PUBLISHED`, notification aux copropriétaires). Le PV est le seul
  endroit du module où le calculé cesse de l'être : partout ailleurs les
  chiffres sont dérivés à la lecture pour ne jamais contredire leurs lignes,
  mais un procès-verbal doit dire ce qui a été décidé le jour même, pas ce
  que les données actuelles concluraient. Le PDF publié est rendu depuis le
  contenu figé, jamais recalculé. Les trois permissions RBAC sont `meeting:read`, `meeting:manage` et
  `meeting:minutes:publish` ; seule la première est accordée à
  `PROPERTY_OWNER`, qui peut répondre à la convocation de ses propres lots
  (`canReplyToConvocation`). Deux partis pris structurent tout le reste et
  sont détaillés dans
  [docs/adr/0002-assemblee-generale-cadrage.md](docs/adr/0002-assemblee-generale-cadrage.md) :
  **c'est le lot qui est convoqué et qui vote** (jamais le copropriétaire —
  une convocation par `Unit`, une voix par `Unit`, y compris pour un lot en
  indivision), et **rien de dérivable n'est stocké** (dépouillement, statut
  de convocation et quorum sont recalculés à la lecture, figés une seule
  fois dans le contenu du procès-verbal). Le poids d'une voix suit la
  configuration de la copropriété (`dues_calculation_mode` : forfait → une
  voix par lot, tantièmes → au prorata) et est snapshoté sur la convocation.
- `shared` — briques transverses : pagination, gestion des exceptions,
  audit, envoi d'email et de messages WhatsApp (`EmailSenderPort` /
  `WhatsAppSenderPort`, chacun avec son adaptateur réel — SMTP, API Messages
  de Vonage — et son adaptateur de log activé par `oikos.mail.enabled` /
  `oikos.whatsapp.enabled`), configuration, ainsi que les VO génériques utilisées
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
> changement de périmètre). Voir aussi [ARCHITECTURE.md](ARCHITECTURE.md)
> pour les règles d'architecture et de dépendances entre couches.

### 2 bis. Chantier en cours — refonte comptabilité PCM

`accounting`/`installment` sont en cours de remplacement par un moteur en
partie double conforme au Plan Comptable Marocain (PCM/CGNC), en 8 phases
(voir [docs/adr/0001-comptabilite-pcm-cadrage.md](docs/adr/0001-comptabilite-pcm-cadrage.md)
pour le détail des décisions). **Phase 1 (cadrage) faite** — aucun code
n'a encore été modifié ; la description du §2 ci-dessus reste donc exacte
tant que la Phase 2 (migrations, suppression de l'ancien modèle) n'a pas
démarré.

**Questions ouvertes** (à lever avant mise en production, pas bloquantes
pour la suite du développement) :

- Rôles de sécurité fins de la spec comptable
  (`LECTEUR`/`SAISIE`/`COMPTABLE`/`ADMIN_COMPTABLE`/`AUDITEUR`, séparation
  des tâches entre créateur et validateur d'une écriture) vs le RBAC actuel
  d'oikos (property manager / board member) — à trancher en phase 6 de
  l'ADR 0001.
- Compte d'avance copropriétaire (`UNIT_ADVANCE`) : retenu collectif par
  property avec auxiliaire = lot (et non un compte dédié par lot comme pour
  `UNIT_RECEIVABLE`) — hypothèse la plus conservatrice en l'absence
  d'exigence explicite contraire, à faire valider par un expert-comptable
  marocain avant mise en production.
- Le plan de comptes livré en v1 est le référentiel « usage syndic » de la
  spec source uniquement (comptes 3415/4415) ; le référentiel « PCGE
  strict » alternatif est différé — le mécanisme de rôle fonctionnel
  (`AccountRole` → `LedgerAccount`) permet de l'ajouter plus tard sans
  changer le moteur, mais aucun des deux référentiels n'a encore été validé
  par un expert-comptable marocain.
- Export FEC et documents PDF (avis d'appel individuel, états destinés à
  l'assemblée générale) : périmètre prévu en phase 6/7 de l'ADR 0001,
  formats exacts non encore arrêtés.

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
`MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `WHATSAPP_ENABLED`
(faux par défaut ; à vrai, `VONAGE_API_KEY`, `VONAGE_API_SECRET` et
`WHATSAPP_FROM` deviennent obligatoires au démarrage), et surtout
`APP_PUBLIC_BASE_URL` — l'origine publique d'oikos-web, dont **tous** les
liens envoyés aux utilisateurs dérivent (emails, lien de confirmation des
convocations et son QR code). Les variables par lien
(`APP_VERIFICATION_BASE_URL`, …) restent disponibles pour surcharger un lien
isolé, mais ne sont plus le passage obligé : c'est leur multiplication qui a
laissé partir des liens vers `localhost` en recette. `MAIL_USERNAME`/`MAIL_PASSWORD` ne sont
requises que si `oikos.mail.enabled=true` (faux par défaut en profil `dev`,
donc aucune de ces variables n'est nécessaire pour `./mvnw spring-boot:run`).

Profil `docker` (Postgres réel, Flyway, seed de comptes de test, vrai SMTP)
: voir `.env.example` dans ce dossier (toutes les variables documentées,
copier en `.env`, jamais commité) et
[../README.md](../README.md#docker--recette) à la racine du repo, qui
pilote l'ensemble via `docker compose up`. Ce même fichier `.env.example`
sert de modèle pour un déploiement recette/prod sur un vrai serveur (mêmes
Dockerfiles, seules les valeurs changent).

## 5. Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) — architecture
  hexagonale, découpage en couches, règles de dépendances.
- Ce README — vision produit et état fonctionnel du projet.

Les deux fichiers sont complémentaires et doivent être consultés/mis à
jour à chaque évolution notable : ce README pour le *quoi* (périmètre
métier, fonctionnalités), ARCHITECTURE.md pour le *comment* (règles de
structuration du code).
