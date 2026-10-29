import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useRegularizeUnitAdvance } from '@/features/property-mngt/installments/hooks/useRegularizeUnitAdvance';

interface RegularizeUnitAdvanceButtonProps {
  propertyId: string;
  unitId: string;
}

/**
 * L'imputation de l'avance se fait d'elle-même - après chaque règlement, après
 * chaque appel, et chaque nuit. Ce bouton la relance à la main, typiquement
 * quand l'une de ces tentatives a échoué (exercice pas encore ouvert…).
 */
export function RegularizeUnitAdvanceButton({ propertyId, unitId }: RegularizeUnitAdvanceButtonProps) {
  const { mutate, isPending, isSuccess, data, error, reset } = useRegularizeUnitAdvance(propertyId, unitId);

  return (
    <div className="flex flex-col gap-2">
      {isSuccess && data && (
        <Alert
          variant="success"
          message={`${data.amountApplied.toLocaleString('fr-FR')} MAD d'avance imputés sur ${data.allocations.length} échéance(s).`}
        />
      )}
      {error && <Alert message={getErrorMessage(error)} />}
      <Button
        type="button"
        variant="secondary"
        className="w-fit"
        isLoading={isPending}
        onClick={() => {
          reset();
          mutate();
        }}
      >
        Imputer l'avance du lot
      </Button>
    </div>
  );
}
