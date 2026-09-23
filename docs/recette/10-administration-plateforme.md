[← Retour à l'index](plan-recette.md)

# Module 10 — Administration plateforme

⚠️ **Aucune interface frontend dédiée n'a été trouvée** dans `oikos-web`
pour ce module au moment de la rédaction de ce plan (pas de route
`/admin*` ni de composant `UserAdmin*`). Les cas ci-dessous testent
`UserAdminController` directement au niveau API (Swagger UI ou client
HTTP), ce qui reste pertinent tant qu'aucun écran ADMIN n'existe encore
côté web — à réviser dès qu'une UI est ajoutée.

Endpoints (base `/users`, `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` sur
l'ensemble du contrôleur) : `GET /users`, `GET /users/{id}`,
`POST /users`, `PATCH /users/{id}/profile`, `PATCH /users/{id}/status`,
`DELETE /users/{id}`, `POST /users/{id}/resend-activation`.
Voir aussi `POST /installment-calls` (`InstallmentCallController`), seul
autre endpoint métier à exiger `ROLE_ADMIN` brut plutôt qu'un droit
`PropertyAccessEvaluator`.

### ADMIN-01 — Liste et recherche des utilisateurs
- **Rôle** : ADMIN (plateforme)
- **Prérequis** : compte `ROLE_ADMIN`
- **Étapes** :
  1. `GET /users` avec filtres (recherche, rôle, statut actif)
- **Résultat attendu** : résultats paginés et filtrés correctement
- **Priorité** : P2
- [ ]

### ADMIN-02 — Création d'un utilisateur par un administrateur
- **Rôle** : ADMIN (plateforme)
- **Prérequis** : aucun
- **Étapes** :
  1. `POST /users` avec les informations du nouvel utilisateur
- **Résultat attendu** : compte créé, email d'activation envoyé (lien `/activate-account?token=...`, TTL 24h) — voir AUTH-11 pour la suite du parcours
- **Priorité** : P2
- [ ]

### ADMIN-03 — Modification du profil d'un utilisateur
- **Rôle** : ADMIN (plateforme)
- **Prérequis** : utilisateur existant
- **Étapes** :
  1. `PATCH /users/{id}/profile`
- **Résultat attendu** : modification persistée
- **Priorité** : P3
- [ ]

### ADMIN-04 — Désactivation puis réactivation d'un utilisateur
- **Rôle** : ADMIN (plateforme)
- **Prérequis** : utilisateur actif
- **Étapes** :
  1. `PATCH /users/{id}/status` → désactiver
  2. Tenter une connexion avec ce compte (doit échouer, cf. AUTH-03)
  3. Réactiver le compte
- **Résultat attendu** : connexion bloquée pendant la désactivation, restaurée après réactivation
- **Priorité** : P2
- [ ]

### ADMIN-05 — Renvoi de l'email d'activation par un administrateur
- **Rôle** : ADMIN (plateforme)
- **Prérequis** : utilisateur créé (ADMIN-02) non encore activé
- **Étapes** :
  1. `POST /users/{id}/resend-activation`
- **Résultat attendu** : nouvel email d'activation reçu (202), ancien token invalidé
- **Priorité** : P3
- [ ]

### ADMIN-06 — Suppression d'un utilisateur
- **Rôle** : ADMIN (plateforme)
- **Prérequis** : utilisateur de test, sans données critiques liées (à évaluer selon les contraintes d'intégrité réelles)
- **Étapes** :
  1. `DELETE /users/{id}`
- **Résultat attendu** : suppression effective ou erreur explicite si des données liées empêchent la suppression (à documenter selon le comportement observé)
- **Priorité** : P3
- [ ]

### ADMIN-07 — Accès refusé aux endpoints d'administration pour un non-admin
- **Rôle** : SYNDIC (admin) ou MANAGER (admin) — le rôle le plus élevé hors ADMIN plateforme
- **Prérequis** : compte sans `ROLE_ADMIN`
- **Étapes** :
  1. Appeler `GET /users` avec ce compte
- **Résultat attendu** : `403` — `hasAuthority('ROLE_ADMIN')` ne prend en compte que le rôle plateforme, pas les rôles `PropertyRole` même admin-tier
- **Priorité** : P1
- [ ]

### ADMIN-08 — Création directe d'un appel de fonds via le point d'entrée admin
- **Rôle** : ADMIN (plateforme)
- **Prérequis** : copropriété existante
- **Étapes** :
  1. `POST /installment-calls` (point d'entrée admin brut, distinct de `POST /properties/{propertyId}/installment-calls` utilisé par le syndic, cf. FIN-03)
- **Résultat attendu** : appel de fonds créé ; à documenter : cas d'usage réel de cette voie de contournement (support, migration de données ?)
- **Priorité** : P3
- [ ]
