import { httpClient } from '@/shared/api/httpClient';
import type { AcceptInvitationPayload, MessageResponse } from '@/features/identity/register/types/register.types';

export async function acceptInvitation(payload: AcceptInvitationPayload): Promise<MessageResponse> {
  const { data } = await httpClient.post<MessageResponse>('/users/accept-invitation', payload);
  return data;
}
