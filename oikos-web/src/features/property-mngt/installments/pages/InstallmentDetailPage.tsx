import { Link, useParams } from 'react-router-dom';
import { useInstallment } from '@/features/property-mngt/installments/hooks/useInstallment';
import { useUnit } from '@/features/property-mngt/properties/hooks/useUnit';
import { UnitPaymentsSection } from '@/features/property-mngt/installments/components/UnitPaymentsSection';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import { formatPeriod } from '@/features/property-mngt/installments/utils/formatPeriod';
import { isNotYetDue } from '@/features/property-mngt/installments/utils/installmentDueness';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

/**
 * L'échéance vue par le syndic. Le lot n'est pas porté par l'endpoint
 * (`unitNumber` n'est renseigné que par les lectures qui balaient toute la
 * copropriété), il est donc résolu ici par `useUnit` - ce qui donne aussi les
 * propriétaires, la première question posée devant un impayé.
 *
 * Les paiements affichés sont ceux du lot, pas ceux de cette échéance : le
 * détail de l'imputation n'est pas historisé (voir RecordOwnerPaymentService et
 * l'ADR 0001), seul `outstandingAmount` fait foi. Les règlements du lot sont ce
 * qui explique ce montant.
 */
export function InstallmentDetailPage() {
  const { propertyId, installmentId } = useParams<{ propertyId: string; installmentId: string }>();
  const currentPropertyId = propertyId ?? '';
  const installment = useInstallment(installmentId ?? '');
  const unitId = installment.data?.unitId ?? '';
  const unit = useUnit(unitId);

  if (installment.isLoading) {
    return <Loader label="Chargement de l'échéance…" />;
  }

  if (installment.isError) {
    return <Alert message={getErrorMessage(installment.error)} />;
  }

  if (!installment.data) {
    return null;
  }

  const settledAmount = installment.data.amount - installment.data.outstandingAmount;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to={`/property-mngt/properties/${currentPropertyId}/installments`}
          className="text-sm text-gray-500 dark:text-gray-400 hover:underline"
        >
          ← Retour aux échéances
        </Link>
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
            Échéance du {new Date(installment.data.dueDate).toLocaleDateString('fr-FR')}
          </h1>
          <span className="flex items-center gap-2">
            {/* Une échéance non échue n'est pas un impayé : le marqueur est le
                même que sur la liste, où ces lignes sont masquées par défaut. */}
            {isNotYetDue(installment.data) && (
              <span className="text-xs text-warning-600 dark:text-warning-400">à venir</span>
            )}
            <Badge color={INSTALLMENT_STATUS_BADGE_COLORS[installment.data.status]}>
              {INSTALLMENT_STATUS_LABELS[installment.data.status]}
            </Badge>
          </span>
        </div>
      </div>

      <Card>
        <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Montant appelé</dt>
            <dd className="text-gray-900 dark:text-white/90">
              {installment.data.amount.toLocaleString('fr-FR')} MAD
            </dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Déjà réglé</dt>
            <dd className="text-gray-900 dark:text-white/90">{settledAmount.toLocaleString('fr-FR')} MAD</dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Reste à payer</dt>
            <dd className="text-gray-900 dark:text-white/90">
              {installment.data.outstandingAmount.toLocaleString('fr-FR')} MAD
            </dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Appel de fonds</dt>
            <dd className="capitalize text-gray-900 dark:text-white/90">
              {installment.data.period ? formatPeriod(installment.data.period) : 'Échéance hors appel'}
            </dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Lot</dt>
            <dd className="text-gray-900 dark:text-white/90">
              <Link
                to={`/property-mngt/properties/${currentPropertyId}/units/${installment.data.unitId}`}
                className="font-medium text-brand-500 hover:underline dark:text-brand-400"
              >
                {unit.data ? `${unit.data.unitNumber} — ${unit.data.unitTypeName}` : 'Voir le lot'}
              </Link>
            </dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Propriétaire(s)</dt>
            <dd className="text-gray-900 dark:text-white/90">
              {unit.data && unit.data.ownerFullNames.length > 0 ? unit.data.ownerFullNames.join(', ') : '—'}
            </dd>
          </div>
        </dl>
      </Card>

      <Card className="flex flex-col gap-2">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Paiements du lot</h2>
        <UnitPaymentsSection propertyId={currentPropertyId} unitId={unitId} />
      </Card>
    </div>
  );
}
