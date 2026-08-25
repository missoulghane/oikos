import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { BrandLogo, BrandMark } from '@/shared/components/BrandLogo/BrandLogo';
import { useSidebar } from '@/shared/context/SidebarContext';
import {
  useCurrentUser,
  boardPropertyIds,
  canManageProperties,
  hasNoPropertyAccess,
  isManagerTier,
} from '@/features/identity/me';
import { useEffectiveSpace, spaceQuerySuffix } from '@/shared/hooks/useEffectiveSpace';
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
  FolderIcon,
  ArrowDownIcon,
  ListIcon,
  MailIcon,
  EnvelopeIcon,
  PencilIcon,
  LockIcon,
  CalenderIcon,
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
  { name: 'Groupes de diffusion', path: '/messaging-groups', icon: <GroupIcon /> },
  { name: "Demandes d'adhésion", path: '/membership-requests', icon: <MailIcon /> },
  { name: 'Documents', path: '/documents', icon: <DocsIcon /> },
  { name: 'Configuration', path: '/configuration', icon: <PlugInIcon /> },
];

const INSTALLMENT_TABS = [
  { name: 'Appels de fonds', path: '/calls', icon: <PaperPlaneIcon /> },
  { name: 'Échéances', path: '', icon: <TimeIcon /> },
  { name: 'Configuration', path: '/configuration', icon: <PlugInIcon /> },
];

// Paths are relative to /property-mngt/properties/:id, like the other groups.
// "Paramètres" (the quorum thresholds) is a sibling route of the AG list rather
// than one of its tabs: it configures the copropriété, not one assembly.
const GENERAL_MEETING_TABS = [
  { name: 'Assemblées', path: '/general-meetings', icon: <GroupIcon /> },
  { name: 'Paramètres', path: '/meeting-settings', icon: <PlugInIcon /> },
];

// Matches the routes actually registered under accounting/ in
// privateRoutes.tsx (index, ledger-accounts, treasury-accounts,
// unit-accounts, journal, expenses) - the PCM accounting rewrite (ADR 0001)
// replaced the old FinancialAccount/UnitAccount model and its routes
// (financial-accounts, units, lettrage no longer exist), but this sidebar
// still pointed at the old paths, causing 404s.
// Paths are relative to /property-mngt/properties/:id (not to accounting/):
// the exercise open/close workflow below is a sibling route
// (accounting-exercise), yet belongs to the "Comptabilité" group in the menu.
const ACCOUNTING_TABS = [
  { name: 'Vue d’ensemble', path: '/accounting', icon: <DollarLineIcon /> },
  { name: 'Plan comptable', path: '/accounting/ledger-accounts', icon: <FolderIcon /> },
  { name: 'Comptes des lots', path: '/accounting/unit-accounts', icon: <BoxIconLine /> },
  { name: 'Journal', path: '/accounting/journal', icon: <ListIcon /> },
  { name: 'Dépenses', path: '/accounting/expenses', icon: <ArrowDownIcon /> },
  { name: 'Exercice comptable', path: '/accounting-exercise', icon: <LockIcon /> },
];

// Réception/Envoyé/Brouillon - each with its own icon, distinct from the
// "Messagerie" group icon above them (EnvelopeIcon) - see messagingGroup.
// The unread count belongs to Réception specifically (it counts unread
// received messages), not to the group as a whole.
//
// spaceSuffix carries the currently active space along (e.g.
// "?space=board&propertyId=…") - /messages/* itself is transverse and has
// no property id in its path, so without this a click from the board space
// lands on a URL useActiveSpace reads as neutral, and useEffectiveSpace's
// landing default (owner-first) silently swaps the viewer into the owner
// space instead of keeping them where they were.
function messagingTabs(messagingUnreadCount: number, spaceSuffix: string) {
  return [
    { name: 'Réception', path: `/messages/reception${spaceSuffix}`, icon: <MailIcon />, badge: messagingUnreadCount },
    { name: 'Envoyé', path: `/messages/sent${spaceSuffix}`, icon: <PaperPlaneIcon /> },
    { name: 'Brouillon', path: `/messages/drafts${spaceSuffix}`, icon: <PencilIcon /> },
  ];
}

