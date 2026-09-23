[← Retour à l'index](plan-recette.md)

# Module 4 — Copropriétés, bâtiments & lots

Routes front : `/property-mngt/properties`, `/properties/new`,
`/properties/:id/property` (onglets infos générales, lots, contacts,
groupes de messagerie, demandes d'adhésion, documents, configuration),
`/properties/:propertyId/buildings/new`,
`/properties/:propertyId/units/:unitId`, `/property-mngt/parties`.
Endpoints : `PropertyController`, `BuildingController`, `UnitController`,
`UnitOwnershipController`, `UnitTypeDefinitionController`,
`UnitTypePricingController`, `BoardMemberController`,
`PropertyContactController`, `PartyController`, `PartyLotsController`.

### PROP-01 — Liste des copropriétés gérées
- **Rôle** : SYNDIC (admin/membre), MANAGER (admin/membre)
- **Prérequis** : compte lié à au moins une copropriété
- **Étapes** :
  1. Aller sur `/property-mngt/properties`
- **Résultat attendu** : liste limitée aux copropriétés gérées par le compte (sauf ADMIN plateforme qui voit tout, `GET /properties`)
- **Priorité** : P2
- [ ]

### PROP-02 — Création d'une copropriété hors wizard
- **Rôle** : SYNDIC (admin) ou MANAGER (admin) déjà titulaire d'un autre rôle admin-tier
- **Prérequis** : compte déjà admin-tier sur au moins une copropriété (ou ADMIN plateforme)
- **Étapes** :
  1. Aller sur `/property-mngt/properties/new`
  2. Remplir et valider
- **Résultat attendu** : `POST /properties` crée la copropriété ; vérifier que la cardinalité métier (un compte SYNDIC bénévole ne peut créer qu'une seule copropriété, cf. `RoleCategory` Javadoc) est bien appliquée si applicable
- **Priorité** : P2
- [ ]

### PROP-03 — Modification des informations générales
- **Rôle** : SYNDIC (admin), MANAGER (admin)
- **Prérequis** : copropriété existante
- **Étapes** :
  1. Onglet "infos générales" de la fiche copropriété, modifier un champ, enregistrer
- **Résultat attendu** : `PUT /properties/{id}` persiste la modification
- **Priorité** : P2
- [ ]

### PROP-04 — Ajout d'un bâtiment
- **Rôle** : SYNDIC (admin), MANAGER (admin)
- **Prérequis** : copropriété existante
- **Étapes** :
  1. `/property-mngt/properties/:propertyId/buildings/new`, remplir, valider
- **Résultat attendu** : `POST /properties/{propertyId}/buildings` crée le bâtiment, visible dans la liste
- **Priorité** : P2
- [ ]

### PROP-05 — Ajout d'un lot à un bâtiment
- **Rôle** : SYNDIC (admin), MANAGER (admin)
- **Prérequis** : bâtiment existant
- **Étapes** :
  1. Ajouter un lot au bâtiment, avec type de lot et tantièmes
- **Résultat attendu** : `POST /buildings/{buildingId}/units` crée le lot
- **Priorité** : P2
- [ ]

### PROP-06 — Modification des tantièmes d'un lot
- **Rôle** : SYNDIC (admin), MANAGER (admin)
- **Prérequis** : lot existant, idéalement dans une copropriété avec appels de fonds déjà générés
- **Étapes** :
  1. Modifier les tantièmes du lot (`PUT /units/{id}/shares`)
- **Résultat attendu** : mise à jour effective ; vérifier l'impact (ou l'absence d'impact rétroactif) sur les appels de fonds déjà émis, à documenter selon le comportement observé
- **Priorité** : P1
- [ ]

### PROP-07 — Gestion des types de lots et de leur tarification
- **Rôle** : SYNDIC (admin), MANAGER (admin)
- **Prérequis** : copropriété existante
- **Étapes** :
  1. Ajouter un type de lot (`POST /properties/{id}/unit-types`)
  2. Lui affecter un prix (`PUT /properties/{id}/unit-type-prices/{unitTypeId}`)
  3. Supprimer un type de lot inutilisé
- **Résultat attendu** : création/mise à jour/suppression fonctionnelles ; suppression d'un type encore utilisé par un lot correctement bloquée ou gérée
- **Priorité** : P2
- [ ]

### PROP-08 — Gestion des membres du conseil syndical
- **Rôle** : SYNDIC (admin)
- **Prérequis** : copropriété avec au moins un copropriétaire
- **Étapes** :
  1. Ajouter un membre au conseil syndical (`POST /properties/{id}/board-members`)
  2. Le valider (`PATCH .../{id}/validate`)
  3. Le retirer (`DELETE .../{id}`)
- **Résultat attendu** : cycle complet fonctionnel, droits `PROPERTY_BOARD_MEMBER` effectifs après validation
- **Priorité** : P2
- [ ]

### PROP-09 — Rattachement d'un copropriétaire à un lot
- **Rôle** : SYNDIC (admin), MANAGER (admin)
- **Prérequis** : lot sans propriétaire, party existante ou à créer
- **Étapes** :
  1. Sur le lot, lier une party existante (`POST /units/{unitId}/owners`) ou en créer une nouvelle (`.../owners/new-party`)
- **Résultat attendu** : lien propriétaire/lot créé, la party accède désormais au lot depuis son espace copropriétaire (module 9)
- **Priorité** : P1
- [ ]

### PROP-10 — Consultation des contacts de la copropriété
- **Rôle** : SYNDIC (admin/membre), MANAGER (admin/membre)
- **Prérequis** : copropriété avec contacts renseignés
- **Étapes** :
  1. Onglet "contacts" de la fiche copropriété
- **Résultat attendu** : liste paginée correcte, recherche par email fonctionnelle
- **Priorité** : P3
- [ ]

### PROP-11 — Accès refusé à la création de copropriété pour un profil non habilité
- **Rôle** : SYNDIC (membre) ou MANAGER (membre)
- **Prérequis** : compte membre-tier, sans droit admin-tier
- **Étapes** :
  1. Tenter d'accéder à `/property-mngt/properties/new`
- **Résultat attendu** : redirection `/forbidden` côté front (`canCreateProperty`) et `403` côté API sur `POST /properties` en appel direct
- **Priorité** : P2
- [ ]

### PROP-12 — Retrait/désolidarisation d'un propriétaire d'un lot
- **Rôle** : SYNDIC (admin), MANAGER (admin)
- **Prérequis** : lot avec au moins un propriétaire lié
- **Étapes** :
  1. Retirer le lien propriétaire (`DELETE /units/{unitId}/owners/{id}`)
- **Résultat attendu** : la party perd l'accès au lot ; vérifier le comportement si c'était son seul lot dans la copropriété (accès résiduel ou non)
- **Priorité** : P2
- [ ]
