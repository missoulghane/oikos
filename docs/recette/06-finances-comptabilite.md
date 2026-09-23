[← Retour à l'index](plan-recette.md)

# Module 6 — Finances & comptabilité

Routes front : `/property-mngt/properties/:id/installments` (liste,
appels de fonds, configuration), `/property-mngt/properties/:id/accounting`
(vue d'ensemble, comptes, trésorerie, journal, dépenses),
`/property-mngt/properties/:id/accounting-exercise`,
`/property-mngt/properties/:propertyId/units/:unitId/payment`.
Endpoints : `InstallmentCallController`, `InstallmentController`,
`PaymentController`, et les contrôleurs comptables (`BankAccountController`,
`BankChargeController`, `ExpenseController`, `JournalEntryController`,
`LedgerAccountController`, `PayrollExpenseController`, `PeriodController`,
`SupplierPaymentController`, `TreasuryTransferController`).

### FIN-01 — Ouverture d'un exercice comptable
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre avec droit `canWriteAccounting`)
- **Prérequis** : copropriété sans exercice ouvert
- **Étapes** :
  1. `/property-mngt/properties/:id/accounting-exercise`, ouvrir un exercice
- **Résultat attendu** : `POST /properties/{id}/accounting/exercises` crée l'exercice, `GET .../exercises/open` le renvoie
- **Priorité** : P1
- [ ]

### FIN-02 — Ajout d'un compte bancaire
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : exercice ouvert
- **Étapes** :
  1. `/property-mngt/properties/:id/accounting/treasury-accounts/new`, remplir, valider
- **Résultat attendu** : `POST /properties/{id}/accounting/bank-accounts` crée le compte, visible dans la vue trésorerie
- **Priorité** : P1
- [ ]

### FIN-03 — Génération d'un appel de fonds
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre avec droit `canWriteInstallmentCall`)
- **Prérequis** : copropriété avec lots et tantièmes définis, exercice ouvert
- **Étapes** :
  1. `/property-mngt/properties/:id/installments/calls`, générer un appel de fonds
- **Résultat attendu** : `POST /properties/{id}/installment-calls` crée l'appel, une échéance générée par lot au prorata des tantièmes
- **Priorité** : P1
- [ ]

### FIN-04 — Consultation du récapitulatif de collecte
- **Rôle** : SYNDIC (admin/membre), MANAGER (admin/membre)
- **Prérequis** : au moins un appel de fonds avec quelques paiements enregistrés
- **Étapes** :
  1. `GET /properties/{id}/installments/collection-summary`
- **Résultat attendu** : taux de recouvrement cohérent avec les paiements réellement enregistrés
- **Priorité** : P2
- [ ]

### FIN-05 — Enregistrement d'un paiement copropriétaire
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : échéance en attente sur un lot
- **Étapes** :
  1. `/property-mngt/properties/:propertyId/units/:unitId/payment`, saisir le paiement
- **Résultat attendu** : `POST /properties/{propertyId}/units/{unitId}/payments` enregistre le paiement, solde de l'échéance mis à jour
- **Priorité** : P1
- [ ]

### FIN-06 — Téléchargement du reçu de paiement
- **Rôle** : SYNDIC (admin/membre), MANAGER (admin/membre), OWNER (son propre paiement)
- **Prérequis** : paiement enregistré (FIN-05)
- **Étapes** :
  1. `GET /payments/{paymentId}/receipt`
- **Résultat attendu** : PDF de reçu généré et téléchargeable, données conformes au paiement
- **Priorité** : P2
- [ ]

### FIN-07 — Enregistrement d'une dépense fournisseur
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : exercice ouvert, compte bancaire existant
- **Étapes** :
  1. `/property-mngt/properties/:id/accounting/treasury-accounts/:accountId/expenses/new`
- **Résultat attendu** : `POST /properties/{id}/accounting/supplier-payments` enregistre la dépense, impact visible en trésorerie et dans le journal
- **Priorité** : P2
- [ ]

