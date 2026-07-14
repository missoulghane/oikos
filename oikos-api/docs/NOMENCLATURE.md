# Nomenclature FR → EN

Référencé par [ARCHITECTURE.md, règle 2](ARCHITECTURE.md#règle-2--nomenclature-en-anglais).
Table de correspondance entre le vocabulaire métier français (SFD, échanges
fonctionnels) et les noms utilisés dans le code. À compléter à chaque
nouvelle SFD, **avant** d'écrire le code correspondant.

## Module structure de copropriété (`copropriete` → `property`)

| Terme FR (SFD)                    | Nom dans le code (EN)         | Notes |
|------------------------------------|--------------------------------|-------|
| Copropriété                        | `Property`                     | Le domaine produit reste "property management" (déjà dans le README) ; l'agrégat est `Property`. |
| Immeuble                           | `Building`                     | |
| Lot                                | `Unit`                         | Terme standard en gestion immobilière anglophone. |
| Type de lot (Appartement/Bureau/Commerce/Parking/Cave/Autre) | `UnitType` (`APARTMENT`/`OFFICE`/`COMMERCIAL`/`PARKING`/`STORAGE`/`OTHER`) | "Cave" (cellier/cave) → `STORAGE`. |
| Tantièmes                          | `Shares`                       | Quote-part des charges communes attachée au lot lui-même — à ne pas confondre avec `OwnershipShare`. |
| Copropriétaire / rattachement copropriétaire-lot (`ProprieteLot`) | `UnitOwnership` | Pivot Contact ↔ Unit. |
| Part de propriété (`PartPropriete`)| `OwnershipShare`                | % de détention du lot par un copropriétaire donné (distinct de `Shares`). |
| Statut d'occupation (RG-LOT-01)    | `OwnershipStatus` (`SOLD` / `UNSOLD_DEVELOPER`) | Calculé à la lecture, jamais stocké. |
| Membre du syndic (`MembreSyndic`)  | `BoardMember`                   | Vocabulaire HOA (Homeowners Association) anglophone. |
| Rôle de gestion (`RoleGestion`)    | `BoardRole` (`PRESIDENT`/`TREASURER`/`SECRETARY`/`MEMBER`/`VOLUNTEER_MANAGER`) | "Syndic bénévole" → `VOLUNTEER_MANAGER`. |
| nom                                 | `name`                          | |
| adresse                             | `address`                       | |
| nombre d'étages                     | `floorCount`                    | |
| numéro de lot                       | `unitNumber`                    | |
| premier immeuble (création composite)| `firstBuilding...` (ex. `firstBuildingName`, `firstBuildingFloorCount`) | |

## Modules déjà en anglais (aucun changement)

`contact`, `user`, `auth`, `shared` ont été conçus directement en anglais et
ne nécessitent pas de renommage : `Contact`, `User`, `RefreshToken`,
`VerificationToken`, etc.

## Comment utiliser cette table

- Avant d'implémenter une nouvelle SFD, traduire chaque terme métier ici
  s'il n'y est pas déjà, en vérifiant qu'il ne collisionne pas avec un
  terme déjà utilisé ailleurs dans le code pour un concept différent.
- Un terme FR peut apparaître dans plusieurs SFD avec des sens différents :
  vérifier le contexte avant de réutiliser un nom EN existant.
