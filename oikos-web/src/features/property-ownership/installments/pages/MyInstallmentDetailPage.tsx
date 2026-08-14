import { Link, useParams } from 'react-router-dom';
import { useInstallment } from '@/features/property-ownership/installments/hooks/useInstallment';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { formatPeriod } from '@/features/property-mngt/installments/utils/formatPeriod';
import { UnitPaymentsSection } from '@/features/property-mngt/installments/components/UnitPaymentsSection';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-mngt/installments/constants/installmentStatusLabels';
import { Card } from '@/shared/components/Card/Card';
import { Badge } from '@/shared/components/Badge/Badge';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function MyInstallmentDetailPage() {
  const { installmentId } = useParams<{ installmentId: string }>();
  const installment = useInstallment(installmentId ?? '');
  const units = useMyUnits();

  if (installment.isLoading) {
    return <Loader label="Chargement de l'échéance…" />;
  }

  if (installment.isError) {
    return <Alert message={getErrorMessage(installment.error)} />;
  }

  if (!installment.data) {
    return null;
  }

  const unit = (units.data ?? []).find((candidate) => candidate.unitId === installment.data.unitId);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to="/property-ownership/installments"
          className="text-sm text-gray-500 dark:text-gray-400 hover:underline"
        >
          ← Retour à mes échéances
        </Link>
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
            Échéance du {new Date(installment.data.dueDate).toLocaleDateString('fr-FR')}
          </h1>
          <Badge color={INSTALLMENT_STATUS_BADGE_COLORS[installment.data.status]}>
            {INSTALLMENT_STATUS_LABELS[installment.data.status]}
          </Badge>
        </div>
      </div>

      <Card className="flex flex-col gap-3">
        <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Montant</dt>
            <dd className="text-gray-900 dark:text-white/90">
              {installment.data.amount.toLocaleString('fr-FR')} MAD
            </dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Reste à payer</dt>
            <dd className="text-gray-900 dark:text-white/90">
              {installment.data.outstandingAmount.toLocaleString('fr-FR')} MAD
            </dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Lot</dt>
            <dd className="text-gray-900 dark:text-white/90">
              {unit ? (
                <Link
                  to={`/property-ownership/units/${unit.propertyId}/${unit.unitId}`}
                  className="hover:underline"
                >
                  {formatUnitLabel(unit)}
                </Link>
              ) : (
                '—'
              )}
            </dd>
          </div>
          {installment.data.period && (
            <div>
              <dt className="text-sm text-gray-500 dark:text-gray-400">Appel de fonds</dt>
              <dd className="text-gray-900 dark:text-white/90">{formatPeriod(installment.data.period)}</dd>
            </div>
          )}
        </dl>
      </Card>

      {unit && (
        <Card className="flex flex-col gap-2">
          {/* Deliberately the payments of the *lot*, not of this echeance: there is
              no endpoint exposing which payments were allocated to a given
              echeance (allocations are only returned when a payment is created). */}
          <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Paiements du lot</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Tous les paiements enregistrés sur ce lot, toutes échéances confondues.
          </p>
          <UnitPaymentsSection propertyId={unit.propertyId} unitId={unit.unitId} canManage={false} />
        </Card>
      )}
    </div>
  );
}
