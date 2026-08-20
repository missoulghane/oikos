import type { DuesCalculationMode } from '@/features/property-mngt/properties/types/property.types';

/**
 * Types de lots proposés par le wizard. Le libellé sert tel quel de nom de type
 * côté API (find-or-create par nom), d'où le pluriel séparé pour les résumés.
 *
 * <p>Réexportés depuis property-mngt et non redéfinis ici : l'onglet
 * Configuration d'une copropriété offre exactement la même liste, et deux
 * copies auraient divergé au premier type ajouté d'un seul côté.
 */
import { UNIT_TYPE_CHOICES } from '@/features/property-mngt/properties/constants/unitTypeChoices';

export { UNIT_TYPE_CHOICES, type UnitTypeName } from '@/features/property-mngt/properties/constants/unitTypeChoices';

export interface OnboardingAccount {
  fullName: string;
  email: string;
  /** Format international, tel que le compose PhoneField (« +212612345678 »). */
  phone: string;
}

export interface OnboardingProperty {
  name: string;
  address: string;
  city: string;
}

export interface OnboardingBuilding {
  name: string;
  /** Nombre d'étages, 0 pour un bâtiment de plain-pied (villa, local commercial). */
  floorCount: number;
  /** Nombre de lots par nom de type ; un type non présent dans ce bâtiment vaut 0. */
  unitCounts: Record<string, number>;
}

export interface OnboardingBankAccount {
  label: string;
  bankAccountNumber: string;
}

export interface OnboardingRegistration {
  propertyId: string;
  onboardingToken: string;
  /**
   * Échéance du jeton d'onboarding (epoch ms). Le brouillon survit dans le
   * localStorage bien au-delà des 2 h du jeton : sans cette date, un wizard
   * repris le lendemain postait un jeton périmé et se heurtait à un 401 sans
   * issue. Absente sur un brouillon écrit avant cet ajout - on tente alors le
   * jeton, l'API tranchera.
   */
  onboardingTokenExpiresAt?: number;
}

/** Marge de sécurité : un jeton qui expire pendant l'appel est déjà périmé. */
const TOKEN_EXPIRY_MARGIN_MS = 30_000;

export function isOnboardingTokenUsable(registration: OnboardingRegistration): boolean {
  return (
    registration.onboardingTokenExpiresAt === undefined ||
    registration.onboardingTokenExpiresAt - TOKEN_EXPIRY_MARGIN_MS > Date.now()
  );
}

export interface OnboardingDraft {
  account: OnboardingAccount;
  property: OnboardingProperty;
  duesCalculationMode: DuesCalculationMode;
  projectedBudget: string;
  selectedUnitTypes: string[];
  /** Montant forfaitaire par type, saisi en texte pour ne pas perdre la frappe en cours. */
  unitTypePrices: Record<string, string>;
  buildings: OnboardingBuilding[];
  bankAccounts: OnboardingBankAccount[];
  registration: OnboardingRegistration | null;
}

export const DEFAULT_BUILDING_UNIT_COUNT = 0;

export function emptyBuilding(index: number): OnboardingBuilding {
  return { name: index === 0 ? 'Bâtiment principal' : `Bâtiment ${index + 1}`, floorCount: 0, unitCounts: {} };
}

export function initialDraft(): OnboardingDraft {
  return {
    account: { fullName: '', email: '', phone: '' },
    property: { name: '', address: '', city: '' },
    // Forfait par défaut, comme demandé - et c'est aussi le défaut du domaine.
    duesCalculationMode: 'FLAT_RATE',
    projectedBudget: '',
    selectedUnitTypes: ['Appartement'],
    unitTypePrices: {},
    buildings: [emptyBuilding(0)],
    bankAccounts: [],
    registration: null,
  };
}

