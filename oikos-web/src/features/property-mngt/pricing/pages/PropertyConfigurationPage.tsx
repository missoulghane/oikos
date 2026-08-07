import { useOutletContext } from 'react-router-dom';
import { useUnitTypeDefinitions } from '@/features/property-mngt/properties/hooks/useUnitTypeDefinitions';
import { AddUnitTypeForm } from '@/features/property-mngt/properties/components/AddUnitTypeForm';
import { useUnitTypePrices } from '@/features/property-mngt/pricing/hooks/useUnitTypePrices';
import { UnitTypePriceRow } from '@/features/property-mngt/pricing/components/UnitTypePriceRow';
import { DuesCalculationModeForm } from '@/features/property-mngt/pricing/components/DuesCalculationModeForm';
import { ProjectedBudgetForm } from '@/features/property-mngt/pricing/components/ProjectedBudgetForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function PropertyConfigurationPage() {
  const { property } = useOutletContext<{ property: Property }>();
  const propertyId = property.id;
  const unitTypes = useUnitTypeDefinitions(propertyId);
  const unitTypePrices = useUnitTypePrices(propertyId);

  if (unitTypes.isLoading || unitTypePrices.isLoading) {
    return <Loader label="Chargement de la configuration…" />;
  }

  const priceByUnitTypeId = new Map(unitTypePrices.data?.map((entry) => [entry.unitTypeId, entry.price]));

  return (
    <div className="flex flex-col gap-4">
      <Card className="flex flex-col gap-4">
        <h2 className="text-base font-semibold text-gray-900">Mode de gestion des appels de fonds</h2>
        <DuesCalculationModeForm propertyId={propertyId} currentMode={property.duesCalculationMode} />
      </Card>

      {property.duesCalculationMode === 'SHARES' && (
        <Card className="flex flex-col gap-4">
          <h2 className="text-base font-semibold text-gray-900">Budget prévisionnel</h2>
          <ProjectedBudgetForm propertyId={propertyId} currentProjectedBudget={property.projectedBudget} />
        </Card>
      )}

      {property.duesCalculationMode === 'FLAT_RATE' && (
        <Card className="flex flex-col gap-4">
          <h2 className="text-base font-semibold text-gray-900">Types de lot et prix</h2>
          {unitTypes.isError && <Alert message={getErrorMessage(unitTypes.error)} />}
          {unitTypePrices.isError && <Alert message={getErrorMessage(unitTypePrices.error)} />}

          <AddUnitTypeForm propertyId={propertyId} />

          {unitTypes.data && unitTypes.data.length === 0 && (
            <EmptyState title="Aucun type de lot pour le moment">
              Ajoutez un type de lot pour pouvoir lui associer un prix.
            </EmptyState>
          )}
          {unitTypes.data && unitTypes.data.length > 0 && (
            <div className="rounded-lg border border-gray-200 px-4">
              {unitTypes.data.map((unitType) => (
                <UnitTypePriceRow
                  key={`${unitType.id}-${priceByUnitTypeId.get(unitType.id) ?? 'none'}`}
                  propertyId={propertyId}
                  unitTypeId={unitType.id}
                  label={unitType.name}
                  currentPrice={priceByUnitTypeId.get(unitType.id)}
                />
              ))}
            </div>
          )}
        </Card>
      )}
    </div>
  );
}
