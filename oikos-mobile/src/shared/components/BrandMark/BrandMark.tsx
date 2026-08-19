import { Image, StyleSheet } from 'react-native';

interface BrandMarkProps {
  /** Côté du carré, en points. 64 par défaut : la taille des écrans d'accueil. */
  size?: number;
}

/**
 * Le symbole Daba Syndic. Un PNG plutôt qu'un SVG : react-native-svg n'est pas
 * une dépendance du projet, et l'ajouter pour une seule image obligerait à
 * reconstruire le natif. Le fichier vient de brand/daba-syndic-mark.svg, rendu
 * à 512 px par brand/generate-app-icons.sh - large pour tenir sur les écrans
 * @3x, où 64 pt valent 192 px.
 */
export function BrandMark({ size = 64 }: BrandMarkProps) {
  return (
    <Image
      source={require('@/shared/assets/brand/logo-mark.png')}
      style={[styles.mark, { width: size, height: size, borderRadius: size / 4 }]}
      accessibilityLabel="Daba Syndic"
    />
  );
}

const styles = StyleSheet.create({
  mark: {
    alignSelf: 'center',
  },
});
