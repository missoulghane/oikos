import { Navigate } from 'react-router-dom';
import { useCurrentUser, canManageProperties, singleManagedPropertyId } from '@/features/identity/me';
import { Loader } from '@/shared/components/Loader/Loader';

/**
 * Role-aware entry point for "/": decides between the gérant/syndic space
 * (/property-mngt) and the copropriétaire self-service space
 * (/property-ownership) before either subtree's own route guard would ever
 * see the request - this must run outside /property-mngt's RequireAccess, or
 * a plain owner landing on "/" would be redirected straight into a 403.
 */
export function LandingPage() {
  const currentUser = useCurrentUser();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  if (!currentUser.data || !canManageProperties(currentUser.data)) {
    return <Navigate to="/property-ownership/units" replace />;
  }

  const singlePropertyId = singleManagedPropertyId(currentUser.data);
  if (singlePropertyId) {
    return <Navigate to={`/property-mngt/properties/${singlePropertyId}`} replace />;
  }

  return <Navigate to="/property-mngt/properties" replace />;
}
