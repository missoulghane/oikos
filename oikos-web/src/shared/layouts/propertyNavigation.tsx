import {
  GridIcon,
  DollarLineIcon,
  DocsIcon,
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
  LockIcon,
  CalenderIcon,
} from '@/shared/icons';

/**
 * Les menus d'une copropriété (Ma copropriété, Gestion des échéances,
 * Comptabilité, Assemblées générales) : une seule définition, lue par la
 * sidebar pour les afficher et par l'en-tête de page pour nommer le menu où
 * l'on se trouve - les deux ne peuvent ainsi jamais se contredire.
 */
export interface NavGroup {
  name: string;
  icon: React.ReactNode;
  children: { name: string; path: string; icon: React.ReactNode; badge?: number }[];
  badge?: number;
}

const PROPERTY_INFO_TABS = [
  { name: 'Informations générales', path: '', icon: <FileIcon /> },
  { name: 'Lots', path: '/lots', icon: <BoxIconLine /> },
  { name: 'Contacts', path: '/contacts', icon: <GroupIcon /> },
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
export function activeChildPath(children: { path: string }[], pathname: string): string | null {
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
export function propertyContextGroups(propertyId: string): NavGroup[] {
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

/**
 * Le nom de l'entrée de menu active pour cette URL (« Configuration »,
 * « Documents »...), ou null hors des menus d'une copropriété. Même règle de
 * correspondance que la surbrillance de la sidebar (activeChildPath) : une page
 * imbriquée - le détail d'une assemblée - porte le nom de son menu.
 */
export function activePropertyMenuName(propertyId: string, pathname: string): string | null {
  for (const group of propertyContextGroups(propertyId)) {
    const activePath = activeChildPath(group.children, pathname);
    if (activePath !== null) {
      return group.children.find((child) => child.path === activePath)?.name ?? null;
    }
  }
  return null;
}
