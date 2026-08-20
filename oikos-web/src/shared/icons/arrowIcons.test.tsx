import { describe, expect, it } from 'vitest';
import { render } from '@testing-library/react';
import { ArrowDownIcon, ArrowRightIcon, ArrowUpIcon, PlusIcon } from '@/shared/icons';

/**
 * Ces quatre icônes ne portaient aucune couleur sur leur tracé : elles
 * comptaient sur une classe Tailwind (`fill-current`) posée sur le <svg>
 * lui-même. Un appelant qui passe sa propre className la remplace, le tracé
 * retombe alors sur le `fill="none"` de la racine, et l'icône disparaît sans
 * qu'aucune erreur ne le dise - c'est ce qui est arrivé aux deux boutons de
 * saisie du tableau de bord.
 *
 * Deux d'entre elles portaient même `className=` dans un fichier .svg, un
 * attribut que le navigateur ignore : la classe n'avait jamais rien fait.
 */
describe('icônes de flèche', () => {
  it('peignent leur tracé même quand l’appelant passe une className', () => {
    const { container } = render(
      <>
        <ArrowDownIcon className="size-5" />
        <ArrowUpIcon className="size-5" />
        <ArrowRightIcon className="size-5" />
        <PlusIcon className="size-5" />
      </>,
    );

    const paths = [...container.querySelectorAll('path')];
    expect(paths).toHaveLength(4);
    for (const path of paths) {
      const fill = path.getAttribute('fill');
      const stroke = path.getAttribute('stroke');
      expect(fill === 'currentColor' || stroke === 'currentColor').toBe(true);
    }
  });
});
