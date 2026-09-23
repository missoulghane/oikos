[← Retour à l'index](plan-recette.md)

# Module 3 — Invitations & demandes d'adhésion

Routes front : `/accept-invitation`, `/invitations` (aperçu + demande
d'adhésion via lien public).
Endpoints : `POST /properties/{id}/invitations`,
`/properties/{id}/board-invitations`, `GET /properties/{id}/invitations`,
`GET /properties/{id}/public-invitation`, `GET|PATCH /invitations/{id}`,
`GET /invitations/by-token/{token}`, `.../available-units`, `.../accept`,
`.../membership-requests`, `GET /properties/{id}/membership-requests`,
`PATCH /membership-requests/{id}/accept|reject`.

Deux mécanismes distincts à ne pas confondre à la recette :
- **Invitation nominative** (un lot précis, un destinataire précis) → email
  avec lien `/accept-invitation?token=...`, acceptation immédiate.
- **Lien public de la copropriété** (permanent, partagé largement) → page
  `/invitations`, débouche sur une **demande d'adhésion** (statut PENDING) à
  valider manuellement par le syndic, pas une acceptation directe.

### INV-01 — Création d'une invitation nominative sur un lot
- **Rôle** : SYNDIC (admin) ou MANAGER (admin/membre avec droit `INVITATION_MANAGE`)
- **Prérequis** : copropriété avec au moins un lot sans propriétaire lié
- **Étapes** :
  1. Depuis la fiche copropriété, onglet lots/invitations, créer une invitation pour un lot et un email donnés
- **Résultat attendu** : `POST /properties/{id}/invitations` crée l'invitation, email envoyé avec lien `/accept-invitation?token=...` (TTL 7 jours)
- **Priorité** : P1
- [ ]

### INV-02 — Création d'une invitation "conseil syndical"
- **Rôle** : SYNDIC (admin)
- **Prérequis** : copropriété existante
- **Étapes** :
  1. Inviter une personne à rejoindre le conseil syndical (board-invitation)
- **Résultat attendu** : `POST /properties/{id}/board-invitations` crée l'invitation avec le bon rôle cible (`PROPERTY_BOARD_MEMBER`)
- **Priorité** : P2
- [ ]

### INV-03 — Acceptation d'une invitation nominative
- **Rôle** : destinataire de l'invitation (futur OWNER ou SYNDIC membre)
- **Prérequis** : email d'invitation reçu (INV-01/INV-02)
- **Étapes** :
  1. Cliquer le lien `/accept-invitation?token=...`
  2. Définir son mot de passe (si nouveau compte) et valider
- **Résultat attendu** : accès accordé immédiatement au lot/rôle concerné, pas d'étape de validation manuelle supplémentaire
- **Priorité** : P1
- [ ]

### INV-04 — Consultation du lien public d'une copropriété
- **Rôle** : SYNDIC (admin) ou MANAGER (admin)
- **Prérequis** : copropriété existante
- **Étapes** :
  1. Récupérer le lien public permanent (`GET /properties/{id}/public-invitation`)
  2. L'ouvrir en navigation privée, en tant qu'ANONYME
- **Résultat attendu** : page `/invitations` accessible sans connexion, `GET /invitations/by-token/{token}/available-units` liste les lots encore disponibles
- **Priorité** : P2
- [ ]

### INV-05 — Demande d'adhésion via le lien public
- **Rôle** : ANONYME → futur OWNER
- **Prérequis** : lien public d'une copropriété (INV-04)
- **Étapes** :
  1. Depuis `/invitations`, choisir un lot disponible
  2. Soumettre une demande d'adhésion (créer un compte si besoin, ou se connecter)
- **Résultat attendu** : `POST /invitations/by-token/{token}/membership-requests` crée une demande au statut PENDING, visible côté syndic, **aucun accès accordé tant qu'elle n'est pas traitée**
- **Priorité** : P1
- [ ]

### INV-06 — Traitement d'une demande d'adhésion — acceptation
- **Rôle** : SYNDIC (admin) ou MANAGER (admin)
- **Prérequis** : demande PENDING (INV-05)
- **Étapes** :
  1. Depuis l'onglet "demandes d'adhésion" de la copropriété, accepter la demande
- **Résultat attendu** : `PATCH /membership-requests/{id}/accept`, email de décision envoyé avec lien `/dashboard`, accès effectif au lot pour le demandeur
- **Priorité** : P1
- [ ]

### INV-07 — Traitement d'une demande d'adhésion — rejet avec motif
- **Rôle** : SYNDIC (admin) ou MANAGER (admin)
- **Prérequis** : demande PENDING
- **Étapes** :
  1. Rejeter la demande en saisissant un motif
- **Résultat attendu** : `PATCH /membership-requests/{id}/reject`, email de décision envoyé (sans lien, rejet uniquement), aucun accès accordé
- **Priorité** : P2
- [ ]

### INV-08 — Désactivation puis réactivation d'une invitation nominative
- **Rôle** : SYNDIC (admin)
- **Prérequis** : invitation nominative créée et non encore acceptée
- **Étapes** :
  1. Désactiver l'invitation (`PATCH /invitations/{id}/disable`)
  2. La réactiver (`PATCH /invitations/{id}/enable`)
- **Résultat attendu** : le lien devient inutilisable après désactivation, réutilisable après réactivation
- **Priorité** : P3
- [ ]

### INV-09 — Tentative d'acceptation d'une invitation désactivée
- **Rôle** : destinataire de l'invitation
- **Prérequis** : invitation désactivée (INV-08) ou expirée (> 7 jours)
- **Étapes** :
  1. Cliquer le lien `/accept-invitation?token=...`
- **Résultat attendu** : message d'erreur explicite, aucun accès accordé
- **Priorité** : P2
- [ ]
