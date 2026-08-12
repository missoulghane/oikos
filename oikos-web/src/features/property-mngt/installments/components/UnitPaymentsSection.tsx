import { Link } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useUnitPayments } from '@/features/property-mngt/installments/hooks/useUnitPayments';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

interface UnitPaymentsSectionProps {
  propertyId: string;
  unitId: string;
  /** Hides the record-payment action for callers without accounting-management rights (e.g. the owner's own read-only lot page). */
  canManage?: boolean;
}

export function UnitPaymentsSection({ propertyId, unitId, canManage = true }: UnitPaymentsSectionProps) {
  const currentUser = useCurrentUser();
  const payments = useUnitPayments(unitId);
  const canWrite = canManage && currentUser.data ? canWriteAccounting(currentUser.data, propertyId) : false;

  return (
    <div className="flex flex-col gap-4">
      {payments.isLoading && <Loader label="Chargement des paiements…" />}
      {payments.isError && <Alert message={getErrorMessage(payments.error)} />}
      {payments.data && payments.data.length === 0 && <EmptyState title="Aucun paiement pour le moment" />}
      {payments.data && payments.data.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-100 dark:divide-gray-800">
          {payments.data.map((payment) => (
            <li key={payment.id} className="flex items-center justify-between py-2 text-sm">
              <p className="text-gray-700 dark:text-gray-300">
                Paiement du {new Date(payment.valueDate).toLocaleDateString('fr-FR')} —{' '}
                {payment.amount.toLocaleString('fr-FR')} MAD
              </p>
              <Badge color="light">{PAYMENT_MODE_LABELS[payment.mode]}</Badge>
            </li>
          ))}
        </ul>
      )}

      {canWrite && (
        <div className="border-t border-gray-200 dark:border-gray-800 pt-4">
          <Link
            to={`/property-mngt/properties/${propertyId}/units/${unitId}/payment`}
            className="inline-flex min-h-11 items-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
          >
            Enregistrer un paiement
          </Link>
        </div>
      )}
    </div>
  );
}
