import { Link } from 'react-router-dom';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { useUnitInstallments } from '@/features/property-mngt/installments/hooks/useUnitInstallments';
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
  const { data, isLoading, isError, error } = useUnitInstallments(unitId);

  return (
    <div className="flex flex-col gap-3">
      {isLoading && <Loader label="Chargement des échéances…" />}
      {isError && <Alert message={getErrorMessage(error)} />}
      {data && data.length === 0 && <EmptyState title="Aucune échéance pour le moment" />}
      {data && data.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-100 dark:divide-gray-800">
          {data.map((installment) => (
            <li key={installment.id} className="flex items-center justify-between py-2 text-sm">
              <p className="text-gray-700 dark:text-gray-300">
                {/* Même page que depuis la liste « Échéances » du menu : le
                    détail de la ligne et les règlements du lot. */}
                <Link
                  to={`/property-mngt/properties/${propertyId}/installments/${installment.id}`}
                  className="font-medium text-brand-500 hover:underline dark:text-brand-400"
                >
                  Échéance du {new Date(installment.dueDate).toLocaleDateString('fr-FR')}
                </Link>{' '}
                — {installment.amount.toLocaleString('fr-FR')} MAD
                {installment.status === 'PARTIALLY_SETTLED' &&
                  ` (reste ${installment.outstandingAmount.toLocaleString('fr-FR')} MAD)`}
              </p>
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
