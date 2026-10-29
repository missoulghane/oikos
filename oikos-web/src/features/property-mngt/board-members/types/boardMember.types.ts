/**
 * Les codes des sièges - fixés par l'API (BoardRole.java), d'où l'enum de
 * validation. Libellés et ordre d'affichage viennent du catalogue (useBoardRoles).
 */
export const BOARD_ROLES = [
  'PRESIDENT',
  'TREASURER',
  'SECRETARY',
  'MEMBER',
  'VOLUNTEER_MANAGER',
  'PROPERTY_MANAGER',
] as const;
export type BoardRole = (typeof BOARD_ROLES)[number];


export const BOARD_MEMBER_STATUSES = ['PENDING_VALIDATION', 'ACTIVE'] as const;
export type BoardMemberStatus = (typeof BOARD_MEMBER_STATUSES)[number];

/** Mirrors BoardMemberResponse (oikos-api). */
export interface BoardMember {
  id: string;
  propertyId: string;
  partyId: string;
  partyFullName: string;
  partyEmail: string | null;
  boardRole: BoardRole;
  status: BoardMemberStatus;
  /** True once the party is linked to a user account - same meaning as PropertyContact.hasLinkedAccount. */
  hasLinkedAccount: boolean;
}

export interface AddBoardMemberPayload {
  propertyId: string;
  fullName: string;
  email?: string;
  phone?: string;
  boardRole: BoardRole;
}

export interface CreateBoardInvitationPayload {
  propertyId: string;
  targetEmail: string;
  boardRole: BoardRole;
}
