[← Retour à l'index](plan-recette.md)

# Module 9 — Espace copropriétaire

Routes front (`/property-ownership/*`, ouvert à tout utilisateur
authentifié — l'API filtre via `ownsUnit`/`ownsParty`) :
`units/:propertyId/:unitId`, `.../statement`, `installments`,
`installments/:installmentId`, `payments`, `payments/:paymentId`,
`general-meetings`, `general-meetings/:meetingId`, `membership-requests`.
Endpoints : `GET /users/me/units`, `/users/me/installments`,
`/users/me/payments`, `/users/me/membership-requests`,
`/users/me/convocations`.

### OWN-01 — Consultation du détail de mon lot
- **Rôle** : OWNER
- **Prérequis** : au moins un lot possédé
- **Étapes** :
  1. `/property-ownership/units/:propertyId/:unitId`
- **Résultat attendu** : informations du lot affichées, aucun accès à un lot ne lui appartenant pas (tester avec l'ID d'un lot tiers → 403)
- **Priorité** : P2
- [ ]

### OWN-02 — Consultation du relevé de compte du lot
- **Rôle** : OWNER
- **Prérequis** : lot avec historique de charges/paiements
- **Étapes** :
  1. `/property-ownership/units/:propertyId/:unitId/statement`
- **Résultat attendu** : relevé cohérent avec les appels de fonds et paiements enregistrés côté syndic (module 6)
- **Priorité** : P1
- [ ]

### OWN-03 — Consultation de mes échéances
- **Rôle** : OWNER
- **Prérequis** : au moins un appel de fonds généré sur son lot
- **Étapes** :
  1. `/property-ownership/installments`, puis le détail d'une échéance
- **Résultat attendu** : `GET /users/me/installments` retourne uniquement ses propres échéances, montants et statuts corrects
- **Priorité** : P1
- [ ]

### OWN-04 — Consultation de l'historique de mes paiements
- **Rôle** : OWNER
- **Prérequis** : au moins un paiement enregistré
- **Étapes** :
  1. `/property-ownership/payments`, puis le détail d'un paiement
- **Résultat attendu** : `GET /users/me/payments` retourne uniquement ses propres paiements
- **Priorité** : P2
- [ ]

### OWN-05 — Consultation de mes assemblées générales
- **Rôle** : OWNER
- **Prérequis** : au moins une AG concernant sa copropriété
- **Étapes** :
  1. `/property-ownership/general-meetings`, puis le détail d'une AG
- **Résultat attendu** : liste et détail cohérents, PV visible seulement si publié (cf. AG-15)
- **Priorité** : P2
- [ ]

### OWN-06 — Suivi de mes demandes d'adhésion
- **Rôle** : OWNER (ou futur OWNER en attente)
- **Prérequis** : une demande d'adhésion soumise (INV-05), en attente ou déjà traitée
- **Étapes** :
  1. `/property-ownership/membership-requests`
- **Résultat attendu** : statut (PENDING/acceptée/rejetée) à jour et cohérent avec le traitement effectué par le syndic (module 3)
- **Priorité** : P3
- [ ]

### OWN-07 — Accès refusé à la console de gestion
- **Rôle** : OWNER
- **Prérequis** : aucun rôle de gestion sur aucune copropriété
- **Étapes** :
  1. Naviguer vers `/property-mngt/properties`
- **Résultat attendu** : redirection `/forbidden`, et `403` sur les appels API correspondants en accès direct
- **Priorité** : P1
- [ ]

### OWN-08 — Redirection de l'ancienne route `/property-ownership/units`
- **Rôle** : OWNER
- **Prérequis** : aucun
- **Étapes** :
  1. Naviguer vers `/property-ownership/units` (route legacy conservée pour d'anciens liens)
- **Résultat attendu** : redirection automatique vers `/dashboard`
- **Priorité** : P3
- [ ]
