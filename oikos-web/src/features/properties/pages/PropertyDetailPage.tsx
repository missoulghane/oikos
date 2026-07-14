import { Link, useParams } from 'react-router-dom';
import { useProperty } from '@/features/properties/hooks/useProperty';
import { useBuildings } from '@/features/properties/hooks/useBuildings';
import { BuildingSection } from '@/features/properties/components/BuildingSection';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function PropertyDetailPage() {
  const { id } = useParams<{ id: string }>();
  const propertyId = id ?? '';
  const property = useProperty(propertyId);
  const buildings = useBuildings(propertyId);

  if (property.isLoading || buildings.isLoading) {
    return <Loader label="Chargement de la copropriété…" />;
  }

  if (property.isError) {
    return <Alert message={getErrorMessage(property.error)} />;
  }

  if (!property.data) {
    return null;
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to="/properties" className="text-sm text-slate-500 hover:underline">
          ← Retour aux copropriétés
        </Link>
        <h1 className="text-lg font-semibold text-slate-900">{property.data.name}</h1>
        <p className="text-sm text-slate-500">{property.data.address}</p>
      </div>

      <div className="flex flex-col gap-4">
        <h2 className="text-base font-semibold text-slate-900">Immeubles</h2>
        {buildings.isError && <Alert message={getErrorMessage(buildings.error)} />}
        {buildings.data && buildings.data.content.length === 0 && (
          <EmptyState title="Aucun immeuble pour le moment">
            Cette copropriété ne contient pas encore d'immeuble.
          </EmptyState>
        )}
        {buildings.data?.content.map((building) => (
          <BuildingSection key={building.id} building={building} />
        ))}
      </div>
    </div>
  );
}
