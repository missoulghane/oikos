import { httpClient } from '@/shared/api/httpClient';

export async function disableInvitation(id: string): Promise<void> {
  await httpClient.patch(`/invitations/${id}/disable`);
}
