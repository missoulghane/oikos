import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useSidebar } from '@/shared/context/SidebarContext';
import {
  useCurrentUser,
  boardPropertyId,
  canManageProperties,
  isManagerTier,
  isManagerTierOnProperty,
} from '@/features/identity/me';
import { useUnreadSummary } from '@/features/messaging';
import {
  GridIcon,
  PieChartIcon,
  DollarLineIcon,
  DocsIcon,
  ChevronDownIcon,
  HorizontaLDots,
  FileIcon,
  BoxIconLine,
  GroupIcon,
  PlugInIcon,
  TimeIcon,
  PaperPlaneIcon,
  MoreDotIcon,
  FolderIcon,
  ArrowDownIcon,
  ListIcon,
  MailIcon,
  UserCircleIcon,
  EnvelopeIcon,
  PencilIcon,
  LockIcon,
} from '@/shared/icons';
import { Badge } from '@/shared/components/Badge/Badge';
import { SidebarWidget } from './SidebarWidget';

interface NavItem {
  name: string;
  path: string;
  icon: React.ReactNode;
  badge?: number;
}

interface NavGroup {
  name: string;
  icon: React.ReactNode;
  children: { name: string; path: string; icon: React.ReactNode; badge?: number }[];
  badge?: number;
}

const PROPERTY_INFO_TABS = [
  { name: 'Informations générales', path: '', icon: <FileIcon /> },
  { name: 'Lots', path: '/lots', icon: <BoxIconLine /> },
  { name: 'Contacts', path: '/contacts', icon: <GroupIcon /> },
  { name: 'Invitations', path: '/invitations', icon: <MailIcon /> },
  { name: 'Bureau', path: '/board', icon: <UserCircleIcon /> },
  { name: 'Documents', path: '/documents', icon: <DocsIcon /> },
  { name: 'Configuration', path: '/configuration', icon: <PlugInIcon /> },
];

const INSTALLMENT_TABS = [
  { name: 'Appels de fonds', path: '/calls', icon: <PaperPlaneIcon /> },
  { name: 'Échéances', path: '', icon: <TimeIcon /> },
  { name: 'Autres', path: '/other', icon: <MoreDotIcon /> },
];

// Matches the routes actually registered under accounting/ in
// privateRoutes.tsx (index, ledger-accounts, treasury-accounts,
// unit-accounts, journal, expenses) - the PCM accounting rewrite (ADR 0001)
// replaced the old FinancialAccount/UnitAccount model and its routes
// (financial-accounts, units, lettrage no longer exist), but this sidebar
// still pointed at the old paths, causing 404s.
const ACCOUNTING_TABS = [
  { name: 'Vue d’ensemble', path: '', icon: <DollarLineIcon /> },
  { name: 'Plan comptable', path: '/ledger-accounts', icon: <FolderIcon /> },
  { name: 'Comptes des lots', path: '/unit-accounts', icon: <BoxIconLine /> },
  { name: 'Journal', path: '/journal', icon: <ListIcon /> },
  { name: 'Dépenses', path: '/expenses', icon: <ArrowDownIcon /> },
];

// Standalone menu (kept out of ACCOUNTING_TABS/"Comptabilité" on purpose - the
// exercise open/close workflow used to live on the accounting overview tab
// but was moved to its own top-level group, placed last among the
// property-scoped groups, see propertyContextGroups below).
const ACCOUNTING_EXERCISE_TABS = [{ name: 'Ouverture / Clôture', path: '', icon: <TimeIcon /> }];

// Réception/Envoyé/Brouillon - each with its own icon, distinct from the
// "Messagerie" group icon above them (EnvelopeIcon) - see messagingGroup.
// The unread count belongs to Réception specifically (it counts unread
// received messages), not to the group as a whole.
function messagingTabs(messagingUnreadCount: number) {
  return [
    { name: 'Réception', path: '/messages/reception', icon: <MailIcon />, badge: messagingUnreadCount },
    { name: 'Envoyé', path: '/messages/sent', icon: <PaperPlaneIcon /> },
    { name: 'Brouillon', path: '/messages/drafts', icon: <PencilIcon /> },
  ];
}

