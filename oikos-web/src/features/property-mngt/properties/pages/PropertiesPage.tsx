import { Link, Navigate } from 'react-router-dom';
import { PropertyList } from '@/features/property-mngt/properties/components/PropertyList';
import { useCurrentUser, singleManagedPropertyId } from '@/features/identity/me';
import { Loader } from '@/shared/components/Loader/Loader';

// Reached only through /property-mngt, already guarded by RequireAccess(canManageProperties)
// at the subtree root - no need to re-check access here.
export function PropertiesPage() {
  const currentUser = useCurrentUser();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  // A staff account managing exactly one property (typically a volunteer
  // syndic board, capped at one) lands directly on its detail page - no need
  // to click through a list with a single card.
  const singlePropertyId = currentUser.data ? singleManagedPropertyId(currentUser.data) : null;
  if (singlePropertyId) {
    return <Navigate to={`/property-mngt/properties/${singlePropertyId}`} replace />;
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-gray-900">Copropriétés</h1>
        <Link
          to="/property-mngt/properties/new"
          className="inline-flex min-h-11 items-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
        >
          Nouvelle copropriété
        </Link>
      </div>
      <PropertyList />
    </div>
  );
}