### FIN-08 — Enregistrement d'une charge bancaire
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : compte bancaire existant
- **Étapes** :
  1. Enregistrer des frais bancaires
- **Résultat attendu** : `POST /properties/{id}/accounting/bank-charges` reflété dans le solde du compte
- **Priorité** : P3
- [ ]

### FIN-09 — Enregistrement d'une dépense de paie
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : exercice ouvert
- **Étapes** :
  1. `/property-mngt/properties/:id/accounting/payroll-expenses/new`
- **Résultat attendu** : `POST /properties/{id}/accounting/payroll-expenses` enregistrée correctement
- **Priorité** : P3
- [ ]

### FIN-10 — Virement entre comptes de trésorerie
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : au moins deux comptes bancaires
- **Étapes** :
  1. `/property-mngt/properties/:id/accounting/treasury-accounts/:accountId/transfers/new`
- **Résultat attendu** : `POST /properties/{id}/accounting/treasury-transfers` débite un compte et crédite l'autre du même montant
- **Priorité** : P2
- [ ]

### FIN-11 — Création et validation d'une écriture au journal
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : exercice ouvert
- **Étapes** :
  1. Créer une écriture (`POST /properties/{id}/accounting/entries`)
  2. La valider (`POST .../entries/{entryId}/validation`)
- **Résultat attendu** : écriture équilibrée acceptée, déséquilibrée rejetée ; une fois validée, non modifiable
- **Priorité** : P2
- [ ]

### FIN-12 — Consultation du plan comptable et des écritures par compte
- **Rôle** : SYNDIC (admin/membre avec `canReadAccounting`), MANAGER (admin/membre)
- **Prérequis** : écritures existantes
- **Étapes** :
  1. `GET /properties/{id}/accounting/ledger-accounts`
  2. `GET .../ledger-accounts/{accountId}/entries`
- **Résultat attendu** : plan comptable et détail par compte cohérents avec les écritures saisies
- **Priorité** : P2
- [ ]

### FIN-13 — Clôture d'une période comptable
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre avec `canWriteAccounting`)
- **Prérequis** : période en cours avec écritures validées
- **Étapes** :
  1. `POST /properties/{id}/accounting/periods/{period}/close`
- **Résultat attendu** : période clôturée, plus d'écriture possible dessus
- **Priorité** : P2
- [ ]

### FIN-14 — Réouverture d'une période comptable clôturée
- **Rôle** : SYNDIC (admin), MANAGER (admin) — droit `managesProperty` (plus restrictif que la clôture)
- **Prérequis** : période clôturée (FIN-13)
- **Étapes** :
  1. `POST /properties/{id}/accounting/periods/{period}/reopen`
- **Résultat attendu** : réouverture réservée aux profils admin-tier, refusée à un simple membre avec seulement `canWriteAccounting`
- **Priorité** : P3
- [ ]

### FIN-15 — Clôture de l'exercice comptable
- **Rôle** : SYNDIC (admin), MANAGER (admin) — `managesProperty`
- **Prérequis** : toutes les périodes de l'exercice clôturées
- **Étapes** :
  1. `POST /properties/{id}/accounting/exercises/close`
- **Résultat attendu** : exercice clôturé, plus aucune écriture possible sans ouverture d'un nouvel exercice
- **Priorité** : P2
- [ ]

### FIN-16 — Accès refusé d'un copropriétaire aux endpoints comptables
- **Rôle** : OWNER
- **Prérequis** : connecté en tant que copropriétaire simple
- **Étapes** :
  1. Appeler directement `GET /properties/{id}/accounting/ledger-accounts` avec le token OWNER
- **Résultat attendu** : `403` — un OWNER n'a jamais `canReadAccounting`/`canWriteAccounting` sur la comptabilité de la copropriété
- **Priorité** : P1
- [ ]
