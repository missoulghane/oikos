import { httpClient } from '@/shared/api/httpClient';
import type { PagedAvailableUnits } from '@/features/identity/invitations/types/invitation.types';

export async function getInvitationAvailableUnits(token: string): Promise<PagedAvailableUnits> {
  const { data } = await httpClient.get<PagedAvailableUnits>(`/invitations/by-token/${token}/available-units`, {
    params: { page: 0, size: 100 },
  });
  return data;
}
