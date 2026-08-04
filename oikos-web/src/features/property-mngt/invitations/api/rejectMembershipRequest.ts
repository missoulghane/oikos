import { httpClient } from '@/shared/api/httpClient';

export interface RejectMembershipRequestPayload {
  id: string;
  reason?: string;
}

export async function rejectMembershipRequest({ id, reason }: RejectMembershipRequestPayload): Promise<void> {
  await httpClient.patch(`/membership-requests/${id}/reject`, { reason });
}
