import { useState } from 'react';
import { Card } from '@/shared/components/Card/Card';
import { Button } from '@/shared/components/Button/Button';
import { UnitList } from '@/features/property-mngt/properties/components/UnitList';
import { AddUnitForm } from '@/features/property-mngt/properties/components/AddUnitForm';
import { EditBuildingForm } from '@/features/property-mngt/properties/components/EditBuildingForm';
import type { Building } from '@/features/property-mngt/properties/types/property.types';

interface BuildingSectionProps {
  building: Building;
  showShares: boolean;
}

export function BuildingSection({ building, showShares }: BuildingSectionProps) {
  const [isAddingUnit, setIsAddingUnit] = useState(false);
  const [isEditingBuilding, setIsEditingBuilding] = useState(false);

  if (isEditingBuilding) {
    return (
      <Card className="flex flex-col gap-4">
        <h2 className="font-medium text-gray-900 dark:text-white/90">Modifier l'immeuble</h2>
        <EditBuildingForm
          building={building}
          onSuccess={() => setIsEditingBuilding(false)}
          onCancel={() => setIsEditingBuilding(false)}
        />
      </Card>
    );
  }

  return (
    <Card className="flex flex-col gap-4">
      <div className="flex items-center justify-between gap-3">
        <div className="min-w-0">
          <h2 className="font-medium text-gray-900 dark:text-white/90">{building.name}</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">{building.floorCount} étage(s)</p>
        </div>
        {!isAddingUnit && (
          <div className="flex shrink-0 gap-2">
            <Button type="button" variant="secondary" onClick={() => setIsEditingBuilding(true)}>
              Modifier l'immeuble
            </Button>
            <Button type="button" variant="secondary" onClick={() => setIsAddingUnit(true)}>
              Ajouter un lot
            </Button>
          </div>
        )}
      </div>
      {isAddingUnit && (
        <AddUnitForm
          propertyId={building.propertyId}
          buildingId={building.id}
          showShares={showShares}
          onSuccess={() => setIsAddingUnit(false)}
          onCancel={() => setIsAddingUnit(false)}
        />
      )}
      <UnitList buildingId={building.id} propertyId={building.propertyId} showShares={showShares} />
    </Card>
  );
}
