import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useSidebar } from '@/shared/context/SidebarContext';
import { useCurrentUser, boardPropertyId, isManagerTier, isManagerTierOnProperty } from '@/features/identity/me';
import { GridIcon, PieChartIcon, DollarLineIcon, DocsIcon, ChevronDownIcon, HorizontaLDots } from '@/shared/icons';

interface NavItem {
  name: string;
  path: string;
  icon: React.ReactNode;
}

interface NavGroup {
  name: string;
  icon: React.ReactNode;
  children: { name: string; path: string }[];
}

const PROPERTY_INFO_TABS = [
  { name: 'Informations générales', path: '' },
  { name: 'Lots', path: '/lots' },
  { name: 'Contacts', path: '/contacts' },
  { name: 'Configuration', path: '/configuration' },
];

const INSTALLMENT_TABS = [
  { name: 'Échéances', path: '' },
  { name: 'Appels à cotisations', path: '/calls' },
  { name: 'Autres', path: '/other' },
];

const ACCOUNTING_TABS = [
  { name: 'Trésorerie', path: '' },
  { name: 'Comptes financiers', path: '/financial-accounts' },
  { name: 'Dépenses', path: '/expenses' },
  { name: 'Comptes des lots', path: '/units' },
  { name: 'Journal', path: '/journal' },
  { name: 'Lettrage', path: '/lettrage' },
];

function propertyContextGroups(propertyId: string): NavGroup[] {
  return [
    {
      name: 'Ma copropriété',
      icon: <GridIcon />,
      children: PROPERTY_INFO_TABS.map((tab) => ({
        name: tab.name,
        path: `/properties/${propertyId}/property${tab.path}`,
      })),
    },
    {
      name: 'Gestion des échéances',
      icon: <DollarLineIcon />,
      children: INSTALLMENT_TABS.map((tab) => ({
        name: tab.name,
        path: `/properties/${propertyId}/installments${tab.path}`,
      })),
    },
    {
      name: 'Comptabilité',
      icon: <DocsIcon />,
      children: ACCOUNTING_TABS.map((tab) => ({
        name: tab.name,
        path: `/properties/${propertyId}/accounting${tab.path}`,
      })),
    },
  ];
}

export function AppSidebar() {
  const { isExpanded, isMobileOpen, isHovered, setIsHovered } = useSidebar();
  const location = useLocation();
  const currentUser = useCurrentUser();
  // Both groups start expanded (matching the previous always-open behaviour);
  // the user can collapse either one independently from there.
  const [openGroups, setOpenGroups] = useState<Set<string>>(
    () => new Set(['Ma copropriété', 'Gestion des échéances', 'Comptabilité']),
  );

  const showExpanded = isExpanded || isHovered || isMobileOpen;

  const user = currentUser.data;
  const boardId = user ? boardPropertyId(user) : null;
  const managerTier = user ? isManagerTier(user) : false;
  const currentPropertyId = location.pathname.match(/^\/properties\/([^/]+)/)?.[1] ?? null;
  const isInOwnManagedProperty =
    managerTier && currentPropertyId !== null && user !== undefined && isManagerTierOnProperty(user, currentPropertyId);

  const groups: NavGroup[] = boardId
    ? propertyContextGroups(boardId)
    : isInOwnManagedProperty && currentPropertyId
      ? propertyContextGroups(currentPropertyId)
      : [];

  const navItems: NavItem[] = [
    ...(boardId || managerTier ? [{ name: 'Tableau de bord', path: '/dashboard', icon: <PieChartIcon /> }] : []),
    ...(!boardId ? [{ name: 'Copropriétés', path: '/properties', icon: <GridIcon /> }] : []),
  ];

  function toggleGroup(name: string) {
    setOpenGroups((prev) => {
      const next = new Set(prev);
      if (next.has(name)) {
        next.delete(name);
      } else {
        next.add(name);
      }
      return next;
    });
  }

  function isActive(path: string) {
    // When a property context group is shown, "/properties" itself only
    // highlights on the list page - its nested paths are represented by the
    // contextual groups below, not by this item, to avoid double-highlighting.
    if (path === '/properties' && groups.length > 0) {
      return location.pathname === path;
    }
    return location.pathname === path || location.pathname.startsWith(`${path}/`);
  }

  return (
    <aside
      className={`fixed left-0 top-0 z-50 mt-16 flex h-screen flex-col border-r border-gray-200 bg-white px-5 transition-all duration-300 ease-in-out lg:mt-0 ${
        isExpanded || isMobileOpen ? 'w-[290px]' : isHovered ? 'w-[290px]' : 'w-[90px]'
      } ${isMobileOpen ? 'translate-x-0' : '-translate-x-full'} lg:translate-x-0`}
      onMouseEnter={() => !isExpanded && setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className={`flex py-8 ${!showExpanded ? 'lg:justify-center' : 'justify-start'}`}>
        <Link to="/properties" className="text-xl font-semibold text-gray-900">
          {showExpanded ? 'Oikos' : 'O'}
        </Link>
      </div>
      <nav className="flex flex-col overflow-y-auto duration-300 ease-linear no-scrollbar">
        <span className={`mb-4 flex text-xs uppercase text-gray-400 ${!showExpanded ? 'lg:justify-center' : ''}`}>
          {showExpanded ? 'Menu' : <HorizontaLDots className="size-6" />}
        </span>
        <ul className="flex flex-col gap-2">
          {navItems.map((item) => (
            <li key={item.path}>
              <Link
                to={item.path}
                className={`menu-item group ${isActive(item.path) ? 'menu-item-active' : 'menu-item-inactive'} ${
                  !showExpanded ? 'lg:justify-center' : ''
                }`}
              >
                <span
                  className={`menu-item-icon-size ${
                    isActive(item.path) ? 'menu-item-icon-active' : 'menu-item-icon-inactive'
                  }`}
                >
                  {item.icon}
                </span>
                {showExpanded && <span className="menu-item-text">{item.name}</span>}
              </Link>
            </li>
          ))}
          {groups.map((group) => {
            const isOpen = openGroups.has(group.name);
            const groupActive = group.children.some((child) => location.pathname === child.path);
            return (
              <li key={group.name}>
                <button
                  type="button"
                  onClick={() => toggleGroup(group.name)}
                  className={`menu-item group w-full cursor-pointer ${
                    groupActive ? 'menu-item-active' : 'menu-item-inactive'
                  } ${!showExpanded ? 'lg:justify-center' : ''}`}
                >
                  <span
                    className={`menu-item-icon-size ${
                      groupActive ? 'menu-item-icon-active' : 'menu-item-icon-inactive'
                    }`}
                  >
                    {group.icon}
                  </span>
                  {showExpanded && <span className="menu-item-text">{group.name}</span>}
                  {showExpanded && (
                    <ChevronDownIcon
                      className={`ml-auto h-5 w-5 transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`}
                    />
                  )}
                </button>
                {showExpanded && (
                  <div
                    className={`grid overflow-hidden transition-all duration-300 ${
                      isOpen ? 'grid-rows-[1fr] opacity-100' : 'grid-rows-[0fr] opacity-0'
                    }`}
                  >
                    <ul className="mt-2 ml-9 space-y-1 overflow-hidden">
                      {group.children.map((child) => (
                        <li key={child.path}>
                          <Link
                            to={child.path}
                            className={`menu-dropdown-item ${
                              location.pathname === child.path
                                ? 'menu-dropdown-item-active'
                                : 'menu-dropdown-item-inactive'
                            }`}
                          >
                            {child.name}
                          </Link>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      </nav>
    </aside>
  );
}
