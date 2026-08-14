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
import { isDue, outstandingTotal } from '@/features/property-ownership/installments/utils/installmentTotals';
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
  const dueCount = (installments.data ?? []).filter(isDue).length;

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
      <Card className="flex flex-col gap-1">
        <span className="text-sm text-gray-500 dark:text-gray-400">Solde à régler</span>
        {installments.isLoading ? (
          <Loader label="Chargement du solde…" />
        ) : (
          <>
            <span className={`text-2xl font-semibold ${getOutstandingColorClass(outstanding)}`}>
              {outstanding.toLocaleString('fr-FR')} MAD
            </span>
            <span className="text-sm text-gray-500 dark:text-gray-400">
              {dueCount === 0
                ? 'Aucune échéance à régler'
                : `${dueCount} échéance${dueCount > 1 ? 's' : ''} à régler`}
            </span>
          </>
        )}
      </Card>

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
