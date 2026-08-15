import { Link, useParams } from 'react-router-dom';
import { useUnit } from '@/features/property-mngt/properties/hooks/useUnit';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { useUnitInstallments } from '@/features/property-mngt/installments/hooks/useUnitInstallments';
import { UnitOwnersSection } from '@/features/property-mngt/properties/components/UnitOwnersSection';
import {
  LastUnitInstallments,
  LastUnitPayments,
} from '@/features/property-ownership/units/components/LastUnitOperations';
import { getOutstandingColorClass } from '@/features/property-ownership/units/utils/unitBalance';
import { isDueBy, outstandingTotal } from '@/features/property-ownership/installments/utils/installmentTotals';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="text-sm text-gray-500 dark:text-gray-400">{label}</dt>
      <dd className="text-gray-900 dark:text-white/90">{value}</dd>
    </div>
  );
}

export function MyUnitDetailPage() {
  const { propertyId, unitId } = useParams<{ propertyId: string; unitId: string }>();
  const id = unitId ?? '';
  const unit = useUnit(id);
  // Same query key as LastUnitInstallments below, so the summary costs no extra
  // request - it reads the very rows that block lists, which is also why the
  // two can never disagree.
  const installments = useUnitInstallments(id);
  // The lot payload has no building or résidence name (nor propertyId); those
  // only exist on GET /users/me/units.
  const myUnits = useMyUnits();

  if (unit.isLoading) {
    return <Loader label="Chargement du lot…" />;
  }

  if (unit.isError) {
    return <Alert message={getErrorMessage(unit.error)} />;
  }

  if (!unit.data) {
    return null;
  }

  const ownedUnit = (myUnits.data ?? []).find((candidate) => candidate.unitId === id);
  const outstanding = outstandingTotal(installments.data ?? []);
  // Counted on the same rule as the amount above, or the two would disagree:
  // "3 échéances à régler" beside a balance that ignores two of them.
  const dueCount = (installments.data ?? []).filter((installment) => isDueBy(installment)).length;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to="/dashboard" className="text-sm text-gray-500 dark:text-gray-400 hover:underline">
          ← Retour à mes lots
        </Link>
        {/* No "Affecté" pill: in the owner's own space the lot is affected to
            them by definition, so the badge only ever states the obvious. */}
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
          Lot {unit.data.unitNumber} — {unit.data.unitTypeName}
        </h1>
        {ownedUnit && (
          <p className="text-sm text-gray-500 dark:text-gray-400">
            {ownedUnit.propertyName} — {ownedUnit.buildingName}
          </p>
        )}
      </div>

      {/* Above the two "dernières opérations" blocks: what the lot still owes is
          the figure the page exists to answer. */}
      {installments.isLoading ? (
        <Card>
          <Loader label="Chargement du solde…" />
        </Card>
      ) : (
        // Opens the echeance list already narrowed to this lot's unpaid rows -
        // the point of a balance is to lead to what makes it up, and filtering on
        // the status alone would show other lots' echeances beside a figure that
        // does not include them.
        <Link
          to={`/property-ownership/installments?status=DUE&unitId=${id}`}
          className="flex flex-col gap-1 rounded-2xl border border-gray-200 bg-white p-4 shadow-theme-xs transition hover:border-brand-500 hover:shadow-theme-md sm:p-6 dark:border-gray-800 dark:bg-white/[0.03] dark:hover:border-brand-500"
        >
          <span className="text-sm text-gray-500 dark:text-gray-400">Solde à régler</span>
          <span className={`text-2xl font-semibold ${getOutstandingColorClass(outstanding)}`}>
            {/* Signed like an account statement, as on the lot cards. */}
            {outstanding > 0 ? `-${outstanding.toLocaleString('fr-FR')}` : '0'} MAD
          </span>
          <span className="text-sm text-gray-500 dark:text-gray-400">
            {dueCount === 0
              ? 'Aucune échéance à régler'
              : `${dueCount} échéance${dueCount > 1 ? 's' : ''} à régler`}
          </span>
        </Link>
      )}

      <Card className="flex flex-col gap-3">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Informations générales</h2>
        <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <InfoRow label="Type de lot" value={unit.data.unitTypeName} />
          <InfoRow label="Tantièmes" value={`${unit.data.shares}`} />
          {ownedUnit && <InfoRow label="Votre quote-part" value={`${ownedUnit.ownershipShare} %`} />}
          {ownedUnit && <InfoRow label="Bâtiment" value={ownedUnit.buildingName} />}
          {ownedUnit && <InfoRow label="Résidence" value={ownedUnit.propertyName} />}
        </dl>
        <UnitOwnersSection unitId={id} propertyId={propertyId ?? ''} canManage={false} />
      </Card>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <LastUnitInstallments unitId={id} />
        <LastUnitPayments unitId={id} />
      </div>
    </div>
  );
}
