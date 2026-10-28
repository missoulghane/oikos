import type { PropsWithChildren } from 'react';
import { Link } from 'react-router-dom';
import { BrandLogo } from '@/shared/components/BrandLogo/BrandLogo';
import { Alert } from '@/shared/components/Alert/Alert';
import { LEGAL_DOCUMENTS_VERSION } from '@/features/legal/constants/legalEntity';

interface LegalDocumentProps {
  title: string;
  /** L'autre document, cité en pied de page : les CGU et les CGV se renvoient l'une à l'autre. */
  related: { label: string; to: string };
}

/**
 * Gabarit commun des pages juridiques : publiques, lisibles sans compte, et
 * ouvertes dans un nouvel onglet depuis l'inscription - d'où l'absence de bouton
 * « retour », qui ramènerait en arrière dans un onglet sans historique.
 */
export function LegalDocument({ title, related, children }: PropsWithChildren<LegalDocumentProps>) {
  return (
    <div className="min-h-screen bg-white px-4 py-10 dark:bg-gray-900">
      <article className="mx-auto flex max-w-3xl flex-col gap-6 text-gray-700 dark:text-gray-300">
        <Link to="/" className="self-start text-gray-900 dark:text-white/90">
          <BrandLogo size="md" wordmarkClassName="text-xl" />
        </Link>
        <header className="flex flex-col gap-1">
          <h1 className="text-2xl font-semibold text-gray-900 dark:text-white/90">{title}</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Version {LEGAL_DOCUMENTS_VERSION.label} - en vigueur au {LEGAL_DOCUMENTS_VERSION.date}
          </p>
        </header>
        <Alert
          variant="warning"
          message="Version provisoire : les informations entre crochets restent à compléter, et le document doit être validé par un conseil juridique avant la mise en production."
        />
        {children}
        <footer className="border-t border-gray-200 pt-4 text-sm dark:border-gray-800">
          Voir aussi :{' '}
          <Link to={related.to} className="font-medium text-brand-500 underline dark:text-brand-400">
            {related.label}
          </Link>
        </footer>
      </article>
    </div>
  );
}

interface LegalSectionProps {
  title: string;
}

export function LegalSection({ title, children }: PropsWithChildren<LegalSectionProps>) {
  return (
    <section className="flex flex-col gap-2 text-sm leading-6">
      <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">{title}</h2>
      {children}
    </section>
  );
}
