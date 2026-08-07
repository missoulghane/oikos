export type OwnedMembershipRequestStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

/** Mirrors OwnedMembershipRequestResponse (oikos-api). */
export interface OwnedMembershipRequest {
  id: string;
  propertyName: string | null;
  unitNumber: string | null;
  unitTypeName: string | null;
  status: OwnedMembershipRequestStatus;
  decidedAt: string | null;
  rejectionReason: string | null;
}
