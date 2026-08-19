#!/usr/bin/env bash
# Régénère les icônes d'application à partir des SVG de ce dossier.
#
# macOS uniquement, et volontairement : qlmanage (rendu SVG) et sips
# (redimensionnement) sont livrés avec le système, là où rsvg/ImageMagick
# demanderaient une installation à chaque poste. Les PNG produits sont commités,
# personne n'a donc à lancer ce script pour construire l'app - seulement pour
# faire évoluer le symbole.
#
#   ./brand/generate-app-icons.sh
set -euo pipefail

brand_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_dir="$(dirname "$brand_dir")"
assets_dir="$repo_dir/oikos-mobile/assets"
tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

# Le symbole, sans tuile : les deux plateformes appliquent leur propre masque,
# et Android recadre en plus l'avant-plan à sa zone de sécurité.
glyph() {
  local color="$1" accent="$2"
  cat <<GLYPH
  <g transform="translate(-1,0)">
    <path d="M22 48V16h6.5c9.7 0 15.5 6.2 15.5 16S38.2 48 28.5 48H22z" fill="none" stroke="$color" stroke-width="5" stroke-linejoin="round"/>
    <rect x="27.4" y="27" width="4.2" height="4.2" rx="1.1" fill="$color"/>
    <rect x="33.6" y="27" width="4.2" height="4.2" rx="1.1" fill="$color"/>
    <rect x="27.4" y="33.8" width="4.2" height="4.2" rx="1.1" fill="$color"/>
    <rect x="33.6" y="33.8" width="4.2" height="4.2" rx="1.1" fill="$accent"/>
  </g>
GLYPH
}

gradient() {
  cat <<'GRAD'
  <defs>
    <linearGradient id="dsGrad" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#465fff"/>
      <stop offset="1" stop-color="#2a31d8"/>
    </linearGradient>
  </defs>
GRAD
}

# render <fichier.svg> <taille> <destination.png>
render() {
  local svg="$1" size="$2" out="$3"
  qlmanage -t -s "$size" -o "$tmp_dir" "$svg" >/dev/null 2>&1
  mv "$tmp_dir/$(basename "$svg").png" "$out"
  echo "  $(basename "$out")  ${size}px"
}

echo "Génération des icônes Daba Syndic"

# Icône iOS / store : pleine page, aucun arrondi, aucune transparence.
render "$brand_dir/daba-syndic-mark-square.svg" 1024 "$assets_dir/icon.png"

# Écran de lancement : le symbole sur fond transparent, à 50% de la toile -
# expo-splash-screen pose lui-même la couleur de fond derrière.
{
  echo '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 128 128" width="1024" height="1024">'
  gradient
  echo '  <g transform="translate(32,32)">'
  echo '    <rect width="64" height="64" rx="16" fill="url(#dsGrad)"/>'
  glyph "#fff" "#fdb022"
  echo '  </g>'
  echo '</svg>'
} > "$tmp_dir/splash.svg"
render "$tmp_dir/splash.svg" 1024 "$assets_dir/splash-icon.png"

# Android, avant-plan : le symbole seul, cantonné à la zone de sécurité (le
# lanceur recadre et anime les 108x108 dp, seuls les 72x72 centraux survivent).
{
  echo '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="512" height="512">'
  echo '  <g transform="translate(23.5,23.5) scale(0.95)">'
  glyph "#fff" "#fdb022"
  echo '  </g>'
  echo '</svg>'
} > "$tmp_dir/android-foreground.svg"
render "$tmp_dir/android-foreground.svg" 512 "$assets_dir/android-icon-foreground.png"

# Android, arrière-plan : l'aplat dégradé, sans forme - c'est le lanceur qui
# découpe la silhouette (cercle, écusson, goutte selon le constructeur).
{
  echo '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="512" height="512">'
  gradient
  echo '  <rect width="108" height="108" fill="url(#dsGrad)"/>'
  echo '</svg>'
} > "$tmp_dir/android-background.svg"
render "$tmp_dir/android-background.svg" 512 "$assets_dir/android-icon-background.png"

# Android, monochrome (thème dynamique) : une seule couleur, le système
# retient la silhouette et la reteinte. L'ambre disparaît donc ici.
{
  echo '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="432" height="432">'
  echo '  <g transform="translate(23.5,23.5) scale(0.95)">'
  glyph "#000" "#000"
  echo '  </g>'
  echo '</svg>'
} > "$tmp_dir/android-monochrome.svg"
render "$tmp_dir/android-monochrome.svg" 432 "$assets_dir/android-icon-monochrome.png"

# Favicon du build web d'Expo.
render "$brand_dir/daba-syndic-mark.svg" 48 "$assets_dir/favicon.png"

# Symbole affiché dans l'app elle-même (écran de connexion, invitation). Il
# vit sous src/shared/assets, comme les avatars : assets/ à la racine ne sert
# qu'aux icônes déclarées dans app.json.
render "$brand_dir/daba-syndic-mark.svg" 512 "$repo_dir/oikos-mobile/src/shared/assets/brand/logo-mark.png"

# Le favicon du web est servi en SVG : une copie suffit, aucun rendu.
cp "$brand_dir/daba-syndic-mark.svg" "$repo_dir/oikos-web/public/favicon.svg"
echo "  favicon.svg  (copie)"

echo "Terminé."
