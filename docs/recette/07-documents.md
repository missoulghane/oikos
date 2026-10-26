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
  1. Onglet "documents", choisir un ou plusieurs fichiers (PDF, image), saisir un titre pour l'un d'eux et laisser l'autre vide, cliquer « Ajouter »
  2. Refaire l'essai avec un fichier volumineux
- **Résultat attendu** : `POST /documents` (multipart, paramètre `title` facultatif) enregistre le document, visible immédiatement dans la liste sous son titre (à défaut : nom du fichier sans extension) ; plus aucune mention « Téléverser » ; une barre de progression indique l'avancement de l'envoi
- **Priorité** : P2
- [ ]

### DOC-02 — Consultation et téléchargement d'un document
- **Rôle** : tout rôle ayant `canReadDocument` sur l'entité concernée (SYNDIC, MANAGER, ou OWNER selon le document)
- **Prérequis** : document déposé (DOC-01)
- **Étapes** :
  1. Cliquer sur le titre du document dans la liste
- **Résultat attendu** : le téléchargement démarre directement (`GET /documents/{id}/content`), avec une barre de progression pour un gros fichier ; contenu intact (pas de corruption)
- **Priorité** : P2
- [ ]

### DOC-02b — Recherche et pagination
- **Rôle** : SYNDIC, MANAGER
- **Prérequis** : plus de 10 documents déposés
- **Étapes** :
  1. Parcourir les pages de la liste
  2. Saisir une partie d'un titre ou d'un nom de fichier dans « Rechercher un document »
- **Résultat attendu** : 10 documents par page, du plus récent au plus ancien ; la recherche (`GET /documents?search=…`, insensible à la casse, sur le titre et le nom du fichier) repart de la page 1 ; message « Aucun résultat » si rien ne correspond
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
