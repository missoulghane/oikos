import { Link, useParams } from 'react-router-dom';
import { useMyPayments } from '@/features/property-ownership/payments/hooks/useMyPayments';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import { useDownloadPaymentReceipt } from '@/features/property-mngt/installments/hooks/useDownloadPaymentReceipt';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { Badge } from '@/shared/components/Badge/Badge';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { isNotFound } from '@/shared/utils/isNotFound';

/**
 * Resolved from the owner's payment list rather than fetched by id: there is no
 * GET /payments/{id} endpoint, and the payment's journalEntryId is unusable here
 * because reading a journal entry requires ACCOUNTING_READ, which a plain owner
 * does not hold. A page refresh still works - the list query refetches, then we
 * pick the row out of it.
 */
export function MyPaymentDetailPage() {
  const { paymentId } = useParams<{ paymentId: string }>();
  const payments = useMyPayments();
  const units = useMyUnits();
  const downloadReceipt = useDownloadPaymentReceipt();

  if (payments.isLoading || units.isLoading) {
    return <Loader label="Chargement du paiement…" />;
  }

  if (payments.isError) {
    return <Alert message={getErrorMessage(payments.error)} />;
  }

  const payment = (payments.data ?? []).find((candidate) => candidate.id === paymentId);

  if (!payment) {
    return <Alert message="Ce paiement est introuvable." />;
  }

  const unit = (units.data ?? []).find((candidate) => candidate.unitId === payment.unitId);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to="/property-ownership/payments" className="text-sm text-gray-500 dark:text-gray-400 hover:underline">
          ← Retour à mes paiements
        </Link>
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
            Paiement du {new Date(payment.valueDate).toLocaleDateString('fr-FR')}
          </h1>
          <Badge color="light">{PAYMENT_MODE_LABELS[payment.mode]}</Badge>
        </div>
      </div>

      <Card className="flex flex-col gap-3">
        <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Montant</dt>
            <dd className="text-gray-900 dark:text-white/90">{payment.amount.toLocaleString('fr-FR')} MAD</dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Mode de paiement</dt>
            <dd className="text-gray-900 dark:text-white/90">{PAYMENT_MODE_LABELS[payment.mode]}</dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Date de valeur</dt>
            <dd className="text-gray-900 dark:text-white/90">
              {new Date(payment.valueDate).toLocaleDateString('fr-FR')}
            </dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Lot</dt>
            <dd className="text-gray-900 dark:text-white/90">
              {unit ? (
                <Link to={`/property-ownership/units/${unit.propertyId}/${unit.unitId}`} className="hover:underline">
                  {formatUnitLabel(unit)}
                </Link>
              ) : (
                '—'
              )}
            </dd>
          </div>
          <div className="sm:col-span-2">
            <dt className="text-sm text-gray-500 dark:text-gray-400">Référence</dt>
            <dd className="font-mono text-xs text-gray-700 dark:text-gray-300">{payment.id}</dd>
          </div>
        </dl>
      </Card>

      <Card className="flex flex-col gap-3">
        <div>
          <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Reçu</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Le justificatif de ce versement, au format PDF.
          </p>
        </div>
        <Button
          type="button"
          variant="secondary"
          className="self-start"
          isLoading={downloadReceipt.isPending}
          onClick={() => downloadReceipt.mutate({ paymentId: payment.id, fileName: `recu-${payment.id}.pdf` })}
        >
          Télécharger le reçu
        </Button>
        {/* A payment recorded before receipts existed, or whose generation failed,
            has none - the endpoint answers 404 and this says so plainly rather
            than leaving a button that silently does nothing. */}
        {downloadReceipt.isError && (
          <Alert
            message={
              isNotFound(downloadReceipt.error)
                ? "Aucun reçu n'est disponible pour ce paiement. Rapprochez-vous de votre syndic."
                : getErrorMessage(downloadReceipt.error)
            }
          />
        )}
      </Card>
    </div>
  );
}
