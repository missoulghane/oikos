import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useUnitInstallments } from '@/features/property-mngt/installments/hooks/useUnitInstallments';
import { useRegularizeUnitInstallments } from '@/features/property-mngt/installments/hooks/useRegularizeUnitInstallments';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

interface UnitInstallmentsSectionProps {
  propertyId: string;
  unitId: string;
}

export function UnitInstallmentsSection({ propertyId, unitId }: UnitInstallmentsSectionProps) {
  const currentUser = useCurrentUser();
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, propertyId) : false;
  const { data, isLoading, isError, error } = useUnitInstallments(unitId);
  const regularize = useRegularizeUnitInstallments(propertyId, unitId);

  return (
    <div className="flex flex-col gap-3">
      {canWrite && (
        <div className="flex flex-col gap-2">
          <Button
            type="button"
            variant="secondary"
            className="self-start"
            isLoading={regularize.isPending}
            onClick={() => regularize.mutate()}
          >
            Régulariser les avances
          </Button>
          {regularize.isError && <Alert message={getErrorMessage(regularize.error)} />}
          {regularize.isSuccess && (
            <Alert
              variant="success"
              message={`${regularize.data.amountApplied.toLocaleString('fr-FR')} MAD imputé(s) sur ${regularize.data.allocations.length} échéance(s).`}
            />
          )}
        </div>
      )}

      {isLoading && <Loader label="Chargement des échéances…" />}
      {isError && <Alert message={getErrorMessage(error)} />}
      {data && data.length === 0 && <p className="text-sm text-gray-400">Aucune échéance pour le moment.</p>}
      {data && data.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-100">
          {data.map((installment) => (
            <li key={installment.id} className="flex items-center justify-between py-2 text-sm">
              <div>
                <p className="text-gray-700">
                  Échéance du {new Date(installment.dueDate).toLocaleDateString('fr-FR')} — {installment.amount} MAD
                  {installment.status === 'PARTIALLY_SETTLED' && ` (reste ${installment.outstandingAmount} MAD)`}
                </p>
              </div>
              <Badge color={INSTALLMENT_STATUS_BADGE_COLORS[installment.status]}>
                {INSTALLMENT_STATUS_LABELS[installment.status]}
              </Badge>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
