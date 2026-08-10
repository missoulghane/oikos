import { useOutletContext } from 'react-router-dom';
import { isAxiosError } from 'axios';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useOpenExercise } from '@/features/property-mngt/accounting/hooks/useOpenExercise';
import { OpenExerciseForm } from '@/features/property-mngt/accounting/components/OpenExerciseForm';
import { ClosePeriodForm } from '@/features/property-mngt/accounting/components/ClosePeriodForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { EXERCISE_STATUS_BADGE_COLORS, EXERCISE_STATUS_LABELS } from '@/features/property-mngt/accounting/constants/accountingLabels';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingExerciseTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const openExercise = useOpenExercise(property.id);
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;

  const hasNoOpenExercise = isAxiosError(openExercise.error) && openExercise.error.response?.status === 400;

  return (
    <Card className="flex flex-col gap-4">
      <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Exercice comptable</h2>

      {openExercise.isLoading && <Loader label="Chargement de l'exercice…" />}

      {openExercise.isError && !hasNoOpenExercise && <Alert message={getErrorMessage(openExercise.error)} />}

      {openExercise.isError && hasNoOpenExercise && (
        <>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Aucun exercice comptable ouvert pour cette copropriété. Toute écriture (appel de fonds, règlement,
            facture…) nécessite un exercice ouvert.
          </p>
          {canWrite && <OpenExerciseForm propertyId={property.id} />}
        </>
      )}

      {openExercise.data && (
        <>
          <div className="flex items-center gap-3">
            <p className="text-sm font-medium text-gray-900 dark:text-white/90">{openExercise.data.label}</p>
            <Badge color={EXERCISE_STATUS_BADGE_COLORS[openExercise.data.status]}>
              {EXERCISE_STATUS_LABELS[openExercise.data.status]}
            </Badge>
          </div>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Du {new Date(openExercise.data.startDate).toLocaleDateString('fr-FR')} au{' '}
            {new Date(openExercise.data.endDate).toLocaleDateString('fr-FR')}
            {openExercise.data.comment && ` · ${openExercise.data.comment}`}
          </p>

          {canWrite && (
            <div className="border-t border-gray-200 dark:border-gray-800 pt-4">
              <ClosePeriodForm propertyId={property.id} />
            </div>
          )}
        </>
      )}
    </Card>
  );
}
