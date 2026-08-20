import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCurrentUser, boardPropertyIds, hasCopro } from '@/features/identity/me';
import type { CurrentUser, PropertyRoleName } from '@/features/identity/me';
import { useEffectiveSpace } from '@/shared/hooks/useEffectiveSpace';
import { useMandateProperties } from '@/shared/hooks/useMandateProperties';
import { Dropdown } from '@/shared/components/Dropdown/Dropdown';

interface SpaceEntry {
  key: string;
  label: string;
  sub: string;
  isActive: boolean;
  to: string;
}

const BOARD_ROLE_LABELS: Partial<Record<PropertyRoleName, string>> = {
  PROPERTY_BOARD_ADMIN: 'Administrateur du conseil',
  PROPERTY_BOARD_MEMBER: 'Membre du conseil',
};

function boardRoleLabel(user: CurrentUser, propertyId: string): string {
  const roles = user.roleByProperty[propertyId] ?? [];
  return roles.map((role) => BOARD_ROLE_LABELS[role]).find((label): label is string => Boolean(label)) ?? 'Mandat de syndic';
}

/**
 * Bascule entre l'espace copropriétaire (consolidé, transversal) et les
 * mandats de bureau de syndic (chacun scopé à une résidence). Rien si un
 * seul espace existe - un choix à une seule option n'est pas un choix -
 * sinon toujours la même liste déroulante dès qu'une bascule est possible,
 * qu'il y ait 2 espaces ou davantage : une forme unique et reconnaissable
 * plutôt qu'une forme qui change selon le nombre d'espaces.
 *
 * Volontairement limité aux mandats de bureau (PROPERTY_BOARD_*) : un
 * compte d'agence gérant professionnellement des dizaines de copropriétés
 * (PROPERTY_MANAGER_*) garde son parcours actuel (liste "Mes copropriétés"),
 * le sélecteur ne modélise pas ce cas dans le prototype cible.
 *
 * Le déclencheur tient sur une seule ligne (texte tronqué si besoin) pour
 * rester ergonomique aussi bien sur mobile que sur desktop ; le libellé
 * "Espace actif" reste disponible pour les lecteurs d'écran via aria-label.
 */
export function SpaceSwitcher() {
  const [isOpen, setIsOpen] = useState(false);
  const navigate = useNavigate();
  const currentUser = useCurrentUser();
  const effectiveSpace = useEffectiveSpace();
  const user = currentUser.data;
  const mandateIds = user ? boardPropertyIds(user) : [];
  const mandateProperties = useMandateProperties(mandateIds);

  if (!user || currentUser.isLoading) {
    return null;
  }

  const entries: SpaceEntry[] = [];
  if (hasCopro(user)) {
    entries.push({
      key: 'owner',
      label: 'Mon espace personnel',
      sub: 'Tous mes biens',
      isActive: effectiveSpace.kind === 'owner',
      to: '/dashboard?space=owner',
    });
  }
  for (const propertyId of mandateIds) {
    const property = mandateProperties.byId.get(propertyId);
    entries.push({
      key: propertyId,
      label: property ? `Espace ${property.name}` : 'Espace de syndic',
      sub: boardRoleLabel(user, propertyId),
      isActive: effectiveSpace.kind === 'board' && effectiveSpace.propertyId === propertyId,
      to: `/dashboard?space=board&propertyId=${propertyId}`,
    });
  }

  // A single available space is not a choice - nothing is shown, exactly as
  // for the large majority of accounts (case 1/3/5, or manager-only).
  if (entries.length <= 1) {
    return null;
  }

  function go(to: string) {
    setIsOpen(false);
    navigate(to);
  }

  const current = entries.find((entry) => entry.isActive) ?? entries[0];

  return (
    <div className="relative flex w-full justify-center px-3 py-2 lg:w-auto lg:justify-start lg:py-0">
      <button
        type="button"
        onClick={() => setIsOpen((value) => !value)}
        title={current.label}
        aria-label={`Espace actif : ${current.label}`}
        className="dropdown-toggle inline-flex min-w-0 max-w-full items-center gap-2 rounded-lg border border-gray-200 px-3 py-1.5 text-sm dark:border-gray-800"
      >
        <span className="truncate font-medium text-gray-800 dark:text-white/90">{current.label}</span>
        <svg
          className={`shrink-0 stroke-gray-500 dark:stroke-gray-400 transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`}
          width="14"
          height="16"
          viewBox="0 0 18 20"
          fill="none"
        >
          <path
            d="M4.3125 8.65625L9 13.3437L13.6875 8.65625"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </button>
      <Dropdown isOpen={isOpen} onClose={() => setIsOpen(false)} className="left-0 flex w-max max-w-[90vw] min-w-[280px] flex-col p-2">
        <p className="px-2 pb-2 pt-1 text-xs text-gray-500 dark:text-gray-400">
          Un seul sélecteur pour la résidence et le rôle. Vos biens restent consolidés dans votre espace personnel.
        </p>
        {entries.map((entry) => (
          <button
            key={entry.key}
            type="button"
            aria-current={entry.isActive}
            onClick={() => go(entry.to)}
            title={entry.label}
            className={`flex w-full items-center justify-between gap-2 rounded-lg px-3 py-2 text-left text-sm ${
              entry.isActive
                ? 'bg-brand-50 text-brand-600 dark:bg-brand-500/[0.12] dark:text-brand-400'
                : 'text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/[0.05]'
            }`}
          >
            <span className="truncate font-medium">{entry.label}</span>
            <span className="shrink-0 text-xs text-gray-500 dark:text-gray-400">{entry.sub}</span>
          </button>
        ))}
      </Dropdown>
    </div>
  );
}
