import { httpClient } from '@/shared/api/httpClient';

export interface RemoveBoardMemberParams {
  propertyId: string;
  boardMemberId: string;
}

export async function removeBoardMember({ propertyId, boardMemberId }: RemoveBoardMemberParams): Promise<void> {
  await httpClient.delete(`/properties/${propertyId}/board-members/${boardMemberId}`);
}
