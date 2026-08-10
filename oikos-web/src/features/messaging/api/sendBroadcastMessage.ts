import { httpClient } from '@/shared/api/httpClient';
import type {
  SendBroadcastMessagePayload,
  StartConversationResult,
} from '@/features/messaging/types/messaging.types';

export async function sendBroadcastMessage(
  propertyId: string,
  payload: SendBroadcastMessagePayload,
): Promise<StartConversationResult> {
  const { data } = await httpClient.post<StartConversationResult>(
    `/properties/${propertyId}/broadcast-messages`,
    payload,
  );
  return data;
}
