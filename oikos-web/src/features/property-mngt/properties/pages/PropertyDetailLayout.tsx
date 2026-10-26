import { Link, Outlet, useLocation, useParams } from 'react-router-dom';
import { useProperty } from '@/features/property-mngt/properties/hooks/useProperty';
import { useCurrentUser, isBoardTierOnProperty } from '@/features/identity/me';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { activePropertyMenuName } from '@/shared/layouts/propertyNavigation';

export function PropertyDetailLayout() {
  const { id } = useParams<{ id: string }>();
  const propertyId = id ?? '';
  const property = useProperty(propertyId);
  const currentUser = useCurrentUser();
  const { pathname } = useLocation();

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
  // « Résidence Al Amal - Configuration » : le nom seul ne disait pas dans quel
  // menu on se trouve, et la moitié des menus portent des onglets homonymes.
  const menuName = activePropertyMenuName(propertyId, pathname);

  return (
    <div className="flex flex-col gap-6">
      <div>
        {showBackLink && (
          <Link to="/property-mngt/properties" className="text-sm text-gray-500 dark:text-gray-400 hover:underline">
            ← Retour aux copropriétés
          </Link>
        )}
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
          {menuName ? `${property.data.name} - ${menuName}` : property.data.name}
        </h1>
      </div>

      <Outlet context={{ property: property.data }} />
    </div>
  );
}
