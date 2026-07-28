import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useBuildings } from '@/features/property-mngt/properties/hooks/useBuildings';
import { BuildingSection } from '@/features/property-mngt/properties/components/BuildingSection';
import { AddBuildingForm } from '@/features/property-mngt/properties/components/AddBuildingForm';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function PropertyLotsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const buildings = useBuildings(property.id);
  const [isAddingBuilding, setIsAddingBuilding] = useState(false);

  if (buildings.isLoading) {
    return <Loader label="Chargement des immeubles…" />;
  }

  return (
    <div className="flex flex-col gap-4">
      {buildings.isError && <Alert message={getErrorMessage(buildings.error)} />}
      {buildings.data && buildings.data.content.length === 0 && !isAddingBuilding && (
        <EmptyState title="Aucun immeuble pour le moment">
          Cette copropriété ne contient pas encore d'immeuble.
        </EmptyState>
      )}
      {buildings.data?.content.map((building) => (
        <BuildingSection key={building.id} building={building} />
      ))}
      {isAddingBuilding ? (
        <AddBuildingForm
          propertyId={property.id}
          onSuccess={() => setIsAddingBuilding(false)}
          onCancel={() => setIsAddingBuilding(false)}
        />
      ) : (
        <Button type="button" variant="secondary" onClick={() => setIsAddingBuilding(true)} className="self-start">
          Ajouter un immeuble
        </Button>
      )}
    </div>
  );
}
