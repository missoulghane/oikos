import { Link } from 'react-router-dom';
import { useMyInstallments } from '@/features/property-ownership/installments/hooks/useMyInstallments';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function MyInstallmentsPage() {
  const installments = useMyInstallments();
  const units = useMyUnits();

  const isLoading = installments.isLoading || units.isLoading;
  const error = installments.error ?? units.error;

  const unitsById = new Map((units.data ?? []).map((unit) => [unit.unitId, unit]));

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Mes échéances</h1>

      <Card className="flex flex-col gap-2">
        {isLoading && <Loader label="Chargement de vos échéances…" />}
        {error && <Alert message={getErrorMessage(error)} />}
        {installments.data && installments.data.length === 0 && (
          <EmptyState title="Aucune échéance">Vous n'avez aucune échéance pour le moment.</EmptyState>
        )}
        {installments.data && installments.data.length > 0 && (
          <ul className="flex flex-col divide-y divide-gray-100 dark:divide-gray-800">
            {installments.data
              .slice()
              .sort((a, b) => new Date(b.dueDate).getTime() - new Date(a.dueDate).getTime())
              .map((installment) => {
                const unit = unitsById.get(installment.unitId);
                return (
                  <li key={installment.id} className="flex items-center justify-between py-2 text-sm">
                    <div>
                      {unit && (
                        <Link
                          to={`/property-ownership/units/${unit.propertyId}/${unit.unitId}`}
                          className="text-gray-500 dark:text-gray-400 hover:underline"
                        >
                          {unit.propertyName} — {unit.buildingName} — Lot {unit.unitNumber}
                        </Link>
                      )}
                      <p className="text-gray-700 dark:text-gray-300">
                        Échéance du {new Date(installment.dueDate).toLocaleDateString('fr-FR')} —{' '}
                        {installment.amount.toLocaleString('fr-FR')} MAD
                        {installment.status === 'PARTIALLY_SETTLED' &&
                          ` (reste ${installment.outstandingAmount.toLocaleString('fr-FR')} MAD)`}
                      </p>
                    </div>
                    <Badge color={INSTALLMENT_STATUS_BADGE_COLORS[installment.status]}>
                      {INSTALLMENT_STATUS_LABELS[installment.status]}
                    </Badge>
                  </li>
                );
              })}
          </ul>
        )}
      </Card>
    </div>
  );
}
