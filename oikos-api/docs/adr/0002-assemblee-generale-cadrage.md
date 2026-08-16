# ADR 0002 — Module « Assemblée Générale » : cadrage

Statut : Accepté — livré de bout en bout (lots 0 à 8 : API, web, mobile)
Date : 2026-08-15

## Contexte

La SFD « Module Assemblée Générale » décrit cinq objets métier :
`AssembléeGénérale`, `OrdreDuJourPoint`, `Convocation` (fusion
invitation + confirmation + émargement), `Vote` et `ProcesVerbal`.

Aucun code n'existe aujourd'hui pour ce périmètre. Le seul point d'ancrage
déjà présent est `NotificationType.GENERAL_MEETING_CALLED`, qui fixe le
vocabulaire anglais du contexte (« general meeting ») avant même que le
module n'existe.

La SFD laisse sept points ouverts, dont trois déterminent directement le
résultat d'un vote et ne peuvent donc pas être tranchés après coup. Ce
document fige ces arbitrages ; il ne contient pas de code.

## Décisions

### 1. Un nouveau contexte métier `meeting`

Module `com.architek.oikos.meeting`, découpé en quatre couches comme tous
les autres contextes (`domain` / `application` / `infrastructure` / `web`,
règle 1 d'[ARCHITECTURE.md](../../ARCHITECTURE.md)). Nom d'agrégat racine
`GeneralMeeting`, aligné sur `GENERAL_MEETING_CALLED` plutôt que sur une
traduction nouvelle de « assemblée générale ».

Le couplage vers `property`, `party`, `user`, `document` et `notification`
passe exclusivement par des `application.port.out` propres à `meeting`,
implémentés par des adapters préfixés `Meeting…` (patron `Messaging…` /
`Accounting…`, qui évite aussi toute collision de bean Spring). Règle 4/6,
vérifiée par `DependencyRulesArchTest.meeting_must_not_depend_on_other_modules_internals`.

**Décision** : pas de rattachement du périmètre AG à `property` ou à
`messaging`. Le cycle de vie (6 statuts), le dépouillement et le PV forment
un domaine autonome, sans lecture ni écriture directe sur les agrégats des
autres modules.

### 2. Le lot est le sujet de la convocation et du vote

La SFD rattache `Convocation` et `Vote` à un `copropriétaire_id`. Dans le
modèle existant, un copropriétaire est un `Party` relié à un `Unit` par
`UnitOwnership` — relation N-N : un lot peut avoir plusieurs propriétaires
(indivision), un propriétaire peut détenir plusieurs lots.

**Décision** : le porteur du droit de vote est le **lot** (`Unit`), pas le
`Party`.

- `convocation` : une ligne par `(general_meeting_id, unit_id)`, FK réelle
  vers `unit(id)` — même patron que `installment.unit_id`.
- `vote` : une ligne par `(agenda_item_id, unit_id)`. Un lot, une voix (dont
  le poids est défini en §3).
- Les destinataires d'une convocation sont les copropriétaires **courants**
  du lot, résolus dynamiquement à l'envoi via `PropertyOwnerDirectoryPort` —
  aucune table `convocation_recipient`. Même raisonnement que l'appartenance
  d'un canal `BROADCAST` dans `messaging` : une vente de lot entre la
  convocation et la séance ne laisse rien à migrer.
- `convocation.checked_in_party_id` (nullable) enregistre qui représente le
  lot en séance : c'est la seule trace nominative nécessaire, et elle lève
  l'ambiguïté en indivision.

Conséquence assumée : une convocation est générée pour **tous** les lots de
la copropriété, y compris ceux sans aucun `UnitOwnership`
(`OwnershipStatus.NOT_AFFECTED`). Ils comptent dans le total des voix (donc
dans le quorum et dans la majorité absolue) mais ne peuvent ni répondre ni
émarger — ce qui est le comportement juridiquement correct, un lot invendu
restant un lot de la copropriété.

### 3. La pondération des voix suit la configuration de la copropriété

`Property.duesCalculationMode` (`FLAT_RATE` / `SHARES`) pilote déjà la
répartition des charges. La même configuration pilote le poids de vote :

| `DuesCalculationMode` | `VotingWeightMode` (module `meeting`) | Poids d'un lot |
|---|---|---|
| `FLAT_RATE` (forfait par type de lot) | `PER_UNIT` | `1` |
| `SHARES` (tantièmes) | `SHARES` | `Unit.shares` |

`meeting` déclare sa **propre** énumération `VotingWeightMode` plutôt que de
dépendre de `property.domain.valueobject.DuesCalculationMode` (règle 4) —
`installment` a déjà sa copie locale du même enum, pour la même raison. La
traduction se fait dans `MeetingPropertyDirectoryAdapter`, seul point du
module qui connaisse les deux.

**Décision** : le poids est **snapshoté** sur `convocation.voting_weight` à
la génération des convocations, et le mode retenu sur
`general_meeting.voting_weight_mode`. Une vente de lot, une correction de
tantièmes ou une bascule de mode postérieures ne réécrivent pas le résultat
d'une AG passée — même principe que le gel du prix d'un appel de fonds déjà
émis (RG002).

### 4. La règle de majorité est choisie point par point

`agenda_item.majority_rule` est obligatoire : `SIMPLE`, `ABSOLUTE` ou
`UNANIMITY`, fixée à la rédaction de l'ordre du jour.

Pour que la règle reste une fonction pure et qu'un changement
d'interprétation soit une ligne, `VoteTally` porte les **trois**
dénominateurs plutôt que le seul qui sert à la règle choisie :

```text
expressedWeight = FOR + AGAINST                     (abstentions exclues)
presentWeight   = Σ poids des lots émargés
totalWeight     = Σ poids de tous les lots de la copropriété
```

`MajorityRuleEvaluator` (fonction pure du domaine) produit un `VoteOutcome`
(`ADOPTED` / `REJECTED`) :

| Règle | Condition d'adoption |
|---|---|
| `SIMPLE` | `FOR > AGAINST` (dénominateur `expressedWeight`) |
| `ABSOLUTE` | `FOR > totalWeight / 2` — majorité absolue des voix de **la copropriété entière**, présents ou non |
| `UNANIMITY` | `presentWeight > 0`, **au moins une voix POUR**, `AGAINST = 0` et `ABSTENTION = 0` |

Le dénominateur d'`ABSOLUTE` retient la lecture stricte de l'article 21 de
la loi 18-00 (majorité des voix des copropriétaires, pas des votants). C'est
le seul arbitrage réellement discutable des trois ; il est isolé dans
`MajorityRuleEvaluator` pour rester révisable sans toucher au reste.

La condition « au moins une voix POUR » d'`UNANIMITY` est une précision
ajoutée à l'implémentation (lot 4) : sans elle, un point sur lequel personne
n'a voté sortirait « adopté à l'unanimité » — aucune voix contre, aucune
abstention, et une salle pleine.

**Décision** : `VoteOutcome` et `VoteTally` sont **calculés à la lecture**,
jamais stockés — convention constante du codebase (`InstallmentStatus`,
`OwnershipStatus`, `unreadCount`). Ils ne sont figés qu'une fois, dans le
contenu du procès-verbal au moment de sa génération.

### 5. Quorum paramétrable par nature d'AG, bloquant mais forçable

Table `meeting_quorum_setting` : un seuil par `(property_id, meeting_type)`,
donc distinct pour l'ordinaire et l'extraordinaire, réglable copropriété par
copropriété. La valeur est **snapshotée** sur
`general_meeting.quorum_percentage` à la création de l'AG, pour la même
raison qu'en §3.

`QuorumEvaluator` : quorum atteint ⇔ `presentWeight / totalWeight ≥
quorum_percentage`, calculé à la lecture.

**Décision** : `GeneralMeeting.open()` **refuse** l'ouverture si le quorum
n'est pas atteint. Le syndic peut passer outre explicitement
(`POST /general-meetings/{id}/open {"forceWithoutQuorum": true}`), ce qui
trace `opened_without_quorum = true` et apparaît en toutes lettres dans le
PV. Ouvrir une séance sans quorum est une décision juridique du syndic : elle
doit être un acte délibéré et tracé, pas un défaut silencieux.

### 6. Le parcours d'une convocation est intégralement saisissable à la main

Les canaux postaux n'ont aucune intégration, et une part des lots n'a aucun
compte applicatif rattaché : ces lots doivent malgré tout pouvoir être
convoqués, confirmés et émargés.

**Décision** : deux verbes distincts plutôt qu'un seul avec un drapeau.

- `POST /convocations/{id}/send` — envoi réel, réservé aux canaux que
  l'application sait effectuer (`automated` dans le catalogue, §9).
- `PUT /convocations/{id}/delivery-status` — marquage manuel (`SENT` /
  `FAILED`) pour le courrier, le recommandé ou la remise en main propre.

`PUT /convocations/{id}/reply` et `POST /convocations/{id}/check-in` sont
ouverts au copropriétaire pour son propre lot (`ownsUnit`) **et** au syndic
pour n'importe quel lot de la copropriété (`meeting:manage`). « J'ai posté
la lettre » n'est pas « envoie l'email » : les confondre rendrait le
tableau de suivi inexploitable.

### 7. Hors périmètre v1

Décisions négatives, prises explicitement :

- **Procurations / pouvoirs** — non gérés. C'est la dette la plus visible de
  cette v1 : en pratique une part significative des voix d'une AG s'exprime
  par mandat. À reprendre dans une itération ultérieure, probablement comme
  un `proxy_holder_unit_id` nullable sur `convocation`.
- **Seconde convocation** — quorum non atteint ⇒ nouvelle AG créée
  manuellement. Pas d'enchaînement automatique.
- **Signature d'émargement** — capture simple stockée comme `Document`
  (`DocumentOwnerType.CONVOCATION`). Aucune valeur juridique revendiquée :
  ni signature électronique qualifiée, ni horodatage certifié.
- **Vote à distance / électronique hors séance** — le vote exige
  `checked_in = true`, donc une présence (sur site ou en visioconférence)
  constatée à l'ouverture.

### 8. Aucune règle de gestion bloquante pour le moment (décision du 2026-08-16)

Les lots 2 à 5 avaient implémenté deux verrous que la lecture stricte du droit
justifie :

- **l'ordre du jour se figeait** dès que l'AG quittait `DRAFT` — un
  copropriétaire est convoqué sur la foi d'une liste de points, en ajouter un
  après coup revient à mettre au vote quelque chose dont personne n'a été
  informé ;
- **la date et le lieu se figeaient** au même moment — les convocations parties
  indiquent une adresse et une heure.

**Décision** : ces deux verrous sont **levés**. L'ordre du jour reste
modifiable à tout statut, la date et le lieu aussi (`GeneralMeeting.update`,
sans garde). Le produit ne veut pas de règle de gestion bloquante à ce stade :
en usage réel un syndic corrige une coquille d'adresse ou ajoute un point
oublié, et l'obliger à supprimer l'assemblée pour la recréer coûte plus qu'un
garde-fou théorique n'apporte.

Ce qui **reste** contraint, et pourquoi ce n'est pas la même chose :

| Règle conservée | Raison |
|---|---|
| Une AG convoquée ne peut plus être supprimée (`MeetingNotDeletableException`) | Supprimer détruit en cascade convocations, votes et PV. C'est de la destruction, pas de la correction. |
| Une AG hors `DRAFT` ne peut pas perdre sa date ou son lieu | Contrainte structurelle, pas règle métier : `chk_general_meeting_scheduled` dit la même chose en base. |
| Un lot doit avoir émargé pour voter, le scrutin doit être ouvert, la séance en cours | Ces règles décident d'un **résultat**, pas d'un confort de saisie. |
| Le PV se fige à sa validation | Un texte validé puis modifié ne serait plus un procès-verbal. |
| Le cycle de vie ne revient jamais en arrière | Une convocation partie ne se départ pas. |

**À reprendre** : si le gel de l'ordre du jour redevient nécessaire (contentieux,
exigence d'un syndic professionnel), le point de réintroduction est unique —
une garde dans `GeneralMeeting.update` et dans les quatre services d'ordre du
jour, plus l'exception `AgendaFrozenException` supprimée par cette décision.
Un avertissement non bloquant à la convocation (« des points ont été ajoutés
après l'envoi des convocations ») serait un moyen terme.

### 9. Une convocation part par plusieurs canaux, et une confirmation dit d'où elle vient (décision du 2026-08-16)

Deux écarts constatés en recette, sur le même objet.

**Le canal était unique et s'écrasait.** La convocation portait un seul
triplet `(channel, sentAt, deliveryStatus)` que `recordDelivery()` remplaçait
à chaque appel : envoyer par email puis constater la remise d'un recommandé
effaçait la trace de l'email. Or c'est exactement le parcours normal — on
envoie par email, et le lot resté muet reçoit un recommandé.

**On savait *si* le copropriétaire avait répondu, pas *comment*.** Une
confirmation prise au téléphone par le bureau et une confirmation donnée par
le copropriétaire depuis son espace n'ont pas la même valeur probante, et
rien ne les distinguait.

**Décisions** :

1. **`ConvocationDelivery`, 1—N.** Une ligne par tentative, ajoutée et jamais
   remplacée, échecs compris. `deliveryStatus` et `sentAt` disparaissent en
   tant que colonnes et deviennent **dérivés**, comme `ConvocationStatus`
   l'est déjà : aucune ligne → `TO_SEND` ; au moins une `SENT` → `SENT` ; que
   des `FAILED` → `FAILED`. `sentAt` est la date du **premier** envoi abouti,
   parce que c'est d'elle que court le délai de convocation — une relance ou
   un second canal ne doit pas la repousser.

2. **Le catalogue des canaux devient une table**, `convocation_channel`, et
   non plus un enum Java doublé d'une contrainte `CHECK`. La demande était
   explicite : « il faut que cette liste soit évolutive rapidement ». Ajouter
   un canal est désormais un `INSERT`. Même bascule que `UnitType` →
   `UnitTypeDefinition`, même forme que le catalogue `journal`.

   L'asymétrie qu'elle introduit est assumée et vaut d'être connue : un canal
   **manuel** est gratuit (la ligne suffit), un canal **automatisé** exige en
   plus un émetteur dans le code. Une ligne « SMS, automatisé » sans émetteur
   promettrait un envoi qui n'a jamais lieu ; `ConvocationChannelLookup` la
   refuse explicitement plutôt que de faire semblant.

   Une ligne `MOBILE` n'a **pas** été créée, bien que la demande la
   mentionne : la notification in-app (`APP`) est exactement ce que
   l'application mobile reçoit. Deux lignes notifieraient deux fois la même
   chose et compteraient deux envois pour un seul. Un canal SMS, lui, serait
   bien une ligne de plus — et un émetteur à écrire.

3. **`ReplySource` (`OWNER_APP` / `OWNER_LINK` / `SYNDIC_OFFICE`) reste un
   enum de code**, contrairement aux canaux : chaque valeur est un chemin de
   code distinct, pas une donnée. Elle est **déduite de l'appelant côté
   serveur et jamais envoyée par le client** — un client capable de déclarer
   sa propre source pourrait écrire « le copropriétaire a confirmé depuis
   l'app » sur une réponse que personne n'a donnée, ce qui est précisément
   l'affirmation sur laquelle se joue une AG contestée. `repliedByPartyId`
   nomme le répondant quand il est identifié, `replyNote` porte la précision
   que le syndic garderait sinon sur un post-it.

La présence *réelle* reste l'émargement et n'est pas touchée : confirmer sa
venue n'a jamais donné le droit de voter (§5).

### 10. Un lien de confirmation pour les copropriétaires sans compte (décision du 2026-08-16)

Une part importante des lots n'a aucun compte applicatif rattaché — c'est le
point de départ du §6, qui rendait tout le parcours saisissable à la main. Mais
la saisie manuelle ne résout que la moitié du problème : le syndic pouvait
enregistrer une réponse, le copropriétaire n'avait toujours aucun moyen d'en
donner une autrement qu'en téléphonant. C'est pourquoi, jusqu'ici, **toute**
réponse enregistrée était `SYNDIC_OFFICE` (§9).

**Décision** : un jeton opaque par convocation, porteur d'un lien personnel,
et deux points d'entrée anonymes — `GET /convocations/by-token/{token}` pour
afficher, `PUT /convocations/by-token/{token}/reply` pour répondre.

Ce qui borde ce choix, dans l'ordre où il faut l'évaluer :

- **L'écriture est anonyme elle aussi**, contrairement au parcours
  d'invitation où seule la lecture l'est. C'est le point délicat, et c'est
  délibéré : exiger un compte laisserait précisément la population visée avec
  le téléphone pour seule option. `PublicConvocationControllerWebMvcTest`
  épingle la règle de sécurité elle-même, parce qu'un resserrement ultérieur en
  `GET`-only ne produirait aucun rapport de bug — les gens concernés n'ont pas
  d'autre voie pour se plaindre.

- **Le jeton est créé à la génération, pas à l'envoi.** Le lien s'imprime sur
  la lettre que le syndic poste, et la voie postale est justement celle dont
  les destinataires n'ont pas de compte.

- **Ce que le lien expose est volontairement étroit** : l'AG, la date, le lieu,
  le lot, la réponse en cours. Ni identifiant, ni nom de copropriétaire, ni
  autre lot, ni décompte de la copropriété. Un lien qui fuite expose une
  convocation, pas une copropriété. Le jeton, symétriquement, n'apparaît dans
  **aucune** réponse JSON du back-office : il ne circule que dans l'email et
  dans le PDF.

- **`repliedByPartyId` reste `null`.** Le jeton prouve qu'on a reçu la
  convocation du lot, jamais lequel des indivisaires clique. Écrire un
  répondant ici serait une invention.

- **Pas de durée de vie propre.** Le lien cesse d'accepter une réponse à
  l'ouverture de la séance : au-delà, la présence est l'émargement et non plus
  une déclaration (§5). Une expiration en jours dirait moins — elle ignorerait
  un report de séance — et dériverait le jour où la date bouge. La page reste
  consultable et explique la clôture, plutôt que de renvoyer un 404 à quelqu'un
  qui a suivi le lien qu'on lui a donné.

**Limite connue** : sur une lettre papier, l'adresse doit être recopiée à la
main (43 caractères). Un QR code la lèverait ; il demande un encodeur d'image
dans le rendu PDF et n'a pas été fait ici.

### 11. Les notifications in-app partent dans leur propre transaction (décision du 2026-08-16)

Deux endroits du module envoient une notification in-app en « best effort » —
l'envoi d'une convocation et la publication d'un PV — chacun avec un `try` /
`catch` et un commentaire disant que l'échec est sans conséquence. Il ne
l'était pas.

Les services traversés (`FindUsersByPartyIdsService`,
`CreateNotificationService`) sont `@Transactional` en propagation par défaut :
ils **rejoignent** la transaction de l'appelant. Quand l'un lève, Spring marque
cette transaction partagée `rollback-only`
(`globalRollbackOnParticipationFailure`, `true` par défaut) *avant* que le
`catch` ne voie l'exception. L'avaler ne change donc rien : le commit échoue
ensuite en `UnexpectedRollbackException`, et la convocation ressort « à
envoyer » — **alors que l'email est parti**. Le syndic la renvoie, le
copropriétaire la reçoit deux fois.

**Décision** : extraire `MeetingNotificationDispatcher`, dont les méthodes sont
`@Transactional(propagation = REQUIRES_NEW)`. `REQUIRES_NEW` **suspend** la
transaction appelante : un échec ne marque plus que la sienne, et les `catch`
existants font enfin ce qu'ils annoncent.

Trois points de mise en œuvre qui ne sont pas interchangeables :

- **Un bean séparé, obligatoirement.** Annoter les méthodes privées d'origine
  n'aurait rien changé — l'auto-invocation ne passe pas par le proxy — et aurait
  été pire que le défaut : un bug sous une annotation affirmant le contraire.

- **Pas `globalRollbackOnParticipationFailure = false`.** Le réglage existe et
  aurait « corrigé » le symptôme, mais il est global au transaction manager :
  changer la sémantique de toute transaction participante du produit pour deux
  méthodes.

- **Pas d'écouteur `AFTER_COMMIT`**, bien qu'il donnerait un ordre plus strict.
  Ce module a délibérément supprimé sa cascade `AFTER_COMMIT` (voir
  « Conséquences »), et le gain serait partiel : l'email SMTP part de toute
  façon avant le commit et ne se rattrape pas non plus.

Conséquence assumée : une notification commitée survit si le travail de
l'appelant échoue ensuite. Elle n'introduit aucune incohérence que l'email
n'ait déjà.

`ConvocationNotificationFailurePostgresIntegrationTest` le vérifie sur une vraie
base — seul niveau où le défaut est visible, puisque sans transaction manager
réel il n'y a pas de commit à faire échouer.

### 12. Commentaire riche et pièces jointes, sur l'AG comme sur ses points (décision du 2026-08-16)

Un ordre du jour dit ce qui sera décidé, pas sur quoi. Le budget qu'on approuve,
le devis qu'on compare, la note qui explique pourquoi le point est inscrit :
rien de tout cela n'avait de place.

**Décisions** :

1. **Pièces jointes à deux niveaux**, via le module `document` et rien d'autre :
   `DocumentOwnerType.AGENDA_ITEM` (déjà prévu au lot 1) pour ce qui documente
   un point, `GENERAL_MEETING` (nouveau) pour ce qui éclaire l'assemblée
   entière. Aucune table propre au module — un `ownerType` de plus, sa branche
   d'existence et sa branche d'autorisation, ce que `DocumentOwnerType`
   documente comme le seul point d'extension.

2. **Un commentaire global en texte riche** (`GeneralMeeting.comment`), sur son
   propre verbe et son propre endpoint. Le faire transiter par
   `UpdateGeneralMeetingUseCase` rendrait « enregistrer un commentaire » capable
   d'écraser la date et le lieu avec ce que l'écran tenait en mémoire — une
   perte de mise à jour sur les deux champs dont dépend un copropriétaire
   convoqué.

3. **Il est lu par les copropriétaires** : espace copropriétaire, mobile, et
   repris dans le PDF de convocation. C'est ce qui en fait une note d'intention
   plutôt qu'un pense-bête, et c'est ce que l'écran de saisie dit en toutes
   lettres — un syndic qui y écrirait un rappel interne l'écrirait à toute la
   copropriété.

**Deux conséquences techniques qui ne se déduisent pas** :

- **Le PDF prend le texte, pas le HTML.** `openhtmltopdf` est utilisé sans
  parseur HTML5 (voir le `pom`), donc le gabarit rendu doit être du XML bien
  formé — ce que la sortie d'un éditeur riche n'est pas : Quill émet `<br>` non
  fermé, et un seul suffit à faire échouer le rendu d'une convocation.
  `RichTextToParagraphs` l'aplatit en paragraphes, que Thymeleaf échappe. Le gras
  et les liens ne survivent pas : c'est le bon compromis pour un document qui
  doit se rendre, à chaque fois, pour chaque lot.

- **Les clients assainissent au lieu d'aplatir**, eux, parce qu'ils le peuvent.
  L'API ne valide le HTML d'aucun de ses champs riches (`MessageBody`,
  `comment`) et la sortie de Quill n'est pas réputée sûre (CVE-2025-15056). La
  frontière XSS est `sanitizeRichText`, côté web, et c'est la raison pour
  laquelle `dangerouslySetInnerHTML` n'apparaît plus que dans un composant
  unique. Deux listes blanches, pas une : celle de l'éditeur, et une plus large
  pour les documents composés par l'API (le PV, qui produit des titres que la
  première supprimerait).

## Conséquences

- Trois permissions nouvelles au catalogue RBAC : `meeting:read`,
  `meeting:manage`, `meeting:minutes:publish`. `meeting:read` est la seule
  accordée à `PROPERTY_OWNER`.
- Trois `DocumentOwnerType` nouveaux (`AGENDA_ITEM`, `CONVOCATION`,
  `MEETING_MINUTES`), donc trois branches à ajouter dans
  `DocumentOwnerExistenceAdapter` et dans
  `PropertyAccessEvaluator.resolveDocumentOwnerPropertyId`, et l'extension de
  la règle ArchUnit `document_must_not_depend_on_other_modules_internals` aux
  packages internes de `meeting`.
- Le PDF de convocation et celui du PV réutilisent la chaîne Thymeleaf +
  openhtmltopdf déjà en place (`ThymeleafPaymentReceiptRenderer`), et sont
  stockés par le module `document` — aucune deuxième façon de conserver un
  fichier.
- **Générer et envoyer les convocations sont deux actions séparées** (décision
  du 2026-08-16). Générer crée les lignes et convoque l'AG sans rien expédier ;
  envoyer expédie ce qui reste en attente. Les deux étaient soudées derrière un
  seul bouton, dont le mode d'échec — « certains emails sont partis, d'autres
  non, et l'AG est convoquée dans tous les cas » — n'était explicable à
  personne. L'écouteur `AFTER_COMMIT` qui portait cette cascade a disparu avec
  elle : chaque envoi tourne dans sa propre transaction `REQUIRES_NEW`, un lot
  injoignable est marqué `FAILED` et les autres partent quand même.

## Feuille de route

| Lot | Contenu | État |
|---|---|---|
| 0 | Nomenclature FR→EN + le présent ADR | **fait** |
| 1 | Migrations `V4`/`V5`, permissions RBAC, règles ArchUnit | **fait** |
| 2 | `GeneralMeeting` + `AgendaItem` + `MeetingQuorumSetting` (domaine → web), cycle `DRAFT → SCHEDULED` | **fait** |
| 3 | `Convocation` : génération, envoi, réponse, relance, émargement, tableau de suivi — **plus** l'ouverture et la clôture de séance, qui dépendent du quorum et n'avaient donc rien à faire avant l'émargement | **fait** |
| 4 | `Vote` : ouverture/clôture de scrutin, saisie nominative et à main levée, dépouillement (`VoteTally` + `MajorityRuleEvaluator`) | **fait** |
| 5 | `MeetingMinutes` : génération depuis la séance, édition, validation (gel du texte), publication (PDF + notification) | **fait** |
| 6 | Front web back-office (`property-mngt/general-meetings`) | **fait** |
| 7 | Front web espace copropriétaire (`property-ownership/general-meetings`) | **fait** |
| 8 | Mobile (consultation + réponse) | **fait** |
| A | Canaux d'envoi multiples (`convocation_delivery` + catalogue `convocation_channel`) et traçabilité de la confirmation (`ReplySource`) — voir §9 | **fait** |
| B | Lien de confirmation tokenisé, pour les copropriétaires sans compte (`reply_source = OWNER_LINK`) — voir §10 | **fait** |
| C | Commentaire global en texte riche et pièces jointes (AG et points de l'ordre du jour) — voir §12 | **fait** |

Les lots 2 à 5 sont strictement ordonnés (chacun dépend du précédent) ; les
lots 6 et 7 peuvent démarrer dès que le lot API correspondant est livré.
