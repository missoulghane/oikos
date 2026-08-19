// Mirrors property-mngt/properties/types/property.types.ts's DuesCalculationMode
// (oikos-web) - duplicated locally rather than importing from property-mngt,
// which doesn't exist on mobile (out of scope, see PLAN.md).
export const DUES_CALCULATION_MODES = ['FLAT_RATE', 'SHARES'] as const;
export type DuesCalculationMode = (typeof DUES_CALCULATION_MODES)[number];

/**
 * Types de lots proposés par le wizard. Le libellé sert tel quel de nom de type
 * côté API (find-or-create par nom), d'où le pluriel séparé pour les résumés.
 */
export const UNIT_TYPE_CHOICES = [
  { name: 'Appartement', plural: 'appartements' },
  { name: 'Box', plural: 'box' },
  { name: 'Bureau', plural: 'bureaux' },
] as const;

export type UnitTypeName = (typeof UNIT_TYPE_CHOICES)[number]['name'];

export interface OnboardingAccount {
  fullName: string;
  email: string;
}

export interface OnboardingProperty {
  name: string;
  address: string;
  addressComplement: string;
  postalCode: string;
  city: string;
}

export interface OnboardingBuilding {
  name: string;
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
  return { name: index === 0 ? 'Bâtiment principal' : `Bâtiment ${index + 1}`, unitCounts: {} };
}

export function initialDraft(): OnboardingDraft {
  return {
    account: { fullName: '', email: '' },
    property: { name: '', address: '', addressComplement: '', postalCode: '', city: '' },
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
 * L'API ne stocke qu'une adresse en un seul champ (250 caractères) : les quatre
 * saisies du wizard sont donc recomposées ici, et c'est cette chaîne qui est
 * validée en longueur, pas chaque champ pris isolément.
 */
export function formatAddress(property: OnboardingProperty): string {
  return [property.address, property.addressComplement, [property.postalCode, property.city].filter(Boolean).join(' ')]
    .map((part) => part.trim())
    .filter(Boolean)
    .join(', ');
}
