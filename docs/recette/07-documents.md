[← Retour à l'index](plan-recette.md)

# Module 7 — Documents

Route front : onglet "documents" de la fiche copropriété
(`/property-mngt/properties/:id/property/documents`).
Endpoints : `DocumentController` (`POST/GET/DELETE /documents`,
`GET /documents/{id}`, `GET /documents/{id}/content`), paramétrés par
`ownerType`/`ownerId` (une copropriété, un lot, une party, etc.).

### DOC-01 — Dépôt d'un document
- **Rôle** : SYNDIC (admin/membre avec droit d'écriture), MANAGER (admin/membre)
- **Prérequis** : copropriété existante
- **Étapes** :
  1. Onglet "documents", déposer un fichier (PDF, image)
- **Résultat attendu** : `POST /documents` (multipart) enregistre le document, visible immédiatement dans la liste
- **Priorité** : P2
- [ ]

### DOC-02 — Consultation et téléchargement d'un document
- **Rôle** : tout rôle ayant `canReadDocument` sur l'entité concernée (SYNDIC, MANAGER, ou OWNER selon le document)
- **Prérequis** : document déposé (DOC-01)
- **Étapes** :
  1. Ouvrir le document depuis la liste (`GET /documents/{id}`)
  2. Télécharger le contenu (`GET /documents/{id}/content`)
- **Résultat attendu** : métadonnées correctes, contenu téléchargé intact (pas de corruption)
- **Priorité** : P2
- [ ]

### DOC-03 — Suppression d'un document
- **Rôle** : SYNDIC (admin), MANAGER (admin/membre avec droit d'écriture)
- **Prérequis** : document déposé
- **Étapes** :
  1. Supprimer le document
- **Résultat attendu** : `DELETE /documents/{id}` retire le document, plus accessible en téléchargement ensuite
- **Priorité** : P3
- [ ]

### DOC-04 — Refus de dépôt sans droit d'écriture
- **Rôle** : OWNER (sans droit `canWriteDocument` sur la copropriété)
- **Prérequis** : connecté en tant que copropriétaire simple
- **Étapes** :
  1. Appeler directement `POST /documents` avec `ownerType`/`ownerId` visant la copropriété
- **Résultat attendu** : `403`
- **Priorité** : P2
- [ ]
