import type { Paged } from '@/shared/types/pagination.types';

export const DUES_CALCULATION_MODES = ['FLAT_RATE', 'SHARES'] as const;
export type DuesCalculationMode = (typeof DUES_CALCULATION_MODES)[number];

export interface Property {
  id: string;
  name: string;
  address: string;
  duesCalculationMode: DuesCalculationMode;
  projectedBudget: number | null;
}

export type PagedProperties = Paged<Property>;

export interface CreatePropertyPayload {
  name: string;
  address: string;
}

export interface AddBuildingPayload {
  name: string;
  floorCount: number;
}

export interface UpdatePropertyPayload {
  name: string;
  address: string;
}

export interface Building {
  id: string;
  propertyId: string;
  name: string;
  floorCount: number;
}

export type PagedBuildings = Paged<Building>;

export interface UnitTypeDefinition {
  id: string;
  propertyId: string;
  name: string;
}

export const UNIT_SORT_FIELDS = ['UNIT_NUMBER', 'SHARES'] as const;
export type UnitSortField = (typeof UNIT_SORT_FIELDS)[number];

/** Criteria of a lots listing, passed whole to the query key - see queryKeys. */
export interface UnitListFilters {
  search?: string;
  ownershipStatus?: OwnershipStatus;
  sortBy?: UnitSortField;
  sortDirection?: 'ASC' | 'DESC';
}

export const CONTACT_SORT_FIELDS = ['FULL_NAME', 'ACCOUNT_STATUS'] as const;
export type ContactSortField = (typeof CONTACT_SORT_FIELDS)[number];

/** Criteria of a contacts listing, passed whole to the query key - see queryKeys. */
export interface ContactListFilters {
  search?: string;
  hasLinkedAccount?: boolean;
  sortBy?: ContactSortField;
  sortDirection?: 'ASC' | 'DESC';
}

export type OwnershipStatus = 'AFFECTED' | 'NOT_AFFECTED';

export interface Unit {
  id: string;
  buildingId: string;
  unitNumber: string;
  unitTypeId: string;
  unitTypeName: string;
  shares: number;
  ownershipStatus: OwnershipStatus;
  ownerFullNames: string[];
}

export type PagedUnits = Paged<Unit>;

export interface AddUnitPayload {
  unitNumber: string;
  unitTypeId: string;
  shares: number;
}

export interface UpdateUnitSharesPayload {
  shares: number;
}

export const PARTY_TYPES = ['INDIVIDUAL', 'COMPANY'] as const;
export type PartyType = (typeof PARTY_TYPES)[number];

export interface UnitOwnership {
  id: string;
  unitId: string;
  partyId: string;
  partyFullName: string;
  partyType: PartyType;
  partyEmail: string;
  ownershipShare: number;
}

export interface AddUnitOwnerPayload {
  fullName: string;
  partyType: PartyType;
  email: string;
  phone?: string;
  ownershipShare: number;
}

export interface AddUnitOwnershipPayload {
  partyId: string;
  ownershipShare: number;
}

export interface PropertyContact {
  id: string;
  partyId: string;
  partyFullName: string;
  partyType: PartyType;
  partyEmail: string;
  partyPhone: string | null;
  unitId: string;
  unitNumber: string;
  buildingName: string;
  ownershipShare: number;
  hasLinkedAccount: boolean;
}

export type PagedPropertyContacts = Paged<PropertyContact>;