// Messaging is transverse to properties (a single mailbox aggregates every
// property the account belongs to), so it always points at the same
// top-level /messages/* routes regardless of which property context this
// sidebar happens to be showing - shared by both the staff (property
// context groups) and plain-owner (flat navItems) sidebar shapes.
function messagingGroup(messagingUnreadCount: number): NavGroup {
  return {
    name: 'Messagerie',
    icon: <EnvelopeIcon />,
    children: messagingTabs(messagingUnreadCount),
  };
}

// Property-scoped groups only (Ma copropriété/Gestion des échéances/Comptabilité) - Messagerie
// is deliberately not built into this list, see the always-appended messagingGroup(...) below:
// unlike these, it never depends on a propertyId in the current URL, so it must not disappear
// just because the viewer has navigated away from a /property-mngt/properties/:id/* route.
function propertyContextGroups(propertyId: string): NavGroup[] {
  return [
    {
      name: 'Ma copropriété',
      icon: <GridIcon />,
      children: PROPERTY_INFO_TABS.map((tab) => ({
        name: tab.name,
        path: `/property-mngt/properties/${propertyId}/property${tab.path}`,
        icon: tab.icon,
      })),
    },
    {
      name: 'Gestion des échéances',
      icon: <DollarLineIcon />,
      children: INSTALLMENT_TABS.map((tab) => ({
        name: tab.name,
        path: `/property-mngt/properties/${propertyId}/installments${tab.path}`,
        icon: tab.icon,
      })),
    },
    {
      name: 'Comptabilité',
      icon: <DocsIcon />,
      children: ACCOUNTING_TABS.map((tab) => ({
        name: tab.name,
        path: `/property-mngt/properties/${propertyId}/accounting${tab.path}`,
        icon: tab.icon,
      })),
    },
    {
      name: 'Exercice comptable',
      icon: <LockIcon />,
      children: ACCOUNTING_EXERCISE_TABS.map((tab) => ({
        name: tab.name,
        path: `/property-mngt/properties/${propertyId}/accounting-exercise${tab.path}`,
        icon: tab.icon,
      })),
    },
  ];
}

