import { Link, Outlet, useParams } from 'react-router-dom';
import { useProperty } from '@/features/property-mngt/properties/hooks/useProperty';
import { useCurrentUser, isBoardTierOnProperty } from '@/features/identity/me';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function PropertyDetailLayout() {
  const { id } = useParams<{ id: string }>();
  const propertyId = id ?? '';
  const property = useProperty(propertyId);
  const currentUser = useCurrentUser();

  if (property.isLoading) {
    return <Loader label="Chargement de la copropriété…" />;
  }

  if (property.isError) {
    return <Alert message={getErrorMessage(property.error)} />;
  }

  if (!property.data) {
    return null;
  }

  // A board account only ever has this one property (see the RBAC creation
  // cardinality cap) - there is nothing to "go back" to.
  const showBackLink = !currentUser.data || !isBoardTierOnProperty(currentUser.data, propertyId);

  return (
    <div className="flex flex-col gap-6">
      <div>
        {showBackLink && (
          <Link to="/properties" className="text-sm text-gray-500 hover:underline">
            ← Retour aux copropriétés
          </Link>
        )}
        <h1 className="text-lg font-semibold text-gray-900">{property.data.name}</h1>
      </div>

      <Outlet context={{ property: property.data }} />
    </div>
  );
}
