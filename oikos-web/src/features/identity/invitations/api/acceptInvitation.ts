import { httpClient } from '@/shared/api/httpClient';
import type { ConsumeInvitationPayload } from '@/features/identity/invitations/types/invitation.types';

export async function acceptInvitation({ token, ...body }: ConsumeInvitationPayload): Promise<void> {
  await httpClient.post(`/invitations/by-token/${token}/accept`, body);
}
