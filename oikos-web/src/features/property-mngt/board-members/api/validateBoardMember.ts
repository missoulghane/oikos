import { httpClient } from '@/shared/api/httpClient';

export interface ValidateBoardMemberParams {
  propertyId: string;
  boardMemberId: string;
}

export async function validateBoardMember({ propertyId, boardMemberId }: ValidateBoardMemberParams): Promise<void> {
  await httpClient.patch(`/properties/${propertyId}/board-members/${boardMemberId}/validate`);
}
