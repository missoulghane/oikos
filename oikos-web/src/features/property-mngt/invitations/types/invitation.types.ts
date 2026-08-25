import type { Paged } from '@/shared/types/pagination.types';

export const INVITATION_TYPES = ['PUBLIC', 'PRIVATE'] as const;
export type InvitationType = (typeof INVITATION_TYPES)[number];

export type InvitationStatus = 'ACTIVE' | 'DISABLED' | 'CONSUMED';

/** Mirrors InvitationResponse (oikos-api). */
export interface Invitation {
  id: string;
  propertyId: string;
  type: InvitationType;
  targetRole: string;
  /** Only set for a board invitation (targetRole === 'PROPERTY_BOARD_MEMBER') - the BoardRole seat offered. */
  boardRole: string | null;
  targetEmail: string | null;
  link: string;
  status: InvitationStatus;
  expiresAt: string;
  createdByUserId: string;
  /** True once a PRIVATE invitation was accepted by an email different from targetEmail - non-blocking, informational only. */
  emailMismatch: boolean;
}

export type PagedInvitations = Paged<Invitation>;

/**
 * Mirrors PublicInvitationResponse (oikos-api) : `invitation` est nul tant
 * qu'aucun lien public n'a été créé pour la copropriété - un état normal, pas
 * une absence de ressource.
 */
export interface PublicInvitationEnvelope {
  invitation: Invitation | null;
}

export interface CreateInvitationPayload {
  propertyId: string;
  type: InvitationType;
  /**
   * Le contact destinataire. Obligatoire pour PRIVATE, absent pour PUBLIC -
   * un lien public ne s'adresse à personne. L'adresse email n'est pas envoyée :
   * le serveur la lit sur la fiche du contact, une adresse venue du client
   * pouvant ne pas être la sienne.
   */
  partyId?: string;
  /** Obligatoire pour PRIVATE, absent pour PUBLIC - un lien public ne désigne aucun lot. */
  unitId?: string;
}

export type MembershipRequestStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

/** Mirrors MembershipRequestResponse (oikos-api) - identités et lot déjà résolus côté serveur. */
export interface MembershipRequest {
  id: string;
  invitationId: string;
  propertyId: string;
  unitId: string | null;
  partyId: string | null;
  userId: string | null;
  /** Nuls si le compte ou le lot a disparu depuis le dépôt : la demande reste un fait à afficher. */
  requesterFullName: string | null;
  requesterEmail: string | null;
  /** Faux tant que le demandeur n'a pas confirmé son adresse email (une demande déposée à l'inscription arrive avant). */
  requesterAccountVerified: boolean;
  unitNumber: string | null;
  unitTypeName: string | null;
  status: MembershipRequestStatus;
  submittedAt: string | null;
  decidedAt: string | null;
  decidedByUserId: string | null;
  decidedByFullName: string | null;
  rejectionReason: string | null;
}

export type PagedMembershipRequests = Paged<MembershipRequest>;

export const MEMBERSHIP_REQUEST_SORT_FIELDS = ['SUBMITTED_AT', 'REQUESTER', 'UNIT', 'STATUS'] as const;
export type MembershipRequestSortField = (typeof MEMBERSHIP_REQUEST_SORT_FIELDS)[number];