export function AppSidebar() {
  const { isExpanded, isMobileOpen, isHovered, setIsHovered } = useSidebar();
  const location = useLocation();
  const currentUser = useCurrentUser();
  // Always mounted regardless of role - drives the unread badge shown next to
  // "Messagerie" below, kept in sync with the header bell (same query/cache).
  const unreadSummary = useUnreadSummary();
  const messagingUnreadCount = unreadSummary.data?.totalUnreadMessageCount ?? 0;
  // Both groups start expanded (matching the previous always-open behaviour);
  // the user can collapse either one independently from there.
  const [openGroups, setOpenGroups] = useState<Set<string>>(
    () => new Set(['Ma copropriété', 'Gestion des échéances', 'Comptabilité', 'Exercice comptable', 'Messagerie']),
  );

  const showExpanded = isExpanded || isHovered || isMobileOpen;

  const user = currentUser.data;
  const boardId = user ? boardPropertyId(user) : null;
  const managerTier = user ? isManagerTier(user) : false;
  const currentPropertyId = location.pathname.match(/^\/property-mngt\/properties\/([^/]+)/)?.[1] ?? null;
  const isInOwnManagedProperty =
    managerTier &&
    currentPropertyId !== null &&
    user !== undefined &&
    isManagerTierOnProperty(user, currentPropertyId);

  // Messagerie is always shown, for every account type, regardless of the current route - unlike
  // the property-scoped groups below (only meaningful while browsing a specific property), it
  // doesn't depend on where in the app the viewer currently is. A NavItem can't show children/
  // expand (see the NavItem/NavGroup split below), so plain owners get it as a NavGroup too
  // (instead of a flat link) to expose the 3 mailbox tabs at all.
  const propertyGroups: NavGroup[] = boardId
    ? propertyContextGroups(boardId)
    : isInOwnManagedProperty && currentPropertyId
      ? propertyContextGroups(currentPropertyId)
      : [];
  const groups: NavGroup[] = [...propertyGroups, messagingGroup(messagingUnreadCount)];

  const canManage = user ? canManageProperties(user) : false;

  const navItems: NavItem[] = [
    ...(boardId || managerTier
      ? [{ name: 'Tableau de bord', path: '/dashboard', icon: <PieChartIcon /> }]
      : []),
    ...(!boardId && canManage
      ? [{ name: 'Copropriétés', path: '/property-mngt/properties', icon: <GridIcon /> }]
      : []),
    // Plain owner accounts (no board/manager role on any property) get their
    // own personal space instead of the staff property list.
    ...(!canManage ? [{ name: 'Mon tableau de bord', path: '/dashboard', icon: <PieChartIcon /> }] : []),
    ...(!canManage ? [{ name: 'Mes lots', path: '/property-ownership/units', icon: <BoxIconLine /> }] : []),
    ...(!canManage
      ? [{ name: 'Mes échéances', path: '/property-ownership/installments', icon: <TimeIcon /> }]
      : []),
    ...(!canManage
      ? [{ name: 'Mes invitations', path: '/property-ownership/membership-requests', icon: <MailIcon /> }]
      : []),
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
    // When a property context group is shown, the top-level properties list
    // itself only highlights on the list page - its nested paths are
    // represented by the contextual groups below, not by this item, to avoid
    // double-highlighting.
    if (path === '/property-mngt/properties' && groups.length > 0) {
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
        <Link to="/" className="text-xl font-semibold text-gray-900">
          {showExpanded ? 'Oikos' : 'O'}
        </Link>
      </div>
      <nav className="flex flex-col overflow-y-auto duration-300 ease-linear no-scrollbar">
        <span
          className={`mb-4 flex text-xs uppercase text-gray-400 ${!showExpanded ? 'lg:justify-center' : ''}`}
        >
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
                {showExpanded && Boolean(item.badge) && (
                  <Badge color="error" variant="solid" className="ml-auto">
                    {item.badge}
                  </Badge>
                )}
              </Link>
            </li>
          ))}
          {groups.map((group) => {
            const isOpen = openGroups.has(group.name);
            const groupActive = group.children.some(
              (child) => location.pathname === child.path || location.pathname.startsWith(`${child.path}/`),
            );
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
                  {showExpanded && Boolean(group.badge) && (
                    <Badge color="error" variant="solid" className="ml-auto">
                      {group.badge}
                    </Badge>
                  )}
                  {showExpanded && (
                    <ChevronDownIcon
                      className={`h-5 w-5 shrink-0 transition-transform duration-200 ${
                        group.badge ? 'ml-2' : 'ml-auto'
                      } ${isOpen ? 'rotate-180' : ''}`}
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
                      {group.children.map((child) => {
                        // startsWith, not just an exact match: a tab like
                        // "Réception" must stay highlighted while a nested
                        // detail route is open (/messages/reception/:id).
                        const childActive =
                          location.pathname === child.path || location.pathname.startsWith(`${child.path}/`);
                        return (
                          <li key={child.path}>
                            <Link
                              to={child.path}
                              className={`menu-dropdown-item ${
                                childActive ? 'menu-dropdown-item-active' : 'menu-dropdown-item-inactive'
                              }`}
                            >
                              <span
                                className={`[&_svg]:size-4 ${
                                  childActive ? 'menu-item-icon-active' : 'menu-item-icon-inactive'
                                }`}
                              >
                                {child.icon}
                              </span>
                              {child.name}
                              {Boolean(child.badge) && (
                                <Badge color="error" variant="solid" className="ml-auto">
                                  {child.badge}
                                </Badge>
                              )}
                            </Link>
                          </li>
                        );
                      })}
                    </ul>
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      </nav>
      {showExpanded && <SidebarWidget />}
    </aside>
  );
}
