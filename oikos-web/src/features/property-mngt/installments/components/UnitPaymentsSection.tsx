import { Link } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useUnitPayments } from '@/features/property-mngt/installments/hooks/useUnitPayments';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import { useDownloadPaymentReceipt } from '@/features/property-mngt/installments/hooks/useDownloadPaymentReceipt';
import { Loader } from '@/shared/components/Loader/Loader';
import { SectionBlock } from '@/shared/components/SectionBlock/SectionBlock';
import { buttonClassName } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { isNotFound } from '@/shared/utils/isNotFound';

interface UnitPaymentsSectionProps {
  propertyId: string;
  unitId: string;
  /** Hides the record-payment action for callers without accounting-management rights (e.g. the owner's own read-only lot page). */
  canManage?: boolean;
  /** Title of the block - « Paiements » on the lot page, « Paiements du lot » on an installment's. */
  title: string;
}

export function UnitPaymentsSection({ propertyId, unitId, canManage = true, title }: UnitPaymentsSectionProps) {
  const currentUser = useCurrentUser();
  const payments = useUnitPayments(unitId);
  const downloadReceipt = useDownloadPaymentReceipt();
  const canWrite = canManage && currentUser.data ? canWriteAccounting(currentUser.data, propertyId) : false;

  // Un lien plutôt qu'un bouton : il mène à une page (clic du milieu, nouvel
  // onglet), mais il a l'allure des autres actions des blocs du lot.
  const recordPaymentLink = canWrite ? (
    <Link to={`/property-mngt/properties/${propertyId}/units/${unitId}/payment`} className={buttonClassName('secondary')}>
      Enregistrer un paiement
    </Link>
  ) : null;

  return (
    <SectionBlock title={title} action={recordPaymentLink}>
      {payments.isLoading && <Loader label="Chargement des paiements…" />}
      {payments.isError && <Alert message={getErrorMessage(payments.error)} />}
      {payments.data && payments.data.length === 0 && <EmptyState title="Aucun paiement pour le moment" />}
      {/* A payment recorded before receipts existed, or whose generation failed,
          has none - say so rather than leave the link failing silently. */}
      {downloadReceipt.isError && (
        <Alert
          message={
            isNotFound(downloadReceipt.error)
              ? "Aucun reçu n'est disponible pour ce paiement. Régénérez-le depuis l'API si besoin."
              : getErrorMessage(downloadReceipt.error)
          }
        />
      )}
      {payments.data && payments.data.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-100 dark:divide-gray-800">
          {payments.data.map((payment) => (
            <li key={payment.id} className="flex flex-col gap-1 py-2 text-sm sm:flex-row sm:items-center sm:justify-between">
              <p className="text-gray-700 dark:text-gray-300">
                Paiement du {new Date(payment.valueDate).toLocaleDateString('fr-FR')} —{' '}
                {payment.amount.toLocaleString('fr-FR')} MAD
              </p>
              <span className="flex items-center gap-3">
                <button
                  type="button"
                  onClick={() =>
                    downloadReceipt.mutate({ paymentId: payment.id, fileName: `recu-${payment.id}.pdf` })
                  }
                  className="text-sm font-medium text-brand-500 hover:underline dark:text-brand-400"
                >
                  Reçu
                </button>
                <Badge color="light">{PAYMENT_MODE_LABELS[payment.mode]}</Badge>
              </span>
            </li>
          ))}
        </ul>
      )}

    </SectionBlock>
  );
}
