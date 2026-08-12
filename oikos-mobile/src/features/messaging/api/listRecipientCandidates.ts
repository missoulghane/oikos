import { httpClient } from '@/shared/api/httpClient';
import type { RecipientCandidate } from '@/features/messaging/types/messaging.types';

export async function listRecipientCandidates(propertyId: string, search?: string): Promise<RecipientCandidate[]> {
  const { data } = await httpClient.get<RecipientCandidate[]>(`/properties/${propertyId}/messaging/recipients`, {
    params: { search },
  });
  return data;
}
