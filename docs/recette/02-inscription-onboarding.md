[← Retour à l'index](plan-recette.md)

# Module 2 — Inscription & onboarding

Routes front : `/register`, `/register/user`, `/register/manager-admin`,
`/register/board-admin/{account,property,dues-mode,unit-types,buildings,bank-accounts,summary,done}`.
Endpoints : `POST /users/onboarding-leads`, `/users/register-property-user`,
`/users/register-property-board-admin`, `/users/register-property-manager-admin`,
`POST /properties/{id}/configuration`.

Rappel important (confirmé dans le code, `RegisterPropertyBoardAdminService`
Javadoc) : dans le wizard syndic bénévole, **le compte utilisateur et la
copropriété ne sont créés qu'à l'étape 2** ("Votre copropriété"), pas à
l'étape 1. Les étapes 1 à 7 utilisent 7 sous-routes distinctes plus une page
finale `/done`.

### REG-01 — Choix du type d'inscription
- **Rôle** : ANONYME
- **Prérequis** : aucun
- **Étapes** :
  1. Aller sur `/register`
- **Résultat attendu** : proposition claire des parcours (copropriétaire, syndic bénévole, syndic professionnel), chaque choix mène à la bonne route
- **Priorité** : P3
- [ ]

### REG-02 — Inscription copropriétaire standard
- **Rôle** : futur OWNER
- **Prérequis** : aucun (ou invitation en main selon parcours testé, cf. module 3)
- **Étapes** :
  1. Aller sur `/register/user`
  2. Remplir le formulaire (`RegisterUserForm`) et valider
- **Résultat attendu** : `POST /users/register-property-user` réussit, email de vérification envoyé, compte utilisable après clic sur le lien
- **Priorité** : P2
- [ ]

### REG-03 — Inscription syndic professionnel (manager admin)
- **Rôle** : futur MANAGER (admin)
- **Prérequis** : aucun
- **Étapes** :
  1. Aller sur `/register/manager-admin`
  2. Remplir le formulaire (`RegisterPropertyAdminForm`) et valider
- **Résultat attendu** : `POST /users/register-property-manager-admin` crée le compte + la copropriété en un seul appel (contrairement au wizard bénévole, page unique, pas multi-étapes), email de vérification envoyé **de façon synchrone**
- **Priorité** : P1
- [ ]

### REG-04 — Wizard syndic bénévole, étape 1 "Votre compte" (capture du lead)
- **Rôle** : futur SYNDIC (admin)
- **Prérequis** : aucun
- **Étapes** :
  1. Aller sur `/register/board-admin/account`
  2. Saisir nom/email/téléphone/mot de passe, cliquer "Continuer"
- **Résultat attendu** : `POST /users/onboarding-leads` (202) déclenché, **aucun compte utilisateur créé en base à ce stade**, redirection vers l'étape 2
- **Priorité** : P1
- [ ]

### REG-05 — Wizard, étape 2 "Votre copropriété" (création effective du compte)
- **Rôle** : futur SYNDIC (admin)
- **Prérequis** : étape 1 complétée dans la session en cours
- **Étapes** :
  1. Sur `/register/board-admin/property`, saisir les infos de la copropriété, valider
- **Résultat attendu** : `POST /users/register-property-board-admin` crée dans une seule transaction la Property, la Party et le User ; un token d'onboarding est renvoyé pour authentifier les étapes suivantes ; email de vérification envoyé **de façon asynchrone** (`@Async`, fire-and-forget — ne doit pas ralentir la réponse HTTP)
- **Priorité** : P1
- [ ]

### REG-06 — Wizard, étape 3 "Mode de gestion"
- **Rôle** : SYNDIC (admin), authentifié via le token d'onboarding
- **Prérequis** : étape 2 complétée
- **Étapes** :
  1. Sur `/register/board-admin/dues-mode`, choisir un mode de calcul des charges, continuer
- **Résultat attendu** : choix conservé en état local (`OnboardingProvider`), pas encore persisté en base, navigation vers l'étape 4
- **Priorité** : P2
- [ ]

### REG-07 — Wizard, étape 4 "Types de lots"
- **Rôle** : SYNDIC (admin)
- **Prérequis** : étape 3 complétée
- **Étapes** :
  1. Sur `/register/board-admin/unit-types`, définir un ou plusieurs types de lots
- **Résultat attendu** : données conservées en état local, navigation vers l'étape 5
- **Priorité** : P2
- [ ]

### REG-08 — Wizard, étape 5 "Bâtiments et lots"
- **Rôle** : SYNDIC (admin)
- **Prérequis** : étape 4 complétée
- **Étapes** :
  1. Sur `/register/board-admin/buildings`, ajouter au moins un bâtiment et ses lots
- **Résultat attendu** : données conservées en état local, cohérentes avec les types de lots définis à l'étape 4
- **Priorité** : P2
- [ ]

### REG-09 — Wizard, étape 6 "Comptes bancaires"
- **Rôle** : SYNDIC (admin)
- **Prérequis** : étape 5 complétée
- **Étapes** :
  1. Sur `/register/board-admin/bank-accounts`, ajouter un compte bancaire
- **Résultat attendu** : données conservées en état local, navigation vers le récapitulatif
- **Priorité** : P2
- [ ]

### REG-10 — Wizard, étape 7 "Récapitulatif" (soumission finale)
- **Rôle** : SYNDIC (admin)
- **Prérequis** : étapes 3 à 6 complétées dans la session
- **Étapes** :
  1. Sur `/register/board-admin/summary`, relire le récapitulatif
  2. Valider
- **Résultat attendu** : `POST /properties/{id}/configuration` persiste en une fois mode de gestion, types de lots, bâtiments/lots et comptes bancaires ; redirection vers `/register/board-admin/done`
- **Priorité** : P1
- [ ]

### REG-11 — Abandon du wizard après l'étape 2
- **Rôle** : SYNDIC (admin)
- **Prérequis** : étape 2 complétée, puis fermeture de l'onglet/navigateur
- **Étapes** :
  1. Compléter l'étape 2 puis quitter sans terminer le wizard
  2. Se reconnecter plus tard avec les identifiants créés à l'étape 2
- **Résultat attendu** : le compte existe et permet la connexion ; la copropriété existe mais reste incomplète (pas de bâtiments/lots/comptes) ; vérifier qu'aucun écran de l'application ne casse face à cet état intermédiaire (ex. dashboard avec copropriété vide)
- **Priorité** : P2
- [ ]

### REG-12 — Vérification d'email suite à inscription bénévole (lien async)
- **Rôle** : SYNDIC (admin)
- **Prérequis** : étape 2 complétée (REG-05)
- **Étapes** :
  1. Attendre la réception de l'email de vérification (envoi asynchrone — l'écran de l'étape 2/3 ne doit pas attendre cet envoi)
  2. Cliquer le lien `/verify-email?token=...`
- **Résultat attendu** : email reçu en quelques secondes malgré l'envoi async, compte marqué vérifié après clic
- **Priorité** : P1
- [ ]

### REG-13 — Rechargement de page en cours de wizard
- **Rôle** : SYNDIC (admin)
- **Prérequis** : en cours d'étape 4 ou 5
- **Étapes** :
  1. Recharger la page (F5) en pleine étape
- **Résultat attendu** : comportement défini et sans perte silencieuse de données déjà saisies dans les étapes précédentes de la session (à valider selon ce que l'`OnboardingProvider` persiste réellement — sessionStorage ou mémoire pure) ; documenter le comportement observé
- **Priorité** : P3
- [ ]
