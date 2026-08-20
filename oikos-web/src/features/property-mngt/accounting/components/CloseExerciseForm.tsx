import { useState } from 'react';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useCloseExercise } from '@/features/property-mngt/accounting/hooks/useCloseExercise';

/**
 * La clôture annuelle. Deux temps volontairement : le bouton demande d'abord
 * confirmation, parce que le geste ne se défait pas - il solde les comptes de
 * charges et de produits, arrête le résultat et scelle l'exercice.
 */
export function CloseExerciseForm({ propertyId, exerciseLabel }: { propertyId: string; exerciseLabel: string }) {
  const [isConfirming, setIsConfirming] = useState(false);
  const { mutate, isPending, isSuccess, data, error } = useCloseExercise(propertyId);

  if (isSuccess && data) {
    const result = data.netResult;
    return (
      <Alert
        variant="success"
        message={`Exercice ${data.exercise.label} clôturé. Résultat : ${result.toLocaleString('fr-FR')} MAD ${
          result >= 0 ? '(excédent)' : '(déficit)'
        }.`}
      />
    );
  }

  return (
    <div className="flex flex-col gap-3">
      {error && <Alert message={getErrorMessage(error)} />}
      {isConfirming ? (
        <>
          <Alert
            variant="warning"
            message={`Clôturer l'exercice ${exerciseLabel} soldera les comptes de charges et de produits, arrêtera le résultat et scellera l'exercice. Ce geste ne se défait pas.`}
          />
          <div className="flex flex-wrap gap-2">
            <Button type="button" isLoading={isPending} onClick={() => mutate()}>
              Confirmer la clôture
            </Button>
            <Button type="button" variant="secondary" onClick={() => setIsConfirming(false)}>
              Annuler
            </Button>
          </div>
        </>
      ) : (
        <div className="flex flex-col gap-1">
          <Button type="button" variant="secondary" className="w-fit" onClick={() => setIsConfirming(true)}>
            Clôturer l'exercice
          </Button>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Possible une fois tous les mois de l'exercice clôturés.
          </p>
        </div>
      )}
    </div>
  );
}
