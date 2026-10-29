import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting, canManageProperties } from '@/features/identity/me';
import { useExercises } from '@/features/property-mngt/accounting/hooks/useExercises';
import { OpenExerciseForm } from '@/features/property-mngt/accounting/components/OpenExerciseForm';
import { CloseExerciseForm } from '@/features/property-mngt/accounting/components/CloseExerciseForm';
import {
  MAX_OPEN_EXERCISES,
  nextExerciseDefaults,
  oldestOpenExercise,
} from '@/features/property-mngt/accounting/utils/nextExercise';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { EXERCISE_STATUS_BADGE_COLORS, EXERCISE_STATUS_LABELS } from '@/features/property-mngt/accounting/constants/accountingLabels';
import type { AccountingExercise } from '@/features/property-mngt/accounting/types/accounting.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

function formatDate(isoDate: string): string {
  return new Date(isoDate).toLocaleDateString('fr-FR');
}

/**
 * Les exercices de la copropriété. Une écriture se rattache à l'exercice qui
 * couvre sa date : deux peuvent être ouverts à la fois, N en attente
 * d'approbation de ses comptes et N+1 déjà mouvementé. Les exercices se
 * suivent sans trou ni chevauchement, et se clôturent dans l'ordre.
 */
export function AccountingExerciseTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const exercises = useExercises(property.id);
  const [isOpeningNext, setIsOpeningNext] = useState(false);
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;
  // Clôturer l'année relève de qui administre la copropriété, pas de qui
  // tient ses comptes - même partage que côté API.
  const canAdminister = currentUser.data ? canManageProperties(currentUser.data) : false;

  const list = exercises.data ?? [];
  const last: AccountingExercise | undefined = list[list.length - 1];
  const closable = oldestOpenExercise(list);
  const openCount = list.filter((exercise) => exercise.status === 'OPEN').length;
  const canOpenNext = canWrite && last !== undefined && openCount < MAX_OPEN_EXERCISES;

  return (
    <Card className="flex flex-col gap-4">
      <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Exercices comptables</h2>

      {exercises.isLoading && <Loader label="Chargement des exercices…" />}

      {exercises.isError && <Alert message={getErrorMessage(exercises.error)} />}

      {exercises.isSuccess && list.length === 0 && (
        <>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Aucun exercice comptable pour cette copropriété. Toute écriture (appel de fonds, règlement, facture…) se
            rattache à l'exercice ouvert qui couvre sa date.
          </p>
          {canWrite && <OpenExerciseForm propertyId={property.id} />}
        </>
      )}

      {list.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-200 dark:divide-gray-800">
          {[...list].reverse().map((exercise) => (
            <li key={exercise.id} className="flex flex-col gap-1 py-3">
              <div className="flex items-center gap-3">
                <p className="text-sm font-medium text-gray-900 dark:text-white/90">{exercise.label}</p>
                <Badge color={EXERCISE_STATUS_BADGE_COLORS[exercise.status]}>
                  {EXERCISE_STATUS_LABELS[exercise.status]}
                </Badge>
              </div>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Du {formatDate(exercise.startDate)} au {formatDate(exercise.endDate)}
                {exercise.comment && ` · ${exercise.comment}`}
              </p>
              {canWrite && canAdminister && closable?.id === exercise.id && (
                <div className="pt-2">
                  <CloseExerciseForm propertyId={property.id} exerciseId={exercise.id} exerciseLabel={exercise.label} />
                </div>
              )}
            </li>
          ))}
        </ul>
      )}

      {openCount > 1 && (
        <p className="text-sm text-gray-500 dark:text-gray-400">
          Deux exercices sont ouverts : chaque écriture rejoint celui qui couvre sa date. Le plus ancien se clôture en
          premier.
        </p>
      )}

      {canOpenNext && last && (
        <div className="flex flex-col gap-3 border-t border-gray-200 pt-4 dark:border-gray-800">
          {isOpeningNext ? (
            <>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Le nouvel exercice commence le lendemain de la fin du précédent ({formatDate(last.endDate)}).
              </p>
              <OpenExerciseForm
                propertyId={property.id}
                defaultValues={nextExerciseDefaults(last)}
                onOpened={() => setIsOpeningNext(false)}
              />
              <Button type="button" variant="secondary" className="w-fit" onClick={() => setIsOpeningNext(false)}>
                Annuler
              </Button>
            </>
          ) : (
            <div className="flex flex-col gap-1">
              <Button type="button" variant="secondary" className="w-fit" onClick={() => setIsOpeningNext(true)}>
                Ouvrir l'exercice suivant
              </Button>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Possible sans attendre la clôture de l'exercice en cours, pour appeler les fonds de la nouvelle année.
              </p>
            </div>
          )}
        </div>
      )}
    </Card>
  );
}
