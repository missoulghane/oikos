import { httpClient } from '@/shared/api/httpClient';
import type { PagedMembershipRequests } from '@/features/property-mngt/invitations/types/invitation.types';

export interface GetMembershipRequestsParams {
  propertyId: string;
  page: number;
  size: number;
}

export async function getMembershipRequests({
  propertyId,
  page,
  size,
}: GetMembershipRequestsParams): Promise<PagedMembershipRequests> {
  const { data } = await httpClient.get<PagedMembershipRequests>(`/properties/${propertyId}/membership-requests`, {
    params: { page, size },
  });
  return data;
}
