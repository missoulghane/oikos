import { httpClient } from '@/shared/api/httpClient';
import type { StartConversationPayload, StartConversationResult } from '@/features/messaging/types/messaging.types';

export async function startConversation(
  propertyId: string,
  payload: StartConversationPayload,
): Promise<StartConversationResult> {
  const { data } = await httpClient.post<StartConversationResult>(
    `/properties/${propertyId}/conversations`,
    payload,
  );
  return data;
}