// Messaging is transverse to properties (a single mailbox aggregates every
// property the account belongs to) - shared by both the staff (property
// context groups) and plain-owner (flat navItems) sidebar shapes - but the
// space the viewer is composing/reading from still needs to survive the
// click, see messagingTabs.
function messagingGroup(messagingUnreadCount: number, spaceSuffix: string): NavGroup {
  return {
    name: 'Messagerie',
    icon: <EnvelopeIcon />,
    children: messagingTabs(messagingUnreadCount, spaceSuffix),
  };
}

// Which child of a group the current URL belongs to, or null if none.
//
// A tab matches when the URL is its path or nested under it (startsWith, not
// just equality: "Réception" must stay highlighted while a nested detail route
// /messages/reception/:id is open). But an index tab ("Informations générales",
// "Échéances", "Vue d'ensemble") carries the section root as its path, which is
// a prefix of every one of its siblings - so a plain startsWith lights it up on
// every sub-tab as well. Resolving a single winner by longest match keeps the
// nested-detail behaviour while letting the more specific sibling take over.
//
// Compared on the path only, ignoring any ?space=… query string a child may
// carry (see messagingTabs) - location.pathname never has one, so comparing
// the raw child.path would never match and messaging tabs would never
// highlight as active.
function activeChildPath(children: { path: string }[], pathname: string): string | null {
  return children.reduce<string | null>((best, child) => {
    const childPathname = child.path.split('?')[0];
    const matches = pathname === childPathname || pathname.startsWith(`${childPathname}/`);
    if (!matches) return best;
    return best === null || childPathname.length > best.split('?')[0].length ? child.path : best;
  }, null);
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
        path: `/property-mngt/properties/${propertyId}${tab.path}`,
        icon: tab.icon,
      })),
    },
    {
      name: 'Assemblées générales',
      icon: <CalenderIcon />,
      children: GENERAL_MEETING_TABS.map((tab) => ({
        name: tab.name,
        path: `/property-mngt/properties/${propertyId}${tab.path}`,
        icon: tab.icon,
      })),
    },
  ];
}