/**
 * Reconstruit un brouillon complet à partir de ce qu'on relit du localStorage.
 *
 * <p>Une fusion à plat ne suffit pas : le brouillon survit aux déploiements, et
 * celui écrit avant l'ajout du téléphone porte un `account` à deux champs qui
 * remplaçait l'objet par défaut en entier - `phone` valait alors `undefined`, et
 * le formulaire de l'étape 1 plantait au premier rendu. Chaque objet imbriqué
 * est donc fusionné champ par champ.
 *
 * <p>Les collections sont vérifiées plutôt que reprises telles quelles : un
 * brouillon tronqué (onglet fermé pendant l'écriture, quota atteint) ferait
 * échouer le premier `.map` bien plus loin, sans rien qui désigne la cause.
 */
/**
 * Un bâtiment relu d'un brouillon écrit avant l'ajout d'un champ n'a pas ce
 * champ : même cause que la fusion en profondeur ci-dessus, une strate plus bas.
 */
function normalizeBuilding(building: OnboardingBuilding): OnboardingBuilding {
  return {
    name: building?.name ?? '',
    floorCount: typeof building?.floorCount === 'number' ? building.floorCount : 0,
    unitCounts: building?.unitCounts ?? {},
  };
}

export function mergeStoredDraft(stored: Partial<OnboardingDraft> | null | undefined): OnboardingDraft {
  const base = initialDraft();
  if (!stored || typeof stored !== 'object') {
    return base;
  }
  return {
    ...base,
    ...stored,
    account: { ...base.account, ...stored.account },
    property: { ...base.property, ...stored.property },
    unitTypePrices: { ...base.unitTypePrices, ...stored.unitTypePrices },
    selectedUnitTypes: Array.isArray(stored.selectedUnitTypes) ? stored.selectedUnitTypes : base.selectedUnitTypes,
    buildings: Array.isArray(stored.buildings) ? stored.buildings.map(normalizeBuilding) : base.buildings,
    bankAccounts: Array.isArray(stored.bankAccounts) ? stored.bankAccounts : base.bankAccounts,
  };
}

/**
 * Ajoute ou retire des bâtiments pour coller au nombre demandé, sans toucher à
 * ceux qui existent déjà : baisser puis remonter le compteur ne doit pas effacer
 * la saisie des premiers bâtiments.
 */
export function resizeBuildings(buildings: OnboardingBuilding[], count: number): OnboardingBuilding[] {
  if (count <= buildings.length) {
    return buildings.slice(0, count);
  }
  const grown = [...buildings];
  while (grown.length < count) {
    grown.push(emptyBuilding(grown.length));
  }
  return grown;
}

export function unitCountOf(building: OnboardingBuilding, unitTypeName: string): number {
  return building.unitCounts[unitTypeName] ?? DEFAULT_BUILDING_UNIT_COUNT;
}

/** « 12 appartements · 8 box », pour l'en-tête d'un accordéon de bâtiment. */
export function buildingSummary(building: OnboardingBuilding, selectedUnitTypes: string[]): string {
  return selectedUnitTypes
    .map((name) => ({ name, count: unitCountOf(building, name) }))
    .filter((entry) => entry.count > 0)
    .map((entry) => {
      const choice = UNIT_TYPE_CHOICES.find((candidate) => candidate.name === entry.name);
      return `${entry.count} ${choice ? choice.plural : entry.name.toLowerCase()}`;
    })
    .join(' · ');
}

export function totalUnitCount(draft: OnboardingDraft): number {
  return draft.buildings.reduce(
    (total, building) =>
      total + draft.selectedUnitTypes.reduce((sum, name) => sum + unitCountOf(building, name), 0),
    0,
  );
}

/**
 * L'API ne stocke qu'une adresse en un seul champ (250 caractères) : les deux
 * saisies du wizard sont donc recomposées ici, et c'est cette chaîne qui est
 * validée en longueur, pas chaque champ pris isolément.
 */
export function formatAddress(property: OnboardingProperty): string {
  return [property.address, property.city]
    .map((part) => part.trim())
    .filter(Boolean)
    .join(', ');
}
