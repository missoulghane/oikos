export type InvitationType = 'PUBLIC' | 'PRIVATE';

/** Mirrors InvitationPreviewResponse (oikos-api) - public, pre-authentication view. */
export interface InvitationPreview {
  type: InvitationType;
  usable: boolean;
  reason: string | null;
  propertyName: string;
  propertyAddress: string;
  targetEmail: string | null;
  /** Non nul pour une invitation au conseil syndical : le siège proposé. Un siège n'est pas un lot. */
  boardRole: string | null;
  /**
   * Le lot désigné par une invitation privée. Nul pour un lien public (il
   * circule, chacun choisit le sien) et nul aussi si ce lot a été supprimé
   * depuis l'envoi - le sélecteur reprend alors la main.
   */
  targetUnitId: string | null;
  targetUnitNumber: string | null;
  targetUnitTypeName: string | null;
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
  /** Nul pour une invitation au conseil syndical, qui ne porte sur aucun lot. */
  unitId: string | null;
}
