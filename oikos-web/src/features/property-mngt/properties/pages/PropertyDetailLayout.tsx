import { Link, NavLink, Outlet, useParams } from 'react-router-dom';
import { useProperty } from '@/features/property-mngt/properties/hooks/useProperty';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const MENUS = [
  { to: 'property', label: 'Ma copropriété' },
  { to: 'installments', label: 'Gestion des échéances' },
  { to: 'finance', label: 'Gestion financière' },
];

export function PropertyDetailLayout() {
  const { id } = useParams<{ id: string }>();
  const propertyId = id ?? '';
  const property = useProperty(propertyId);

  if (property.isLoading) {
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
      </div>

      <nav className="flex gap-2 border-b border-slate-200 pb-2">
        {MENUS.map((menu) => (
          <NavLink
            key={menu.label}
            to={menu.to}
            className={({ isActive }) =>
              `rounded-md px-3 py-2 text-sm font-medium ${
                isActive ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
              }`
            }
          >
            {menu.label}
          </NavLink>
        ))}
      </nav>

      <Outlet context={{ property: property.data }} />
    </div>
  );
}
