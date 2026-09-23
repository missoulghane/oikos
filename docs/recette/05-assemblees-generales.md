[← Retour à l'index](plan-recette.md)

# Module 5 — Assemblées générales & convocations

Routes front : `/property-mngt/properties/:id/general-meetings`
(liste, création, détail avec sous-onglets informations/agenda/convocations/
session/PV), `/property-mngt/properties/:id/meeting-settings`,
`/convocations/confirmation` (public), `/property-ownership/general-meetings`
(espace copropriétaire).
Endpoints : `GeneralMeetingController`, `AgendaItemController`,
`VoteController`, `ConvocationController`, `PublicConvocationController`
(public, base `/convocations/by-token`), `MeetingMinutesController`,
`MeetingQuorumSettingController`.

### AG-01 — Création d'une assemblée générale
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre avec droit d'écriture)
- **Prérequis** : copropriété avec au moins un lot habité
- **Étapes** :
  1. `/property-mngt/properties/:id/general-meetings/new`, remplir, valider
- **Résultat attendu** : `POST /properties/{id}/general-meetings` crée l'AG en brouillon
- **Priorité** : P1
- [ ]

### AG-02 — Ajout et réordonnancement de l'ordre du jour
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : AG créée (AG-01)
- **Étapes** :
  1. Ajouter plusieurs points à l'ordre du jour (`POST /general-meetings/{id}/agenda-items`)
  2. Réordonner (`PUT .../agenda-items/order`)
- **Résultat attendu** : ordre reflété correctement à l'affichage et dans le PDF de convocation généré ensuite
- **Priorité** : P2
- [ ]

### AG-03 — Programmation de l'AG (date/lieu)
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : AG en brouillon avec ordre du jour renseigné
- **Étapes** :
  1. `POST /general-meetings/{id}/schedule` avec date et lieu
- **Résultat attendu** : AG passe à l'état programmé, date/lieu affichés
- **Priorité** : P1
- [ ]

### AG-04 — Génération des convocations
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : AG programmée (AG-03)
- **Étapes** :
  1. `POST /general-meetings/{meetingId}/convocations`
- **Résultat attendu** : une convocation générée par copropriétaire/lot concerné, visible dans la liste (`GET .../convocations`), en attente d'envoi
- **Priorité** : P1
- [ ]

### AG-05 — Envoi des convocations
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : convocations générées (AG-04), au moins un destinataire avec une adresse de test réelle
- **Étapes** :
  1. `POST /general-meetings/{meetingId}/convocations/send`
  2. Vérifier la réception de l'email
- **Résultat attendu** : email reçu avec le PDF de convocation en pièce jointe ; le PDF contient un QR code encodant le lien de confirmation (`ConvocationDocumentComposer`/`ZxingQrCodeRenderer`) ; le corps diffère selon que le destinataire a déjà un compte (pas de lien de confirmation, réponse depuis l'espace authentifié) ou non (lien + QR code, page publique)
- **Priorité** : P1
- [ ]

### AG-06 — Confirmation de présence par un copropriétaire avec compte
- **Rôle** : OWNER (avec compte existant)
- **Prérequis** : convocation envoyée à un copropriétaire déjà inscrit
- **Étapes** :
  1. Depuis `/property-ownership/general-meetings/:meetingId`, répondre (présent/pouvoir/absent)
- **Résultat attendu** : `PUT /convocations/{id}/reply`, réponse enregistrée et visible côté syndic dans le récapitulatif de présence
- **Priorité** : P1
- [ ]

### AG-07 — Confirmation via le lien public (sans compte)
- **Rôle** : ANONYME (copropriétaire sans compte)
- **Prérequis** : convocation envoyée à un copropriétaire sans compte
- **Étapes** :
  1. Cliquer le lien `/convocations/confirmation?token=...` reçu par email
  2. Répondre présent/pouvoir/absent
- **Résultat attendu** : `PUT /convocations/by-token/{token}/reply` fonctionne sans authentification, réponse visible côté syndic
- **Priorité** : P1
- [ ]

### AG-08 — Confirmation via le QR code du PDF
- **Rôle** : ANONYME
- **Prérequis** : PDF de convocation reçu (AG-05)
- **Étapes** :
  1. Scanner le QR code avec un smartphone
- **Résultat attendu** : redirection vers la même page de confirmation que AG-07, réponse enregistrable
- **Priorité** : P2
- [ ]

### AG-09 — Confirmation via le code à 6 caractères (repli téléphonique/papier)
- **Rôle** : ANONYME
- **Prérequis** : convocation générée, référence de réunion + code disponibles (visibles sur le PDF)
- **Étapes** :
  1. Aller sur la page de confirmation sans lien, saisir manuellement référence de réunion + code (`GET /convocations/by-token/{meetingReference}/{code}`)
- **Résultat attendu** : accès à la même page de confirmation, réponse enregistrable (`PUT .../{meetingReference}/{code}/reply`)
- **Priorité** : P3
- [ ]

### AG-10 — Relance des convocations sans réponse
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : au moins une convocation envoyée sans réponse
- **Étapes** :
  1. `POST /general-meetings/{meetingId}/convocations/reminders`
- **Résultat attendu** : email de relance envoyé uniquement aux non-répondants, pas aux copropriétaires ayant déjà répondu
- **Priorité** : P2
- [ ]

### AG-11 — Ouverture de la session de vote sur un point de l'ordre du jour
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : AG ouverte le jour J (`POST /general-meetings/{id}/open`)
- **Étapes** :
  1. `POST /agenda-items/{id}/vote-session/open`
- **Résultat attendu** : session de vote active pour ce point
- **Priorité** : P1
- [ ]

### AG-12 — Enregistrement des votes / résultat à main levée
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre) — saisie par le secrétaire de séance
- **Prérequis** : session de vote ouverte (AG-11)
- **Étapes** :
  1. Enregistrer les votes individuels (`POST /agenda-items/{id}/votes`) ou un résultat global à main levée (`.../votes/show-of-hands`)
  2. Clôturer la session (`POST .../vote-session/close`)
  3. Consulter le résultat (`GET .../result`)
- **Résultat attendu** : tally correct, cohérent avec les tantièmes des votants
- **Priorité** : P1
- [ ]

### AG-13 — Clôture de l'assemblée générale
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : tous les points de l'ordre du jour traités
- **Étapes** :
  1. `POST /general-meetings/{id}/close`
- **Résultat attendu** : AG passée à l'état clos, plus de modification possible sur l'ordre du jour/votes
- **Priorité** : P2
- [ ]

### AG-14 — Génération, validation et publication du procès-verbal
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre pour la génération, droit `canPublishMinutesOfMeeting` pour validation/publication)
- **Prérequis** : AG clôturée (AG-13)
- **Étapes** :
  1. Générer le brouillon de PV (`POST /general-meetings/{meetingId}/minutes`)
  2. L'éditer si besoin (`PUT .../minutes`)
  3. Le valider (`POST .../minutes/validate`) puis le publier (`POST .../minutes/publish`)
