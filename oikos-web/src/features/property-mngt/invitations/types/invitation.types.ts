import type { Paged } from '@/shared/types/pagination.types';

export const INVITATION_TYPES = ['PUBLIC', 'PRIVATE_WITH_UNIT', 'PRIVATE_WITHOUT_UNIT'] as const;
export type InvitationType = (typeof INVITATION_TYPES)[number];

export type InvitationStatus = 'ACTIVE' | 'DISABLED' | 'CONSUMED';

/** Mirrors InvitationResponse (oikos-api). */
export interface Invitation {
  id: string;
  propertyId: string;
  type: InvitationType;
  targetRole: string;
  unitId: string | null;
  targetEmail: string | null;
  link: string;
  status: InvitationStatus;
  expiresAt: string;
  createdByUserId: string;
}

export type PagedInvitations = Paged<Invitation>;

export interface CreateInvitationPayload {
  propertyId: string;
  type: InvitationType;
  unitId?: string;
  targetEmail?: string;
}

export type MembershipRequestStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

/** Mirrors MembershipRequestResponse (oikos-api). */
export interface MembershipRequest {
  id: string;
  invitationId: string;
  propertyId: string;
  unitId: string;
  partyId: string;
  status: MembershipRequestStatus;
  decidedAt: string | null;
  decidedByUserId: string | null;
  rejectionReason: string | null;
}

export type PagedMembershipRequests = Paged<MembershipRequest>;
