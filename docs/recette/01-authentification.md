[← Retour à l'index](plan-recette.md)

# Module 1 — Authentification & compte

Routes front : `/login`, `/forgot-password`, `/reset-password`,
`/verify-email`, `/activate-account`, `/profile`.
Endpoints : `POST /auth/login`, `/auth/refresh-token`, `/auth/logout`,
`/auth/forgot-password`, `/auth/reset-password`, `/users/verify`,
`/users/resend-verification`, `/users/activate-account`,
`PATCH /users/me/password`.

### AUTH-01 — Connexion avec identifiants valides
- **Rôle** : tous
- **Prérequis** : compte actif et email vérifié
- **Étapes** :
  1. Aller sur `/login`
  2. Saisir identifiant + mot de passe valides
  3. Valider
- **Résultat attendu** : redirection vers `/dashboard`, session établie (token en place)
- **Priorité** : P1
- [ ]

### AUTH-02 — Connexion refusée avec mot de passe erroné
- **Rôle** : tous
- **Prérequis** : compte existant
- **Étapes** :
  1. Aller sur `/login`
  2. Saisir un mauvais mot de passe
- **Résultat attendu** : message d'erreur générique (pas d'énumération du compte), pas de session créée
- **Priorité** : P1
- [ ]

### AUTH-03 — Connexion refusée sur un compte non vérifié/désactivé
- **Rôle** : tous
- **Prérequis** : compte créé mais email non vérifié, ou désactivé via `/users/{id}/status`
- **Étapes** :
  1. Tenter une connexion avec ce compte
- **Résultat attendu** : refus explicite invitant à vérifier l'email / compte indisponible
- **Priorité** : P2
- [ ]

### AUTH-04 — Rafraîchissement de session
- **Rôle** : tous
- **Prérequis** : session active
- **Étapes** :
  1. Rester inactif jusqu'à expiration de l'access token, ou forcer un appel après expiration
  2. Effectuer une action nécessitant un appel API
- **Résultat attendu** : `POST /auth/refresh-token` renouvelle la session de façon transparente, pas de déconnexion intempestive
- **Priorité** : P2
- [ ]

### AUTH-05 — Déconnexion
- **Rôle** : tous
- **Prérequis** : session active
- **Étapes** :
  1. Utiliser l'action de déconnexion dans l'UI
- **Résultat attendu** : `POST /auth/logout` révoque le refresh token, retour à `/login`, impossible de réutiliser l'ancien token
- **Priorité** : P2
- [ ]

### AUTH-06 — Demande de réinitialisation de mot de passe
- **Rôle** : tous
- **Prérequis** : compte existant avec email valide
- **Étapes** :
  1. Aller sur `/forgot-password`
  2. Saisir l'email du compte
- **Résultat attendu** : réponse `202` générique (pas d'indication si l'email existe ou non), email de reset reçu (lien `/reset-password?token=...`, TTL 1h)
- **Priorité** : P1
- [ ]

### AUTH-07 — Réinitialisation du mot de passe via le lien reçu
- **Rôle** : tous
- **Prérequis** : email de reset reçu (AUTH-06)
- **Étapes** :
  1. Cliquer le lien reçu → arrivée sur `/reset-password?token=...`
  2. Saisir un nouveau mot de passe conforme
  3. Valider puis se connecter avec le nouveau mot de passe
- **Résultat attendu** : mot de passe changé, connexion possible avec le nouveau, ancien mot de passe refusé
- **Priorité** : P1
- [ ]

### AUTH-08 — Lien de réinitialisation expiré ou déjà utilisé
- **Rôle** : tous
- **Prérequis** : token de reset généré il y a plus d'1h, ou déjà consommé une fois
- **Étapes** :
  1. Utiliser ce lien sur `/reset-password`
- **Résultat attendu** : message d'erreur clair, aucun changement de mot de passe
- **Priorité** : P2
- [ ]

### AUTH-09 — Vérification d'email après inscription simple
- **Rôle** : futur OWNER (inscription via `/register/user`)
- **Prérequis** : compte créé, email de vérification reçu (TTL 24h)
- **Étapes** :
  1. Cliquer le lien `/verify-email?token=...` reçu
- **Résultat attendu** : compte marqué vérifié, connexion possible ensuite
- **Priorité** : P1
- [ ]

### AUTH-10 — Renvoi de l'email de vérification
- **Rôle** : tous (compte non vérifié)
- **Prérequis** : compte créé, email non vérifié
- **Étapes** :
  1. Déclencher le renvoi (`POST /users/resend-verification`)
- **Résultat attendu** : nouvel email reçu avec un nouveau token valide 24h, l'ancien token devient invalide
- **Priorité** : P3
- [ ]

### AUTH-11 — Activation d'un compte créé par un administrateur
- **Rôle** : nouvel utilisateur créé via `POST /users` (admin)
- **Prérequis** : compte créé côté admin (module 10), email d'activation reçu
- **Étapes** :
  1. Cliquer le lien `/activate-account?token=...`
  2. Définir son mot de passe
- **Résultat attendu** : compte activé, connexion possible avec le mot de passe défini
- **Priorité** : P2
- [ ]

### AUTH-12 — Accès à une route protégée sans être connecté
- **Rôle** : ANONYME
- **Prérequis** : aucune session
- **Étapes** :
  1. Naviguer directement vers une URL protégée, ex. `/property-mngt/properties`
- **Résultat attendu** : redirection automatique vers `/login` (`ProtectedRoute`)
- **Priorité** : P2
- [ ]

### AUTH-13 — Accès à une route hors périmètre de rôle
- **Rôle** : OWNER
- **Prérequis** : connecté en tant que copropriétaire simple, sans rôle de gestion sur aucune copropriété
- **Étapes** :
  1. Naviguer directement vers `/property-mngt/properties`
- **Résultat attendu** : redirection vers `/forbidden` (`RequireAccess` / `canManageProperties` refuse) — vérifier aussi côté API que l'appel correspondant renvoie 403, pas seulement le front qui masque l'accès
- **Priorité** : P2
- [ ]

### AUTH-14 — Changement de mot de passe depuis le profil
- **Rôle** : tous
- **Prérequis** : connecté
- **Étapes** :
  1. Aller sur `/profile`
  2. Changer le mot de passe (`PATCH /users/me/password`)
- **Résultat attendu** : mot de passe mis à jour, ancien mot de passe refusé, nouveau accepté à la prochaine connexion
- **Priorité** : P2
- [ ]