- **Résultat attendu** : PV publié visible par les copropriétaires ; vérifier qu'un compte sans droit `canPublishMinutesOfMeeting` ne peut pas publier
- **Priorité** : P1
- [ ]

### AG-15 — Consultation du PV publié côté copropriétaire
- **Rôle** : OWNER
- **Prérequis** : PV publié (AG-14)
- **Étapes** :
  1. `/property-ownership/general-meetings/:meetingId`
- **Résultat attendu** : PV consultable, pas de PV visible tant qu'il n'est pas publié
- **Priorité** : P2
- [ ]

### AG-16 — Paramétrage du quorum
- **Rôle** : SYNDIC (admin), MANAGER (admin)
- **Prérequis** : copropriété existante
- **Étapes** :
  1. `/property-mngt/properties/:id/meeting-settings`, définir une règle de quorum
- **Résultat attendu** : `PUT /properties/{id}/meeting-quorum-settings` persiste la règle, appliquée lors du calcul de quorum d'une AG
- **Priorité** : P3
- [ ]

### AG-17 — Émargement le jour de l'AG
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : AG ouverte, convocations envoyées
- **Étapes** :
  1. Cocher la présence d'un participant (`POST /convocations/{id}/check-in`)
  2. Annuler l'émargement (`DELETE .../check-in`)
- **Résultat attendu** : statut de présence mis à jour en temps réel, reflété dans le résumé de présence/quorum
- **Priorité** : P2
- [ ]
