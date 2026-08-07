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

export interface CreateInvitationPayload {
  propertyId: string;
  type: InvitationType;
  targetEmail?: string;
}

/**
 * INVITED is synthetic: a still-outstanding PRIVATE invitation nobody has
 * accepted yet, surfaced here for traceability but never a real, persisted
 * request (no unitId/partyId - see targetEmail instead).
 */
export type MembershipRequestStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'INVITED';

/** Mirrors MembershipRequestResponse (oikos-api). */
export interface MembershipRequest {
  id: string;
  invitationId: string;
  propertyId: string;
  unitId: string | null;
  partyId: string | null;
  /** Only populated for an INVITED entry - the party carries the email once one exists. */
  targetEmail: string | null;
  status: MembershipRequestStatus;
  decidedAt: string | null;
  decidedByUserId: string | null;
  rejectionReason: string | null;
}

export type PagedMembershipRequests = Paged<MembershipRequest>;
