import { useId } from 'react';

/**
 * Le symbole Daba Syndic, en SVG inline plutôt qu'en <img> : il se colore avec
 * le thème (la tuile disparaît sur le panneau indigo de l'authentification) et
 * ne coûte pas une requête de plus au premier rendu, où il est visible.
 *
 * La source de vérité reste brand/daba-syndic-mark.svg à la racine du dépôt -
 * c'est elle qui produit le favicon et les icônes mobiles. Toute retouche du
 * dessin se fait là-bas, puis se recopie ici.
 */

const SIZES = {
  sm: 'size-7',
  md: 'size-9',
  lg: 'size-12',
} as const;

type Size = keyof typeof SIZES;

interface BrandMarkProps {
  size?: Size;
  /**
   * Sur fond indigo (panneau d'authentification), la tuile ferait un carré dans
   * un carré : le symbole passe alors en blanc détouré.
   */
  onDark?: boolean;
  className?: string;
}

export function BrandMark({ size = 'md', onDark = false, className = '' }: BrandMarkProps) {
  // Deux instances du logo cohabitent (en-tête et menu latéral) : un id de
  // dégradé en dur ferait pointer la seconde sur la définition de la première.
  const gradientId = useId();

  return (
    <svg viewBox="0 0 64 64" className={`${SIZES[size]} ${className}`} role="img" aria-label="Daba Syndic">
      <defs>
        <linearGradient id={gradientId} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#465fff" />
          <stop offset="1" stopColor="#2a31d8" />
        </linearGradient>
      </defs>
      <rect width="64" height="64" rx="16" fill={onDark ? 'rgba(255,255,255,0.12)' : `url(#${gradientId})`} />
      <g transform="translate(-1,0)">
        <path
          d="M22 48V16h6.5c9.7 0 15.5 6.2 15.5 16S38.2 48 28.5 48H22z"
          fill="none"
          stroke="#fff"
          strokeWidth="5"
          strokeLinejoin="round"
        />
        <rect x="27.4" y="27" width="4.2" height="4.2" rx="1.1" fill="#fff" />
        <rect x="33.6" y="27" width="4.2" height="4.2" rx="1.1" fill="#fff" />
        <rect x="27.4" y="33.8" width="4.2" height="4.2" rx="1.1" fill="#fff" />
        <rect x="33.6" y="33.8" width="4.2" height="4.2" rx="1.1" fill="#fdb022" />
      </g>
    </svg>
  );
}

interface BrandLogoProps extends BrandMarkProps {
  /** Taille du nom, indépendante de celle du symbole (verrou d'écran d'accueil). */
  wordmarkClassName?: string;
}

/**
 * Verrou complet : symbole + nom. « Daba » porte le poids, « Syndic »
 * qualifie - le contraste de graisse est le seul écart entre les deux mots, ils
 * gardent la même taille.
 */
export function BrandLogo({ size = 'md', onDark = false, className = '', wordmarkClassName = '' }: BrandLogoProps) {
  return (
    <span className={`inline-flex items-center gap-2.5 ${className}`}>
      <BrandMark size={size} onDark={onDark} />
      <span className={`whitespace-nowrap tracking-tight ${wordmarkClassName}`}>
        <span className="font-bold">Daba</span> <span className="font-normal opacity-70">Syndic</span>
      </span>
    </span>
  );
}
