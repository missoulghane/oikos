[← Retour à l'index](plan-recette.md)

# Module 8 — Messagerie & notifications

Routes front : `/messages/reception`, `/messages/sent`, `/messages/drafts`,
`/messages/new`, `/notifications`.
Endpoints : `ConversationController`, `MessageDraftController`,
`NotificationController`.

### MSG-01 — Envoi d'un message direct
- **Rôle** : tout membre de la copropriété (OWNER, SYNDIC, MANAGER)
- **Prérequis** : au moins deux comptes liés à la même copropriété
- **Étapes** :
  1. `/messages/new`, choisir un destinataire, écrire et envoyer
- **Résultat attendu** : `POST /properties/{propertyId}/conversations` crée la conversation, message visible côté destinataire dans `/messages/reception`
- **Priorité** : P2
- [ ]

### MSG-02 — Conversation réservée au conseil syndical
- **Rôle** : SYNDIC (admin)
- **Prérequis** : au moins deux membres du conseil syndical
- **Étapes** :
  1. Démarrer une conversation "board" (`POST /properties/{propertyId}/board-conversations`)
- **Résultat attendu** : conversation créée, invisible pour les copropriétaires simples n'appartenant pas au conseil
- **Priorité** : P3
- [ ]

### MSG-03 — Message diffusé (broadcast) à toute la copropriété
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre avec droit `canBroadcastOnProperty`)
- **Prérequis** : copropriété avec plusieurs membres
- **Étapes** :
  1. `POST /properties/{propertyId}/broadcast-messages`
- **Résultat attendu** : tous les membres de la copropriété reçoivent le message
- **Priorité** : P2
- [ ]

### MSG-04 — Brouillon enregistré puis envoyé
- **Rôle** : tout membre
- **Prérequis** : aucun
- **Étapes** :
  1. Commencer un message, l'enregistrer en brouillon (`POST /properties/{propertyId}/message-drafts`)
  2. Revenir sur `/messages/drafts`, le rouvrir, l'envoyer (`POST /message-drafts/{id}/send`)
- **Résultat attendu** : brouillon retrouvé intact, envoi transforme le brouillon en conversation/message réel
- **Priorité** : P3
- [ ]

### MSG-05 — Marquage lu/non lu d'une conversation
- **Rôle** : tout membre
- **Prérequis** : conversation avec messages non lus
- **Étapes** :
  1. Marquer comme lu (`POST /conversations/{id}/read`) puis non lu (`.../unread`)
- **Résultat attendu** : compteur de non-lus mis à jour en conséquence (`GET /users/me/conversations/unread-summary`)
- **Priorité** : P3
- [ ]

### MSG-06 — Réception et lecture d'une notification
- **Rôle** : tout membre
- **Prérequis** : un événement générant une notification (ex. décision de demande d'adhésion, nouveau message)
- **Étapes** :
  1. `/notifications`, ouvrir une notification non lue
- **Résultat attendu** : `POST /notifications/{id}/read` marque comme lue, compteur (`GET /users/me/notifications/unread-count`) décrémenté
- **Priorité** : P2
- [ ]

### MSG-07 — Groupes de diffusion supprimés
- **Rôle** : SYNDIC (admin)
- **Prérequis** : aucun
- **Étapes** :
  1. Ouvrir le menu « Ma copropriété », puis « Nouveau message »
- **Résultat attendu** : plus d'entrée « Groupes de diffusion » dans le menu ni de raccourcis « Groupes » dans le choix des destinataires ; `/properties/{propertyId}/messaging/groups` répond 404. Les conversations déjà envoyées à des membres d'un groupe restent intactes
- **Priorité** : P3
- [ ]
