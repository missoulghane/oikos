import { httpClient } from '@/shared/api/httpClient';
import type { Invitation } from '@/features/property-mngt/invitations/types/invitation.types';

export async function getInvitation(id: string): Promise<Invitation> {
  const { data } = await httpClient.get<Invitation>(`/invitations/${id}`);
  return data;
}
