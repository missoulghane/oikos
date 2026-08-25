import { Link, useParams } from 'react-router-dom';
import { useUnit } from '@/features/property-mngt/properties/hooks/useUnit';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { useUnitInstallments } from '@/features/property-mngt/installments/hooks/useUnitInstallments';
import { useUnitPayments } from '@/features/property-mngt/installments/hooks/useUnitPayments';
import { UnitOwnersSection } from '@/features/property-mngt/properties/components/UnitOwnersSection';
import {
  LastUnitInstallments,
  LastUnitPayments,
} from '@/features/property-ownership/units/components/LastUnitOperations';
import { UnitBalanceSummary } from '@/features/property-ownership/units/components/UnitBalanceSummary';
import { unitAccountBalance, unitDueCount, unitStatementLink } from '@/features/property-ownership/units/utils/unitBalance';
import { Card } from '@/shared/components/Card/Card';
import { CardLink } from '@/shared/components/Card/CardLink';
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
  // Idem : même clé que LastUnitPayments plus bas. Le solde est un compte
  // courant, il lui faut les versements autant que les appels.
  const payments = useUnitPayments(id);
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
  const balance = unitAccountBalance(installments.data ?? [], payments.data ?? [], id);
  const dueCount = unitDueCount(installments.data ?? [], id);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to="/dashboard" className="text-sm text-gray-500 dark:text-gray-400 hover:underline">
          ← Retour à mes lots
        </Link>
        {/* No "Affecté" pill: in the owner's own space the lot is affected to
            them by definition, so the badge only ever states the obvious. */}
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
          {unit.data.unitNumber} — {unit.data.unitTypeName}
        </h1>
        {ownedUnit && (
          <p className="text-sm text-gray-500 dark:text-gray-400">
            {ownedUnit.propertyName} — {ownedUnit.buildingName}
          </p>
        )}
      </div>

      {/* Above the two "dernières opérations" blocks: what the lot still owes is
          the figure the page exists to answer. */}
      {installments.isLoading || payments.isLoading ? (
        <Card>
          <Loader label="Chargement du solde…" />
        </Card>
      ) : (
        // Mène au relevé du lot : le chiffre s'y décompose ligne à ligne, et le
        // total du relevé est exactement celui affiché ici.
        //
        // CardLink plutôt que la carte réécrite à la main qu'il y avait ici :
        // elle bleuissait sa bordure au survol, seule de toute l'application à
        // le faire. Une carte cliquable prend l'ombre, rien d'autre.
        <CardLink to={unitStatementLink(propertyId ?? '', id)}>
          <UnitBalanceSummary balance={balance} dueCount={dueCount} />
        </CardLink>
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
