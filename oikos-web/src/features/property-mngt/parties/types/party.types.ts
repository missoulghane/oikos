import type { Paged } from '@/shared/types/pagination.types';
import type { PartyType } from '@/features/property-mngt/properties/types/property.types';

export interface Party {
  id: string;
  fullName: string;
  partyType: PartyType;
  email: string;
  phone: string | null;
}

export type PagedParties = Paged<Party>;

export interface PartyLot {
  id: string;
  unitId: string;
  unitNumber: string;
  buildingId: string;
  buildingName: string;
  propertyId: string;
  propertyName: string;
  ownershipShare: number;
}
