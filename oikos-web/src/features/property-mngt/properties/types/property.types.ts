import type { Paged } from '@/shared/types/pagination.types';

export interface Property {
  id: string;
  name: string;
  address: string;
}

export type PagedProperties = Paged<Property>;

export interface CreatePropertyPayload {
  name: string;
  address: string;
  firstBuildingName: string;
  firstBuildingFloorCount: number;
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

export type OwnershipStatus = 'SOLD' | 'UNSOLD_DEVELOPER';

export interface Unit {
  id: string;
  buildingId: string;
  unitNumber: string;
  unitTypeId: string;
  unitTypeName: string;
  shares: number;
  ownershipStatus: OwnershipStatus;
}

export type PagedUnits = Paged<Unit>;

export interface AddUnitPayload {
  unitNumber: string;
  unitTypeId: string;
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
  ownershipShare: number;
}
