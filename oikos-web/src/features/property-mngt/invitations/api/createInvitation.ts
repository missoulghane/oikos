import { httpClient } from '@/shared/api/httpClient';
import type { CreateInvitationPayload } from '@/features/property-mngt/invitations/types/invitation.types';

export async function createInvitation({ propertyId, ...body }: CreateInvitationPayload): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/invitations`, body);
}
