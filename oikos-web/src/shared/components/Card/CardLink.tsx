import type { PropsWithChildren } from 'react';
import { Link } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';

interface CardLinkProps {
  to: string;
  className?: string;
}

/**
 * Une carte qui mène quelque part. Le lien porte l'ombre au survol, et deux
 * choses doivent lui coller pour que cette ombre épouse la carte : le même
 * arrondi, et la même hauteur.
 *
 * <p>La hauteur n'allait pas de soi. Dans une grille, l'élément s'étire à la
 * hauteur de la ligne, tandis que la carte à l'intérieur s'arrête à son
 * contenu : la carte la plus courte d'une paire voyait donc son ombre déborder
 * sous elle, exactement de l'écart entre les deux. Le {@code h-full} des deux
 * côtés les réaligne.
 *
 * <p>Un composant plutôt que trois fois les mêmes classes : c'est la répétition
 * qui avait laissé passer l'oubli initial.
 */
export function CardLink({ to, className = '', children }: PropsWithChildren<CardLinkProps>) {
  return (
    <Link to={to} className="block h-full rounded-2xl transition-shadow hover:shadow-theme-md">
      <Card className={`h-full ${className}`}>{children}</Card>
    </Link>
  );
}
