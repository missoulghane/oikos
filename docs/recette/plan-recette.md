# Plan de recette fonctionnelle — Oikos

Ce plan couvre `oikos-api` (Spring Boot, préfixe `/api/v1` sur tous les
endpoints listés) et `oikos-web` (React/Vite). `oikos-mobile` n'est pas
couvert ici. Les cas de test sont répartis par module fonctionnel dans des
fichiers séparés ; ce fichier sert d'index et regroupe les tests transverses
("smoke tests multi-domaines").

Construit à partir des routes et endpoints réellement présents dans le code
au 2026-09-22 (voir `oikos-web/src/router/`, les contrôleurs sous
`oikos-api/src/main/java/com/architek/oikos/**/web/controller/`, et les
`*EmailComposer` pour les emails transactionnels).

## Comment utiliser ce plan

- Chaque cas de test a un ID unique, un rôle, des prérequis, des étapes, un
  résultat attendu, une priorité (P1 = bloquant, P2 = important,
  P3 = confort) et une case à cocher pour le suivi d'exécution.
- Cocher `- [x]` uniquement après exécution réelle sur l'environnement
  cible, pas par anticipation.
- Un cas qui échoue reste décoché ; noter le ticket/anomalie associé en
  commentaire Markdown juste en dessous si besoin.

## Rôles utilisés dans ce plan

Le code distingue des rôles plus fins que le triptyque usuel
OWNER/SYNDIC/MANAGER ; ce plan garde les deux niveaux pour rester lisible :

| Libellé dans ce plan | Rôle technique (`PropertyRole`) | Sens métier |
|---|---|---|
| **ANONYME** | — | Visiteur non connecté |
| **OWNER** | `PROPERTY_OWNER` | Copropriétaire |
| **SYNDIC (admin)** | `PROPERTY_BOARD_ADMIN` | Président du conseil syndical (syndic bénévole) |
| **SYNDIC (membre)** | `PROPERTY_BOARD_MEMBER` | Membre du conseil syndical |
| **MANAGER (admin)** | `PROPERTY_MANAGER_ADMIN` | Gérant du cabinet de syndic professionnel |
| **MANAGER (membre)** | `PROPERTY_MANAGER_MEMBER` | Collaborateur du cabinet de syndic professionnel |
| **ADMIN** | `ROLE_ADMIN` / `ROLE_MASTER` | Administrateur plateforme (accès global) |

Les rôles "admin" (SYNDIC admin, MANAGER admin) créent/possèdent une
copropriété ; les rôles "membre" n'y sont qu'invités. Autorisation réelle
appliquée côté API via `PropertyAccessEvaluator` (permissions dérivées du
rôle sur la copropriété ciblée) — les gardes de route côté front
(`RequireAccess`) ne font qu'éviter des clics dans le vide, l'API reste la
seule frontière de sécurité réelle.

## Modules

1. [Authentification & compte](01-authentification.md)
2. [Inscription & onboarding](02-inscription-onboarding.md)
3. [Invitations & demandes d'adhésion](03-invitations-adhesions.md)
4. [Copropriétés, bâtiments & lots](04-coproprietes-batiments-lots.md)
5. [Assemblées générales & convocations](05-assemblees-generales.md)
6. [Finances & comptabilité](06-finances-comptabilite.md)
7. [Documents](07-documents.md)
8. [Messagerie & notifications](08-messagerie-notifications.md)
9. [Espace copropriétaire](09-espace-coproprietaire.md)
10. [Administration plateforme](10-administration-plateforme.md)

---

## Smoke tests multi-domaines

Le même déploiement (`docker-compose.yml` + `docker-compose.staging.yml` +
Caddy) répond sur **deux domaines distincts** : `oikos-staging.tech` et
`daba-syndic.com` / `www.daba-syndic.com` (voir `Caddyfile:1`). Les deux
pointent vers les mêmes conteneurs `api`/`web` — mais plusieurs réglages
d'environnement sont **mono-valués** côté serveur (`APP_PUBLIC_BASE_URL`,
`APP_CORS_ALLOWED_ORIGINS`), ce qui peut créer des comportements différents
selon le domaine utilisé. Ces tests visent spécifiquement ces angles morts,
à rejouer après chaque déploiement ou changement de configuration serveur.

