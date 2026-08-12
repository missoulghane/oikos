import { Link } from 'react-router-dom';
import { useMyPayments } from '@/features/property-ownership/payments/hooks/useMyPayments';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function MyPaymentsPage() {
  const payments = useMyPayments();
  const units = useMyUnits();

  const isLoading = payments.isLoading || units.isLoading;
  const error = payments.error ?? units.error;

  const unitsById = new Map((units.data ?? []).map((unit) => [unit.unitId, unit]));

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Mes paiements</h1>

      <Card className="flex flex-col gap-2">
        {isLoading && <Loader label="Chargement de vos paiements…" />}
        {error && <Alert message={getErrorMessage(error)} />}
        {payments.data && payments.data.length === 0 && (
          <EmptyState title="Aucun paiement">Vous n'avez aucun paiement pour le moment.</EmptyState>
        )}
        {payments.data && payments.data.length > 0 && (
          <ul className="flex flex-col divide-y divide-gray-100 dark:divide-gray-800">
            {payments.data
              .slice()
              .sort((a, b) => new Date(b.valueDate).getTime() - new Date(a.valueDate).getTime())
              .map((payment) => {
                const unit = unitsById.get(payment.unitId);
                return (
                  <li key={payment.id} className="flex items-center justify-between py-2 text-sm">
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
                        Paiement du {new Date(payment.valueDate).toLocaleDateString('fr-FR')} —{' '}
                        {payment.amount.toLocaleString('fr-FR')} MAD
                      </p>
                    </div>
                    <Badge color="light">{PAYMENT_MODE_LABELS[payment.mode]}</Badge>
                  </li>
                );
              })}
          </ul>
        )}
      </Card>
    </div>
  );
}
