import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useUnit } from '@/features/property-mngt/properties/hooks/useUnit';
import { useProperty } from '@/features/property-mngt/properties/hooks/useProperty';
import { UnitOwnersSection } from '@/features/property-mngt/properties/components/UnitOwnersSection';
import { EditUnitSharesForm } from '@/features/property-mngt/properties/components/EditUnitSharesForm';
import { UnitInstallmentsSection } from '@/features/property-mngt/installments/components/UnitInstallmentsSection';
import { UnitPaymentsSection } from '@/features/property-mngt/installments/components/UnitPaymentsSection';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const OWNERSHIP_STATUS_LABELS = {
  AFFECTED: 'Affecté',
  NOT_AFFECTED: 'Non affecté',
} as const;

export function UnitDetailPage() {
  const { propertyId, unitId } = useParams<{ propertyId: string; unitId: string }>();
  const id = unitId ?? '';
  const unit = useUnit(id);
  const property = useProperty(propertyId ?? '');
  const [isEditingShares, setIsEditingShares] = useState(false);
  const showShares = property.data?.duesCalculationMode === 'SHARES';

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
        <Link to={`/property-mngt/properties/${propertyId}/property/lots`} className="text-sm text-gray-500 hover:underline">
          ← Retour aux lots
        </Link>
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <h1 className="text-lg font-semibold text-gray-900">
            Lot {unit.data.unitNumber} — {unit.data.unitTypeName}
          </h1>
          <span className="w-fit rounded-full bg-gray-100 px-2 py-1 text-xs font-medium text-gray-600">
            {OWNERSHIP_STATUS_LABELS[unit.data.ownershipStatus]}
          </span>
        </div>
        {showShares &&
          (isEditingShares ? (
            <div className="mt-2 max-w-xs">
              <EditUnitSharesForm
                unit={unit.data}
                onSuccess={() => setIsEditingShares(false)}
                onCancel={() => setIsEditingShares(false)}
              />
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <p className="text-sm text-gray-500">{unit.data.shares} tantièmes</p>
              <Button type="button" variant="secondary" className="min-h-0 px-2 py-1 text-xs" onClick={() => setIsEditingShares(true)}>
                Modifier
              </Button>
            </div>
          ))}
      </div>

      <Card className="flex flex-col gap-2">
        <h2 className="text-base font-semibold text-gray-900">Propriétaires</h2>
        <UnitOwnersSection unitId={id} propertyId={propertyId ?? ''} />
      </Card>

      <Card className="flex flex-col gap-2">
        <h2 className="text-base font-semibold text-gray-900">Échéances</h2>
        <UnitInstallmentsSection propertyId={propertyId ?? ''} unitId={id} />
      </Card>

      <Card className="flex flex-col gap-2">
        <h2 className="text-base font-semibold text-gray-900">Paiements</h2>
        <UnitPaymentsSection propertyId={propertyId ?? ''} unitId={id} />
      </Card>
    </div>
  );
}
