import { httpClient } from '@/shared/api/httpClient';
import type { CreateBoardInvitationPayload } from '@/features/property-mngt/board-members/types/boardMember.types';

export async function createBoardInvitation({ propertyId, ...body }: CreateBoardInvitationPayload): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/board-invitations`, body);
}
