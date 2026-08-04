import { httpClient } from '@/shared/api/httpClient';
import type { PagedInvitations } from '@/features/property-mngt/invitations/types/invitation.types';

export interface GetInvitationsParams {
  propertyId: string;
  page: number;
  size: number;
}

export async function getInvitations({ propertyId, page, size }: GetInvitationsParams): Promise<PagedInvitations> {
  const { data } = await httpClient.get<PagedInvitations>(`/properties/${propertyId}/invitations`, {
    params: { page, size },
  });
  return data;
}