### SMOKE-01 — TLS et chargement de la SPA sur `oikos-staging.tech`
- **Rôle** : ANONYME
- **Prérequis** : déploiement à jour
- **Étapes** :
  1. Ouvrir `https://oikos-staging.tech` dans un navigateur
  2. Vérifier le certificat TLS (cadenas, pas d'avertissement)
  3. Vérifier que la page de connexion se charge sans erreur console
- **Résultat attendu** : certificat valide (Caddy), SPA affichée, aucune erreur JS bloquante
- **Priorité** : P1
- [ ]

### SMOKE-02 — TLS et chargement de la SPA sur `daba-syndic.com` et `www.daba-syndic.com`
- **Rôle** : ANONYME
- **Prérequis** : déploiement à jour
- **Étapes** :
  1. Ouvrir `https://daba-syndic.com` puis `https://www.daba-syndic.com`
  2. Vérifier le certificat TLS sur chacun
  3. Vérifier que la page de connexion se charge sans erreur console
- **Résultat attendu** : les deux sous-domaines répondent en HTTPS valide et affichent la même SPA
- **Priorité** : P1
- [ ]

### SMOKE-03 — Connexion (login) réussie depuis `oikos-staging.tech`
- **Rôle** : n'importe quel rôle applicatif (compte de test existant)
- **Prérequis** : compte actif sur l'environnement
- **Étapes** :
  1. Depuis `https://oikos-staging.tech/login`, se connecter avec un compte valide
  2. Observer la console réseau du navigateur pendant l'appel `POST /api/v1/auth/login`
- **Résultat attendu** : connexion réussie, redirection vers `/dashboard`, aucune erreur CORS en console
- **Priorité** : P1
- [ ]

### SMOKE-04 — Connexion (login) réussie depuis `daba-syndic.com`
- **Rôle** : idem SMOKE-03
- **Prérequis** : même compte que SMOKE-03
- **Étapes** :
  1. Depuis `https://daba-syndic.com/login`, se connecter avec le même compte
  2. Observer la console réseau pendant `POST /api/v1/auth/login`
- **Résultat attendu** : connexion réussie, aucune erreur `Invalid CORS request`
- **Priorité** : P1
- **⚠️ Point d'attention documenté dans le code** : `oikos-api/.env.example:13-19` prévient explicitement que `APP_CORS_ALLOWED_ORIGINS` ne liste souvent **qu'une seule** origine (ex. `https://oikos-staging.tech`) — si `daba-syndic.com` n'y est pas ajouté, ce test échoue précisément avec `Invalid CORS request` au login, même si `VITE_API_URL` étant relatif (`/api/v1`, voir `.github/workflows/deploy-staging.yml:75`) l'appel reste techniquement same-path. Vérifier la valeur réelle de `APP_CORS_ALLOWED_ORIGINS` dans `/opt/oikos/oikos-api/.env` sur le serveur si ce test échoue.
- [ ]

### SMOKE-05 — Endpoint de santé accessible sur les deux domaines
- **Rôle** : ANONYME
- **Prérequis** : déploiement à jour
- **Étapes** :
  1. `curl -I https://oikos-staging.tech/api/v1/actuator/health`
  2. `curl -I https://daba-syndic.com/api/v1/actuator/health`
- **Résultat attendu** : `200 OK` avec `{"status":"UP"}` sur les deux
- **Priorité** : P2
- [ ]

### SMOKE-06 — Swagger UI accessible sur les deux domaines
- **Rôle** : ANONYME
- **Prérequis** : déploiement à jour
- **Étapes** :
  1. `curl -I https://oikos-staging.tech/api/v1/swagger-ui.html` (ou `/swagger-ui/index.html`)
  2. `curl -I https://daba-syndic.com/api/v1/swagger-ui.html`
- **Résultat attendu** : `200 OK` sur les deux domaines
- **Priorité** : P3
- [ ]

### SMOKE-07 — Lien d'un email de vérification/invitation/reset — cohérence de domaine
- **Rôle** : ANONYME → futur OWNER/SYNDIC
- **Prérequis** : accès à la boîte mail utilisée pour le test
- **Étapes** :
  1. Déclencher un envoi d'email (ex. inscription syndic bénévole étape 2, ou "mot de passe oublié") **depuis** `daba-syndic.com`
  2. Ouvrir l'email reçu et relever le domaine du lien (vérification, activation, invitation, reset)
  3. Cliquer le lien et vérifier qu'il aboutit à une page fonctionnelle (pas d'erreur 404/DNS)
- **Résultat attendu** : le lien pointe vers le domaine configuré en `APP_PUBLIC_BASE_URL` côté serveur (probablement `oikos-staging.tech`, valeur unique commune aux deux domaines — voir `oikos-api/.env.example:69-71`), **même si l'inscription a été initiée sur `daba-syndic.com`**. Comportement attendu et non un bug tant que ce domaine répond correctement : à documenter/valider auprès du produit si `daba-syndic.com` doit un jour devenir le domaine canonique des emails.
- **Priorité** : P1
- [ ]

### SMOKE-08 — Confirmation de convocation (lien public + QR code) sur les deux domaines
- **Rôle** : ANONYME (copropriétaire sans compte)
- **Prérequis** : une convocation envoyée à une adresse de test (voir [05 — AG-05](05-assemblees-generales.md))
- **Étapes** :
  1. Ouvrir le lien de confirmation reçu par email (`/convocations/confirmation?token=...`)
  2. Vérifier qu'il s'ouvre sur le domaine `APP_CONVOCATION_CONFIRMATION_BASE_URL` configuré et fonctionne
  3. Scanner le QR code du PDF joint et vérifier qu'il pointe vers la même URL fonctionnelle
- **Résultat attendu** : page de confirmation accessible sans compte, saisie du code à 6 caractères acceptée, réponse enregistrée
- **Priorité** : P1
- [ ]

### SMOKE-09 — Absence d'erreur CORS sur les appels API authentifiés depuis chaque domaine
- **Rôle** : SYNDIC (admin) ou MANAGER (admin)
- **Prérequis** : connecté sur chaque domaine (cf. SMOKE-03/04)
- **Étapes** :
  1. Depuis `oikos-staging.tech`, naviguer dans `/property-mngt/properties` et charger une fiche copropriété
  2. Répéter depuis `daba-syndic.com` avec le même compte
- **Résultat attendu** : aucune requête bloquée par CORS dans la console réseau, sur aucun des deux domaines
- **Priorité** : P1
- [ ]
