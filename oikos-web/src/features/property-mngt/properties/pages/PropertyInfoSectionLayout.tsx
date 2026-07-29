import { NavLink, Outlet, useOutletContext } from 'react-router-dom';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

const TABS = [
  { to: '.', label: 'Informations générales', end: true },
  { to: 'lots', label: 'Lots' },
  { to: 'contacts', label: 'Contacts' },
  { to: 'configuration', label: 'Configuration' },
];

export function PropertyInfoSectionLayout() {
  const { property } = useOutletContext<{ property: Property }>();

  return (
    <div className="flex flex-col gap-4">
      <nav className="flex gap-4 border-b border-gray-200">
        {TABS.map((tab) => (
          <NavLink
            key={tab.label}
            to={tab.to}
            end={tab.end}
            className={({ isActive }) =>
              `-mb-px border-b-2 px-1 pb-2 text-sm font-medium ${
                isActive
                  ? 'border-gray-900 text-gray-900'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`
            }
          >
            {tab.label}
          </NavLink>
        ))}
      </nav>

      <Outlet context={{ property }} />
    </div>
  );
}
