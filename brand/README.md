# Marque Daba Syndic

Le symbole : un D dont le contre-poinçon abrite quatre lots, dont un allumé.
Le D porte le nom, les carrés portent le métier — une copropriété est un
ensemble de lots, pas un immeuble vu de la rue.

| Fichier | Usage |
| --- | --- |
| `daba-syndic-mark.svg` | Symbole seul, tuile arrondie. Favicon, avatar, en-tête d'app. |
| `daba-syndic-mark-square.svg` | Symbole pleine page, sans arrondi. Source des icônes iOS/Android, qui appliquent leur propre masque. |
| `daba-syndic-mark-mono.svg` | Une seule couleur (`currentColor`). Impression noir et blanc, tampon, gravure. |
| `daba-syndic-lockup.svg` | Verrou horizontal symbole + nom. Signature d'email, présentation, papier en-tête. |

## Couleurs

| Rôle | Hex | Jeton de l'app |
| --- | --- | --- |
| Indigo | `#465FFF` | `brand-500` |
| Indigo profond | `#2A31D8` | entre `brand-600` et `brand-700` |
| Nuit | `#161950` | `brand-950` |
| Ambre | `#FDB022` | accent, réservé au lot allumé |

## Règles

- Zone de protection : la moitié de la hauteur du symbole sur les quatre côtés.
- Taille minimale du symbole : 16 px. En dessous, les fenêtres se referment —
  utiliser un aplat indigo ou le seul D.
- Sur fond indigo, le symbole perd sa tuile : le D et les fenêtres passent en
  blanc, l'ambre reste ambre.
- Le nom s'écrit « Daba Syndic », jamais « DabaSyndic » ni « DABA SYNDIC ».
  « Daba » en gras, « Syndic » en régulier : c'est le seul contraste du verrou.
- Ne pas recolorer le symbole hors de la palette ci-dessus, ne pas l'étirer,
  ne pas lui ajouter d'ombre portée.

## Régénérer les icônes d'application

Les PNG de `oikos-mobile/assets/` et le `favicon.svg` de `oikos-web/public/`
dérivent de ces SVG. Le script `generate-app-icons.sh` les reconstruit
(macOS : il s'appuie sur `qlmanage` et `sips`, tous deux fournis par le
système, donc aucune dépendance à installer).
