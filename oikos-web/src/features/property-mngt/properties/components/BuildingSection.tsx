import { useState } from 'react';
import { Card } from '@/shared/components/Card/Card';
import { Button } from '@/shared/components/Button/Button';
import { UnitList } from '@/features/property-mngt/properties/components/UnitList';
import { AddUnitForm } from '@/features/property-mngt/properties/components/AddUnitForm';
import type { Building } from '@/features/property-mngt/properties/types/property.types';

export function BuildingSection({ building }: { building: Building }) {
  const [isAddingUnit, setIsAddingUnit] = useState(false);

  return (
    <Card className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="font-medium text-slate-900">{building.name}</h2>
          <p className="text-sm text-slate-500">{building.floorCount} étage(s)</p>
        </div>
        {!isAddingUnit && (
          <Button type="button" variant="secondary" onClick={() => setIsAddingUnit(true)}>
            Ajouter un lot
          </Button>
        )}
      </div>
      {isAddingUnit && (
        <AddUnitForm
          propertyId={building.propertyId}
          buildingId={building.id}
          onSuccess={() => setIsAddingUnit(false)}
          onCancel={() => setIsAddingUnit(false)}
        />
      )}
      <UnitList buildingId={building.id} />
    </Card>
  );
}
