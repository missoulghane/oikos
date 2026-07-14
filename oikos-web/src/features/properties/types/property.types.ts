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

export interface Building {
  id: string;
  propertyId: string;
  name: string;
  floorCount: number;
}

export type PagedBuildings = Paged<Building>;

export const UNIT_TYPES = ['APARTMENT', 'OFFICE', 'COMMERCIAL', 'PARKING', 'STORAGE', 'OTHER'] as const;
export type UnitType = (typeof UNIT_TYPES)[number];

export type OwnershipStatus = 'SOLD' | 'UNSOLD_DEVELOPER';

export interface Unit {
  id: string;
  buildingId: string;
  unitNumber: string;
  unitType: UnitType;
  shares: number;
  ownershipStatus: OwnershipStatus;
}

export type PagedUnits = Paged<Unit>;

export interface AddUnitPayload {
  unitNumber: string;
  unitType: UnitType;
  shares: number;
}
