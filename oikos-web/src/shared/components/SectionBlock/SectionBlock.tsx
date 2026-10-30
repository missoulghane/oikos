import type { ReactNode } from 'react';
import { Card } from '@/shared/components/Card/Card';

interface SectionBlockProps {
  title: string;
  /** Le bouton d'action du bloc, en haut à droite, sur la ligne du titre. */
  action?: ReactNode;
  children: ReactNode;
}

/**
 * Un bloc d'une page de détail : le titre et son action sur une même ligne, un
 * trait, puis le contenu. Les blocs d'une même page (Propriétaires, Échéances,
 * Paiements d'un lot) se lisent ainsi tous de la même façon - l'action toujours
 * au même endroit, le trait toujours sous le titre.
 */
export function SectionBlock({ title, action, children }: SectionBlockProps) {
  return (
    <Card className="flex flex-col gap-3">
      <div className="flex min-h-11 items-center justify-between gap-3">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">{title}</h2>
        {action && <div className="shrink-0">{action}</div>}
      </div>
      <div className="flex flex-col gap-3 border-t border-gray-100 pt-3 dark:border-gray-800">{children}</div>
    </Card>
  );
}
