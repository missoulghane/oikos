import { Link } from 'react-router-dom';
import { useCurrentUser, boardPropertyId, isManagerTier } from '@/features/identity/me';
import { useProperty } from '@/features/property-mngt/properties/hooks/useProperty';
import { useProperties } from '@/features/property-mngt/properties/hooks/useProperties';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

function BoardDashboard({ propertyId }: { propertyId: string }) {
  const property = useProperty(propertyId);

  if (property.isLoading) {
    return <Loader label="Chargement de votre copropriété…" />;
  }

  if (property.isError) {
    return <Alert message={getErrorMessage(property.error)} />;
  }

  if (!property.data) {
    return null;
  }

  return (
    <Card className="flex flex-col gap-4">
      <div>
        <h2 className="text-base font-semibold text-gray-900">{property.data.name}</h2>
        <p className="text-sm text-gray-500">{property.data.address}</p>
      </div>
      <div className="flex flex-wrap gap-3">
        <Link
          to={`/properties/${propertyId}/property`}
          className="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
        >
          Ma copropriété
        </Link>
        <Link
          to={`/properties/${propertyId}/installments`}
          className="rounded-lg px-4 py-2 text-sm font-medium text-gray-700 ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
        >
          Gestion des échéances
        </Link>
      </div>
    </Card>
  );
}

function ManagerDashboard() {
  const properties = useProperties(0);

  if (properties.isLoading) {
    return <Loader label="Chargement de vos copropriétés…" />;
  }

  if (properties.isError) {
    return <Alert message={getErrorMessage(properties.error)} />;
  }

  const count = properties.data?.totalElements ?? 0;

  return (
    <Card className="flex flex-col gap-4">
      <div>
        <h2 className="text-base font-semibold text-gray-900">
          Vous gérez {count} copropriété{count > 1 ? 's' : ''}
        </h2>
      </div>
      <Link
        to="/properties"
        className="w-fit rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
      >
        Voir mes copropriétés
      </Link>
    </Card>
  );
}

export function DashboardPage() {
  const currentUser = useCurrentUser();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  const boardId = currentUser.data ? boardPropertyId(currentUser.data) : null;
  const isManager = currentUser.data ? isManagerTier(currentUser.data) : false;

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900">Tableau de bord</h1>
      {boardId ? (
        <BoardDashboard propertyId={boardId} />
      ) : isManager ? (
        <ManagerDashboard />
      ) : (
        <Card>
          <p className="text-sm text-gray-600">Bienvenue sur Oikos.</p>
        </Card>
      )}
    </div>
  );
}
