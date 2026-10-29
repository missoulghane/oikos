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
- **Prérequis** : copropriété sans exercice
- **Étapes** :
  1. `/property-mngt/properties/:id/accounting-exercise`, ouvrir un exercice
- **Résultat attendu** : `POST /properties/{id}/accounting/exercises` crée l'exercice, `GET .../exercises` le liste
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

### FIN-01b — Ouverture de l'exercice suivant avant la clôture
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre avec droit `canWriteAccounting`)
- **Prérequis** : un exercice ouvert (ex. 2026)
- **Étapes** :
  1. `/property-mngt/properties/:id/accounting-exercise`, « Ouvrir l'exercice suivant » (dates pré-remplies)
  2. Générer un appel de fonds de janvier 2027, puis un de décembre 2026
- **Résultat attendu** : deux exercices ouverts ; l'appel de janvier 2027 est rattaché à 2027, celui de décembre 2026 à 2026. Un exercice qui ne commence pas le lendemain du précédent est refusé (409), un troisième exercice ouvert aussi
- **Priorité** : P1
- [ ]

### FIN-03b — Annulation d'un appel de fonds
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : un appel de fonds sans aucun règlement, un autre partiellement réglé
- **Étapes** :
  1. `/property-mngt/properties/:id/installments/calls`, « Annuler l'appel » sur le premier, puis sur le second
- **Résultat attendu** : `POST /installment-calls/{id}/cancellation` passe une extourne au journal VT (l'écriture d'origine passe « Extournée »), l'appel reste listé « Annulé », ses échéances disparaissent et la période peut être regénérée. Le second est refusé (409 : appel déjà réglé)
- **Priorité** : P1
- [ ]

### FIN-05b — Imputation d'une avance sur un appel ultérieur
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre)
- **Prérequis** : un lot ayant payé d'avance (règlement supérieur à ce qui était dû)
- **Étapes** :
  1. Générer un appel dont l'échéance est déjà passée
  2. Sur la fiche du lot, « Imputer l'avance du lot » (`POST /properties/{id}/units/{unitId}/regularization`)
- **Résultat attendu** : l'avance est imputée dès la génération de l'appel (sinon par le bouton, ou par le balayage quotidien à 2 h 30 UTC) ; une écriture OD est passée, l'échéance est réglée
- **Priorité** : P2
- [ ]

### FIN-15 — Clôture de l'exercice comptable
- **Rôle** : SYNDIC (admin), MANAGER (admin) — `managesProperty`
- **Prérequis** : exercice ouvert, toutes ses écritures validées (aucun brouillon), numérotation des pièces continue dans chaque journal
- **Étapes** :
  1. `POST /properties/{id}/accounting/exercises/{exerciseId}/close`
- **Résultat attendu** : exercice clôturé, plus aucune écriture datée dans sa période ; avec un brouillon restant ou un trou de numérotation → 409 listant chaque manquement. Avec deux exercices ouverts, seul le plus ancien peut être clôturé (409 sur le plus récent)
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
