export type InvitationType = 'PUBLIC' | 'PRIVATE';

/** Mirrors InvitationPreviewResponse (oikos-api) - public, pre-authentication view. */
export interface InvitationPreview {
  type: InvitationType;
  usable: boolean;
  reason: string | null;
  propertyName: string;
  propertyAddress: string;
  targetEmail: string | null;
}

export interface AvailableUnit {
  id: string;
  unitNumber: string;
  unitTypeName: string;
}

export interface PagedAvailableUnits {
  content: AvailableUnit[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

/**
 * Shared shape for both /accept and /membership-requests: the caller is
 * always authenticated by the time either is called (account creation now
 * happens upstream through the standard registration flow), so all that's
 * left to send is which lot was chosen.
 */
export interface ConsumeInvitationPayload {
  token: string;
  unitId: string;
}
