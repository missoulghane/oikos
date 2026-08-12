import { httpClient } from '@/shared/api/httpClient';
import type { OwnedMembershipRequest } from '@/features/property-ownership/membership-requests/types/membershipRequest.types';

export async function getMyMembershipRequests(): Promise<OwnedMembershipRequest[]> {
  const { data } = await httpClient.get<OwnedMembershipRequest[]>('/users/me/membership-requests');
  return data;
}
