import { httpClient } from '@/shared/api/httpClient';
import type { AddBoardMemberPayload } from '@/features/property-mngt/board-members/types/boardMember.types';

export async function addBoardMember({ propertyId, ...body }: AddBoardMemberPayload): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/board-members`, body);
}
