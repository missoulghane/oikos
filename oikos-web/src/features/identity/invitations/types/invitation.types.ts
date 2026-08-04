export type InvitationType = 'PUBLIC' | 'PRIVATE_WITH_UNIT' | 'PRIVATE_WITHOUT_UNIT';

/** Mirrors InvitationPreviewResponse (oikos-api) - public, pre-authentication view. */
export interface InvitationPreview {
  type: InvitationType;
  usable: boolean;
  reason: string | null;
  propertyName: string;
  propertyAddress: string;
  unitNumber: string | null;
  unitTypeName: string | null;
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
 * Shared shape for both /accept and /candidacies: when the caller is
 * authenticated, email/fullName/password are ignored by the backend
 * (resolved from the session instead) - unitId still applies, since it
 * selects the lot regardless of who is submitting.
 */
export interface ConsumeInvitationPayload {
  token: string;
  email?: string;
  fullName?: string;
  password?: string;
  unitId?: string;
}
