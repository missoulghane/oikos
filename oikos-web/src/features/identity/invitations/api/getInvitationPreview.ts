import { httpClient } from '@/shared/api/httpClient';
import type { InvitationPreview } from '@/features/identity/invitations/types/invitation.types';

export async function getInvitationPreview(token: string): Promise<InvitationPreview> {
  const { data } = await httpClient.get<InvitationPreview>(`/invitations/by-token/${token}`);
  return data;
}
