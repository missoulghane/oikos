import { Link, Navigate } from 'react-router-dom';
import { PropertyList } from '@/features/property-mngt/properties/components/PropertyList';
import { useCurrentUser, canManageProperties } from '@/features/identity/me';
import { Loader } from '@/shared/components/Loader/Loader';

export function PropertiesPage() {
  const currentUser = useCurrentUser();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  if (currentUser.data && !canManageProperties(currentUser.data)) {
    return <Navigate to="/my-units" replace />;
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-slate-900">Copropriétés</h1>
        <Link
          to="/properties/new"
          className="min-h-11 rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700"
        >
          Nouvelle copropriété
        </Link>
      </div>
      <PropertyList />
    </div>
  );
}
