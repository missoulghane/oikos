import type { Paged } from '@/shared/types/pagination.types';
import type { PartyType } from '@/features/property-mngt/properties/types/property.types';

export interface Party {
  id: string;
  fullName: string;
  partyType: PartyType;
  /** Nul pour un contact qui n'a qu'un téléphone (voir Party côté API). */
  email: string | null;
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

export interface InvitePartyResult {
  invited: boolean;
}

export interface CreatePartyPayload {
  propertyId: string;
  fullName: string;
  partyType: PartyType;
  /** Facultatif : sans adresse, aucune invitation ne part. */
  email?: string;
  phone?: string;
  /** Envoyer le lien de création de compte. Absent, l'API invite (compatibilité). */
  invite: boolean;
}

export interface CreatePartyResult {
  id: string;
}

export const PARTY_SORT_FIELDS = ['FULL_NAME', 'EMAIL'] as const;
export type PartySortField = (typeof PARTY_SORT_FIELDS)[number];

/** Criteria of a parties listing, passed whole to the query key - see queryKeys. */
export interface PartyListFilters {
  search?: string;
  sortBy?: PartySortField;
  sortDirection?: 'ASC' | 'DESC';
}
