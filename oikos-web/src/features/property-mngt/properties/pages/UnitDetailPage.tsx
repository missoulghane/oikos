import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useUnit } from '@/features/property-mngt/properties/hooks/useUnit';
import { useProperty } from '@/features/property-mngt/properties/hooks/useProperty';
import { UnitOwnersSection } from '@/features/property-mngt/properties/components/UnitOwnersSection';
import { EditUnitForm } from '@/features/property-mngt/properties/components/EditUnitForm';
import { useBuilding } from '@/features/property-mngt/properties/hooks/useBuilding';
import { UnitInstallmentsSection } from '@/features/property-mngt/installments/components/UnitInstallmentsSection';
import { UnitPaymentsSection } from '@/features/property-mngt/installments/components/UnitPaymentsSection';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { floorLabel } from '@/features/property-mngt/properties/utils/floorLabel';

const OWNERSHIP_STATUS_LABELS = {
  AFFECTED: 'Affecté',
  NOT_AFFECTED: 'Non affecté',
} as const;

export function UnitDetailPage() {
  const { propertyId, unitId } = useParams<{ propertyId: string; unitId: string }>();
  const id = unitId ?? '';
  const unit = useUnit(id);
  const property = useProperty(propertyId ?? '');
  const [isEditing, setIsEditing] = useState(false);
  const currentUser = useCurrentUser();
  const canRegularize = currentUser.data ? canWriteAccounting(currentUser.data, propertyId ?? '') : false;
  const showShares = property.data?.duesCalculationMode === 'SHARES';
  // Son nombre d'étages borne la liste des étages du formulaire de modification.
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
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
            {unit.data.unitNumber} — {unit.data.unitTypeName}
          </h1>
          <span className="w-fit rounded-full bg-gray-100 dark:bg-white/[0.05] px-2 py-1 text-xs font-medium text-gray-600 dark:text-gray-400">
            {OWNERSHIP_STATUS_LABELS[unit.data.ownershipStatus]}
          </span>
        </div>
        {isEditing && building.data ? (
          <div className="mt-3 max-w-md">
            <EditUnitForm
              unit={unit.data}
              building={building.data}
              showShares={showShares}
              onSuccess={() => setIsEditing(false)}
              onCancel={() => setIsEditing(false)}
            />
          </div>
        ) : (
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
            {typeof unit.data.floor === 'number' && (
              <p className="text-sm text-gray-500 dark:text-gray-400">{floorLabel(unit.data.floor)}</p>
            )}
            {showShares && <p className="text-sm text-gray-500 dark:text-gray-400">{unit.data.shares} tantièmes</p>}
            <Button
              type="button"
              variant="secondary"
              className="min-h-0 px-2 py-1 text-xs"
              disabled={!building.data}
              onClick={() => setIsEditing(true)}
            >
              Modifier le lot
            </Button>
          </div>
        )}
      </div>

      <UnitOwnersSection unitId={id} propertyId={propertyId ?? ''} title="Propriétaires" />
      <UnitInstallmentsSection propertyId={propertyId ?? ''} unitId={id} canRegularize={canRegularize} />
      <UnitPaymentsSection propertyId={propertyId ?? ''} unitId={id} title="Paiements" />
    </div>
  );
}