export function AppSidebar() {
  const { isExpanded, isMobileOpen, isHovered, setIsHovered, closeMobileSidebar } = useSidebar();
  const location = useLocation();

  // On mobile the sidebar is an overlay covering most of the screen, so leaving
  // it open after a navigation hides the page the user just asked for. Closing
  // on the route change (rather than only on the links below) also covers
  // navigation started from the header, which stays clickable above the
  // backdrop while the drawer is open.
  useEffect(() => {
    closeMobileSidebar();
  }, [location.pathname, location.search, closeMobileSidebar]);
  const currentUser = useCurrentUser();
  // Always mounted regardless of role - drives the unread badge shown next to
  // "Messagerie" below, kept in sync with the header bell (same query/cache).
  const unreadSummary = useUnreadSummary();
  const messagingUnreadCount = unreadSummary.data?.totalUnreadMessageCount ?? 0;

  const user = currentUser.data;
  // Demande d'adhésion déposée, pas encore validée : le compte n'a ni lot ni
  // mandat. Les entrées qui n'ouvriraient que du vide sont retirées ci-dessous
  // - il ne reste que le tableau de bord, qui porte le lot en attente.
  const noPropertyAccess = user ? hasNoPropertyAccess(user) : false;
  const effectiveSpace = useEffectiveSpace();
  const mandateIds = user ? boardPropertyIds(user) : [];
  const managerTier = user ? isManagerTier(user) : false;

  // Which property's admin menu (Ma copropriété/Échéances/Comptabilité) to
  // show: exactly the property of the resolved space when it's 'board',
  // none otherwise - owner and manager both show no property-scoped group.
  // Reads the same resolution as the dashboard and the space switcher (see
  // useEffectiveSpace) so the three can never disagree on which space is
  // actually showing, whether the URL says so explicitly or a default
  // applies (landing on /dashboard with no ?space=).
  const contextPropertyId = effectiveSpace.kind === 'board' ? effectiveSpace.propertyId : null;

  // Carried onto the Messagerie links below so a click from the board space
  // stays there instead of being read as neutral and defaulted back to
  // owner (see messagingTabs' own note and useActiveSpace).
  const spaceSuffix = spaceQuerySuffix(effectiveSpace);

  // Messagerie is shown for every account type that belongs to a property at all
  // (see noPropertyAccess just below), regardless of the current route - unlike
  // the property-scoped groups (only meaningful while browsing a specific property), it
  // doesn't depend on where in the app the viewer currently is. A NavItem can't show children/
  // expand (see the NavItem/NavGroup split below), so plain owners get it as a NavGroup too
  // (instead of a flat link) to expose the 3 mailbox tabs at all.
  const propertyGroups: NavGroup[] = contextPropertyId ? propertyContextGroups(contextPropertyId) : [];
  // Un compte sans aucune affectation valide n'a pas de boîte à lettres qui
  // tienne : la messagerie est adossée aux copropriétés dont on fait partie, et
  // il n'en fait partie d'aucune tant que sa demande n'est pas validée. Le
  // profil, lui, reste atteignable - il est dans l'en-tête, pas ici.
  const groups: NavGroup[] = noPropertyAccess
    ? propertyGroups
    : [...propertyGroups, messagingGroup(messagingUnreadCount, spaceSuffix)];

  // Every menu starts closed - except the one holding the page the app
  // actually opened on (owner space or board space alike), opened by
  // itself with focus on that page, rather than every group expanded
  // regardless of where we land. `groups` above must already be resolved
  // (it depends on the space read off the initial URL) before this runs.
  const [openGroups, setOpenGroups] = useState<Set<string>>(() => {
    const landingGroup = groups.find((group) => activeChildPath(group.children, location.pathname) !== null);
    return landingGroup ? new Set([landingGroup.name]) : new Set();
  });

  const showExpanded = isExpanded || isHovered || isMobileOpen;

  const canManage = user ? canManageProperties(user) : false;
  // Distinct from canManage: an account can own units AND hold a staff role
  // elsewhere (e.g. board member on one property, owner on another) - those
  // accounts still need the owner self-service links below while browsing
  // their owner space, same as DashboardPage's defaultSpace already
  // prioritizes 'owner' whenever hasCopro is true. Gated on the *current*
  // effective space, not just account type: switching into a board mandate
  // (SpaceSwitcher) must hide these again, or a mixed account sees the
  // personal-space menu items while browsing a résidence it manages.
  // Gated on noPropertyAccess as well: the owner space is also where an account
  // with nothing at all lands (see useEffectiveSpace's defaultSpace), and these
  // three entries would then open on empty screens.
  const isOwnerSpace = effectiveSpace.kind === 'owner' && !noPropertyAccess;

  // Notifications deliberately have no sidebar entry: the header bell
  // (NotificationsBell, see AppHeader) is their single entry point, in the
  // owner space and the board/manager space alike. /notifications itself
  // stays routed and reachable from that bell.
  const navItems: NavItem[] = [
    // Carries the resolved space along (see spaceSuffix above) - without it,
    // clicking this from the board space lands on the param-less /dashboard,
    // which useEffectiveSpace reads as neutral and defaults back to owner.
    ...(mandateIds.length > 0 || managerTier
      ? [{ name: 'Tableau de bord', path: `/dashboard${spaceSuffix}`, icon: <PieChartIcon /> }]
      : []),
    ...(mandateIds.length === 0 && canManage
      ? [{ name: 'Copropriétés', path: '/property-mngt/properties', icon: <GridIcon /> }]
      : []),
    // Plain owner accounts (no board/manager role on any property) get their
    // own personal space instead of the staff property list.
    ...(!canManage ? [{ name: 'Mon tableau de bord', path: '/dashboard', icon: <PieChartIcon /> }] : []),
    // Owner self-service links: shown while the owner space is the active
    // one, even for an account that also holds a staff role elsewhere -
    // gating on canManage alone hid these entirely for that mixed case;
    // gating on account type alone (hasCopro) showed them even while
    // browsing a board mandate, which is the regression this guards against.
    // No "Mes lots" entry: the owner dashboard *is* the lot list now, so a
    // second entry would just be a duplicate of "Mon tableau de bord".
    ...(isOwnerSpace
      ? [{ name: 'Mes échéances', path: '/property-ownership/installments', icon: <TimeIcon /> }]
      : []),
    ...(isOwnerSpace
      ? [{ name: 'Mes paiements', path: '/property-ownership/payments', icon: <DollarLineIcon /> }]
      : []),
    // Convocations, answers and published minutes - the owner's whole AG surface.
    ...(isOwnerSpace
      ? [{ name: 'Assemblées générales', path: '/property-ownership/general-meetings', icon: <CalenderIcon /> }]
      : []),
  ];

  // Accordion behaviour: opening a group collapses every other one, except a
  // group currently holding the active page (see activeChildPath) - that one
  // stays open regardless, so navigating deeper into it never hides where
  // you are. Closing the group you clicked (it was already open) never
  // touches the others.
  function toggleGroup(name: string) {
    setOpenGroups((prev) => {
      const next = new Set(prev);
      if (next.has(name)) {
        next.delete(name);
        return next;
      }
      for (const group of groups) {
        if (group.name !== name && activeChildPath(group.children, location.pathname) === null) {
          next.delete(group.name);
        }
      }
      next.add(name);
      return next;
    });
  }

  function isActive(path: string) {
    // Compared on the path only, ignoring any ?space=… query string a nav
    // item may carry (see the "Tableau de bord" item below) - location.pathname
    // never has one, so comparing the raw path would never match.
    const pathname = path.split('?')[0];
    // When a property context group is shown, the top-level properties list
    // itself only highlights on the list page - its nested paths are
    // represented by the contextual groups below, not by this item, to avoid
    // double-highlighting.
    if (pathname === '/property-mngt/properties' && groups.length > 0) {
      return location.pathname === pathname;
    }
    return location.pathname === pathname || location.pathname.startsWith(`${pathname}/`);
  }

  function renderNavItem(item: NavItem) {
    return (
      <li key={item.path}>
        {/* Also closed here, not just by the route effect above: tapping the
            entry for the page already open changes no route at all. */}
        <Link
          to={item.path}
          onClick={closeMobileSidebar}
          className={`menu-item group ${isActive(item.path) ? 'menu-item-active' : 'menu-item-inactive'} ${
            !showExpanded ? 'lg:justify-center' : ''
          }`}
        >
          <span
            className={`menu-item-icon-size ${isActive(item.path) ? 'menu-item-icon-active' : 'menu-item-icon-inactive'}`}
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
    );
  }

  return (
    <aside
      className={`fixed left-0 top-0 z-50 mt-16 flex h-screen flex-col border-r border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 px-5 transition-all duration-300 ease-in-out lg:mt-0 ${
        isExpanded || isMobileOpen ? 'w-[290px]' : isHovered ? 'w-[290px]' : 'w-[90px]'
      } ${isMobileOpen ? 'translate-x-0' : '-translate-x-full'} lg:translate-x-0`}
      onMouseEnter={() => !isExpanded && setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className={`flex py-8 ${!showExpanded ? 'lg:justify-center' : 'justify-start'}`}>
        <Link to="/" onClick={closeMobileSidebar} className="text-gray-900 dark:text-white/90">
          {showExpanded ? <BrandLogo wordmarkClassName="text-xl" /> : <BrandMark />}
        </Link>
      </div>
      <nav className="flex flex-col overflow-y-auto duration-300 ease-linear no-scrollbar">
        <span
          className={`mb-4 flex text-xs uppercase text-gray-400 dark:text-gray-500 ${!showExpanded ? 'lg:justify-center' : ''}`}
        >
          {showExpanded ? 'Menu' : <HorizontaLDots className="size-6" />}
        </span>
        <ul className="flex flex-col gap-2">
          {navItems.map(renderNavItem)}
          {groups.map((group) => {
            const isOpen = openGroups.has(group.name);
            const activePath = activeChildPath(group.children, location.pathname);
            const groupActive = activePath !== null;
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
                        // Exactly one child wins per group, see activeChildPath.
                        const childActive = child.path === activePath;
                        return (
                          <li key={child.path}>
                            <Link
                              to={child.path}
                              onClick={closeMobileSidebar}
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
