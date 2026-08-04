import { httpClient } from '@/shared/api/httpClient';

export async function acceptMembershipRequest(id: string): Promise<void> {
  await httpClient.patch(`/membership-requests/${id}/accept`);
}
