import { NavLink, Outlet, useOutletContext } from 'react-router-dom';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

const TABS = [
  { to: '.', label: 'Mouvements comptables', end: true },
  { to: 'other', label: 'Autres' },
];

export function FinanceSectionLayout() {
  const { property } = useOutletContext<{ property: Property }>();

  return (
    <div className="flex flex-col gap-4">
      <nav className="flex gap-4 border-b border-slate-200">
        {TABS.map((tab) => (
          <NavLink
            key={tab.label}
            to={tab.to}
            end={tab.end}
            className={({ isActive }) =>
              `-mb-px border-b-2 px-1 pb-2 text-sm font-medium ${
                isActive
                  ? 'border-slate-900 text-slate-900'
                  : 'border-transparent text-slate-500 hover:text-slate-700'
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
