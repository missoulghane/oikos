import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { useUnitInstallments } from '@/features/property-mngt/installments/hooks/useUnitInstallments';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function UnitInstallmentsSection({ unitId }: { unitId: string }) {
  const { data, isLoading, isError, error } = useUnitInstallments(unitId);

  if (isLoading) {
    return <Loader label="Chargement des échéances…" />;
  }

  if (isError) {
    return <Alert message={getErrorMessage(error)} />;
  }

  if (!data || data.length === 0) {
    return <p className="text-sm text-gray-400">Aucune échéance pour le moment.</p>;
  }

  return (
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
  );
}
