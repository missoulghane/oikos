import { Link, useParams } from 'react-router-dom';
import { useUnit } from '@/features/property-mngt/properties/hooks/useUnit';
import { useProperty } from '@/features/property-mngt/properties/hooks/useProperty';
import { UnitOwnersSection } from '@/features/property-mngt/properties/components/UnitOwnersSection';
import { UnitInfoSection } from '@/features/property-mngt/properties/components/UnitInfoSection';
import { useBuilding } from '@/features/property-mngt/properties/hooks/useBuilding';
import { UnitInstallmentsSection } from '@/features/property-mngt/installments/components/UnitInstallmentsSection';
import { UnitPaymentsSection } from '@/features/property-mngt/installments/components/UnitPaymentsSection';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function UnitDetailPage() {
  const { propertyId, unitId } = useParams<{ propertyId: string; unitId: string }>();
  const id = unitId ?? '';
  const unit = useUnit(id);
  const property = useProperty(propertyId ?? '');
  const currentUser = useCurrentUser();
  const canRegularize = currentUser.data ? canWriteAccounting(currentUser.data, propertyId ?? '') : false;
  const showShares = property.data?.duesCalculationMode === 'SHARES';
  // Son nom s'affiche dans les informations du lot, son nombre d'étages borne la modification.
  const building = useBuilding(unit.data?.buildingId ?? '');

  if (unit.isLoading) {
    return <Loader label="Chargement du lot…" />;
  }

  if (unit.isError) {
    return <Alert message={getErrorMessage(unit.error)} />;
  }

  if (!unit.data) {
    return null;
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to={`/property-mngt/properties/${propertyId}/property/lots`} className="text-sm text-gray-500 dark:text-gray-400 hover:underline">
          ← Retour aux lots
        </Link>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
          {unit.data.unitNumber} — {unit.data.unitTypeName}
        </h1>
      </div>

      <UnitInfoSection unit={unit.data} building={building.data} showShares={showShares} />
      <UnitOwnersSection unitId={id} propertyId={propertyId ?? ''} title="Propriétaires" />
      <UnitInstallmentsSection propertyId={propertyId ?? ''} unitId={id} canRegularize={canRegularize} />
      <UnitPaymentsSection propertyId={propertyId ?? ''} unitId={id} title="Paiements" />
    </div>
  );
}
