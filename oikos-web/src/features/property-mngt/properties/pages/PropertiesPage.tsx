import { Link } from 'react-router-dom';
import { PropertyList } from '@/features/property-mngt/properties/components/PropertyList';

export function PropertiesPage() {
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
