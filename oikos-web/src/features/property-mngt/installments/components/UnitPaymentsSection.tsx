import { Link } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useUnitPayments } from '@/features/property-mngt/installments/hooks/useUnitPayments';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

interface UnitPaymentsSectionProps {
  propertyId: string;
  unitId: string;
}

export function UnitPaymentsSection({ propertyId, unitId }: UnitPaymentsSectionProps) {
  const currentUser = useCurrentUser();
  const payments = useUnitPayments(unitId);
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, propertyId) : false;

  return (
    <div className="flex flex-col gap-4">
      {payments.isLoading && <Loader label="Chargement des paiements…" />}
      {payments.isError && <Alert message={getErrorMessage(payments.error)} />}
      {payments.data && payments.data.length === 0 && <EmptyState title="Aucun paiement pour le moment" />}
      {payments.data && payments.data.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-100">
          {payments.data.map((payment) => (
            <li key={payment.id} className="flex items-center justify-between py-2 text-sm">
              <p className="text-gray-700">
                {new Date(payment.valueDate).toLocaleDateString('fr-FR')} — {payment.amount.toLocaleString('fr-FR')} MAD
                {' · '}
                {PAYMENT_MODE_LABELS[payment.mode]}
              </p>
            </li>
          ))}
        </ul>
      )}

      {canWrite && (
        <div className="border-t border-gray-200 pt-4">
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
