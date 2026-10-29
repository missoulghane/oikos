[← Retour à l'index](plan-recette.md)

# Module 2 — Inscription & onboarding

Routes front : `/register`, `/register/user`, `/register/manager-admin`,
`/register/board-admin/{account,property,dues-mode,unit-types,buildings,bank-accounts,summary,done}`.
Endpoints : `/users/register-property-user`,
`/users/register-property-board-admin`, `POST /users/me/board-admin-property`,
`/users/register-property-manager-admin`, `POST /properties/{id}/configuration`.

Rappel important : dans le wizard syndic bénévole, **le compte utilisateur est
créé à l'étape 1** ("Votre compte") et **la copropriété à l'étape 2** ("Votre
copropriété"). Les étapes 1 à 7 utilisent 7 sous-routes distinctes plus une
page finale `/done`. Le syndic professionnel n'est plus proposé sur `/register`
(la route `/register/manager-admin` reste accessible directement).

### REG-01 — Choix du type d'inscription
- **Rôle** : ANONYME
- **Prérequis** : aucun
- **Étapes** :
  1. Aller sur `/register`
- **Résultat attendu** : deux parcours proposés (« Syndic ou membre du conseil syndical », copropriétaire), chaque choix mène à la bonne route ; plus d'option « Syndic professionnel »
- **Priorité** : P3
- [ ]

### REG-02 — Inscription copropriétaire standard
- **Rôle** : futur OWNER
- **Prérequis** : aucun (ou invitation en main selon parcours testé, cf. module 3)
- **Étapes** :
  1. Aller sur `/register/user`
  2. Remplir le formulaire (`RegisterUserForm`) et valider
- **Résultat attendu** : `POST /users/register-property-user` répond 202 (sans `Location`), email de vérification au gabarit HTML commun envoyé, compte utilisable après clic sur le lien. Avec une adresse déjà inscrite : même réponse 202 et même écran, aucun compte créé ; la boîte reçoit « Vous avez déjà un compte » (liens connexion / mot de passe oublié)
- **Priorité** : P2
- [ ]

### REG-03 — Inscription syndic professionnel (manager admin)
- **Rôle** : futur MANAGER (admin)
- **Prérequis** : aucun
- **Étapes** :
  1. Aller sur `/register/manager-admin`
  2. Remplir le formulaire (`RegisterPropertyAdminForm`) et valider
- **Résultat attendu** : `POST /users/register-property-manager-admin` répond 202 (sans `Location`) et crée le compte + la copropriété en un seul appel (contrairement au wizard bénévole, page unique, pas multi-étapes), email de vérification envoyé **de façon synchrone**. Adresse déjà inscrite : même réponse, rien de créé, email « Vous avez déjà un compte »
- **Priorité** : P1
- [ ]

### REG-04 — Wizard syndic bénévole, étape 1 "Votre compte" (création du compte)
- **Rôle** : futur SYNDIC (admin)
- **Prérequis** : aucun
- **Étapes** :
  1. Aller sur `/register/board-admin/account`
  2. Saisir nom/email/téléphone/mot de passe, choisir son rôle dans le conseil syndical (« Syndic bénévole » par défaut ; mêmes libellés que « Ajouter un membre », sans « Gestionnaire »), cocher « J'accepte les CGV et CGU » (mention en petit et en gris), cliquer "Continuer"
  3. Refaire l'essai sans cocher la case CGV/CGU
  4. Refaire l'essai avec une adresse déjà inscrite
- **Résultat attendu** : `POST /users/register-property-board-admin` (201) crée le compte seul (`app_user.terms_accepted_at` renseigné), renvoie un jeton d'onboarding de compte, email de vérification (gabarit HTML commun) envoyé de façon asynchrone ; redirection vers l'étape 2. Sans la case cochée : message « Vous devez accepter les CGV et CGU », aucun appel. Adresse déjà inscrite : refus, aucun compte créé. Revenir à l'étape 1 affiche « Votre compte est créé » sans recréer le compte
- **Priorité** : P1
- [ ]

### REG-05 — Wizard, étape 2 "Votre copropriété" (création de la copropriété)
- **Rôle** : futur SYNDIC (admin)
- **Prérequis** : étape 1 complétée dans la session en cours
- **Étapes** :
  1. Sur `/register/board-admin/property`, saisir les infos de la copropriété, valider
- **Résultat attendu** : `POST /users/me/board-admin-property` (authentifié par le jeton de l'étape 1) crée dans une seule transaction la Property (**sans type de lot « OTHERS »**), la Party, le siège au conseil syndical choisi à l'étape 1 et le rôle `PROPERTY_BOARD_ADMIN` ; un jeton d'onboarding de copropriété est renvoyé pour authentifier les étapes suivantes. Un second envoi pour le même compte est refusé (409)
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
- **Résultat attendu** : les types et leurs descriptions viennent de la table `unit_type_choice` (Appartement, Parking, Box, Bureau - chacun avec sa description) ; mêmes types que l'onglet Configuration de la copropriété ; données conservées en état local, navigation vers l'étape suivante
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

### REG-11a — Abandon du wizard après l'étape 1
- **Rôle** : SYNDIC (admin)
- **Prérequis** : étape 1 complétée, puis fermeture de l'onglet
- **Étapes** :
  1. Activer le compte depuis l'email, se connecter
  2. Sur le tableau de bord, cliquer « Créer ma copropriété »
- **Résultat attendu** : l'étape 2 s'ouvre connectée (avec le choix du rôle au conseil syndical), la copropriété est créée avec la session, le wizard se poursuit normalement
- **Priorité** : P2
- [ ]

### REG-11 — Abandon du wizard après l'étape 2
- **Rôle** : SYNDIC (admin)
- **Prérequis** : étape 2 complétée, puis fermeture de l'onglet/navigateur
- **Étapes** :
  1. Compléter l'étape 2 puis quitter sans terminer le wizard
  2. Se reconnecter plus tard avec les identifiants créés à l'étape 1
- **Résultat attendu** : le compte existe et permet la connexion ; la copropriété existe mais reste incomplète (pas de bâtiments/lots/comptes) ; vérifier qu'aucun écran de l'application ne casse face à cet état intermédiaire (ex. dashboard avec copropriété vide)
- **Priorité** : P2
- [ ]

### REG-12 — Vérification d'email suite à inscription bénévole (lien async, gabarit HTML)
- **Rôle** : SYNDIC (admin)
- **Prérequis** : étape 1 complétée (REG-04)
- **Étapes** :
  1. Attendre la réception de l'email de vérification (envoi asynchrone — l'écran de l'étape 1/2 ne doit pas attendre cet envoi)
  2. L'ouvrir dans Gmail (web et mobile) et Outlook, puis cliquer « Confirmer mon adresse email »
- **Résultat attendu** : email reçu en quelques secondes malgré l'envoi async, au gabarit commun (`templates/email/layout.html` : en-tête Daba Syndic, cadre blanc, bouton, lien de secours, pied de page « Merci de ne pas y répondre ») ; compte marqué vérifié après clic. Même gabarit pour tous les emails : vérification, réinitialisation de mot de passe, activation, invitations (copropriétaire et conseil syndical), décisions d'adhésion, convocations et rappels d'AG
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
