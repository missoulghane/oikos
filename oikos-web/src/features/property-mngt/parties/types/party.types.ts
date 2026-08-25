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

/**
 * Où en est le contact vis-à-vis d'un compte de la plateforme, et donc ce que
 * sa fiche autorise :
 * - ACTIVE : un compte lui est rattaché. Son titulaire tient lui-même son
 *   identité à jour, la fiche n'en est que la copie du syndic.
 * - INVITED : une invitation court toujours, personne ne l'a encore acceptée.
 *   Le nom et l'adresse partis avec elle sont figés (voir PartyInvitationToken
 *   côté API) : les modifier ici n'y change rien, il faut la renvoyer.
 * - NONE : ni compte, ni invitation en cours.
 */
export const PARTY_ACCOUNT_STATUSES = ['ACTIVE', 'INVITED', 'NONE'] as const;
export type PartyAccountStatus = (typeof PARTY_ACCOUNT_STATUSES)[number];

/** La fiche d'un contact : le contact, plus son rapport aux comptes (voir PartyDetailResponse côté API). */
export interface PartyDetail extends Party {
  accountStatus: PartyAccountStatus;
}

export interface UpdatePartyPayload {
  fullName: string;
  partyType: PartyType;
  /** Facultatif : un contact peut n'avoir qu'un téléphone. */
  email?: string;
  phone?: string;
}

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
