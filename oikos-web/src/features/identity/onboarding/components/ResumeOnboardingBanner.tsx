import { Link } from 'react-router-dom';
import { useCurrentUser, boardPropertyId } from '@/features/identity/me';
import { useBuildings } from '@/features/property-mngt/properties/hooks/useBuildings';
import { stepPath } from '@/features/identity/onboarding/constants/steps';

/**
 * Un syndic bénévole dont la copropriété n'a aucun bâtiment n'a pas fini son
 * wizard (le compte et la copropriété naissent à l'étape 2, la structure à
 * l'étape 7). Plutôt qu'une redirection forcée - qui l'enfermerait dans le
 * tunnel alors que son espace est parfaitement utilisable - on lui propose de
 * reprendre là où il s'est arrêté.
 */
export function ResumeOnboardingBanner() {
  const currentUser = useCurrentUser();
  const propertyId = currentUser.data ? boardPropertyId(currentUser.data) : null;
  const buildings = useBuildings(propertyId ?? '');

  const isUnconfigured = Boolean(propertyId) && buildings.isSuccess && buildings.data.content.length === 0;
  if (!isUnconfigured) {
    return null;
  }

  return (
    <div className="mb-4 flex flex-col gap-3 rounded-2xl border border-brand-200 bg-brand-50 p-4 sm:flex-row sm:items-center sm:justify-between dark:border-brand-400/30 dark:bg-brand-500/[0.12]">
      <div>
        <p className="text-sm font-medium text-gray-900 dark:text-white/90">Votre configuration n'est pas terminée</p>
        <p className="text-sm text-gray-600 dark:text-gray-400">
          Ajoutez vos bâtiments et vos lots pour finaliser votre copropriété.
        </p>
      </div>
      <Link
        to={stepPath('dues-mode')}
        className="inline-flex min-h-11 shrink-0 items-center justify-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600"
      >
        Reprendre la configuration
      </Link>
    </div>
  );
}
