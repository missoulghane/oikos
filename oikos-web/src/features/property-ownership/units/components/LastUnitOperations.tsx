import { Link } from 'react-router-dom';
import { useUnitInstallments } from '@/features/property-mngt/installments/hooks/useUnitInstallments';
import { useUnitPayments } from '@/features/property-mngt/installments/hooks/useUnitPayments';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import { Card } from '@/shared/components/Card/Card';
import { Badge } from '@/shared/components/Badge/Badge';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

/** Most recent first, capped - a lot's whole history belongs on Mes échéances / Mes paiements. */
const TOP_COUNT = 5;

/**
 * Owner-side, unlike property-mngt's UnitInstallmentsSection/UnitPaymentsSection:
 * rows here are links into the owner's own detail screens, which the shared
 * (manager) sections deliberately have no notion of.
 *
 * Both hooks call GET /units/{unitId}/… - this lot's rows only, filtered
 * server-side and cached per unitId, never the owner's whole account.
 */
export function LastUnitInstallments({ unitId }: { unitId: string }) {
  const installments = useUnitInstallments(unitId);

  const latest = (installments.data ?? [])
    .slice()
    .sort((a, b) => b.dueDate.localeCompare(a.dueDate))
    .slice(0, TOP_COUNT);

  return (
    <Card className="flex flex-col gap-3">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Dernières échéances</h2>
        <Link to="/property-ownership/installments" className="text-sm text-brand-500 hover:underline dark:text-brand-400">
          Tout voir
        </Link>
      </div>

      {installments.isLoading && <Loader label="Chargement des échéances…" />}
      {installments.isError && <Alert message={getErrorMessage(installments.error)} />}
      {installments.data && latest.length === 0 && <EmptyState title="Aucune échéance pour le moment" />}

      {latest.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-100 dark:divide-gray-800">
          {latest.map((installment) => (
            <li key={installment.id}>
              <Link
                to={`/property-ownership/installments/${installment.id}`}
                className="flex items-center justify-between gap-3 py-2 text-sm hover:underline"
              >
                <span className="text-gray-700 dark:text-gray-300">
                  Échéance du {new Date(installment.dueDate).toLocaleDateString('fr-FR')} —{' '}
                  {installment.amount.toLocaleString('fr-FR')} MAD
                </span>
                <Badge color={INSTALLMENT_STATUS_BADGE_COLORS[installment.status]}>
                  {INSTALLMENT_STATUS_LABELS[installment.status]}
                </Badge>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </Card>
  );
}

export function LastUnitPayments({ unitId }: { unitId: string }) {
  const payments = useUnitPayments(unitId);

  const latest = (payments.data ?? [])
    .slice()
    .sort((a, b) => b.valueDate.localeCompare(a.valueDate))
    .slice(0, TOP_COUNT);

  return (
    <Card className="flex flex-col gap-3">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Derniers paiements</h2>
        <Link to="/property-ownership/payments" className="text-sm text-brand-500 hover:underline dark:text-brand-400">
          Tout voir
        </Link>
      </div>

      {payments.isLoading && <Loader label="Chargement des paiements…" />}
      {payments.isError && <Alert message={getErrorMessage(payments.error)} />}
      {payments.data && latest.length === 0 && <EmptyState title="Aucun paiement pour le moment" />}

      {latest.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-100 dark:divide-gray-800">
          {latest.map((payment) => (
            <li key={payment.id}>
              <Link
                to={`/property-ownership/payments/${payment.id}`}
                className="flex items-center justify-between gap-3 py-2 text-sm hover:underline"
              >
                <span className="text-gray-700 dark:text-gray-300">
                  Paiement du {new Date(payment.valueDate).toLocaleDateString('fr-FR')} —{' '}
                  {payment.amount.toLocaleString('fr-FR')} MAD
                </span>
                <Badge color="light">{PAYMENT_MODE_LABELS[payment.mode]}</Badge>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </Card>
  );
}
