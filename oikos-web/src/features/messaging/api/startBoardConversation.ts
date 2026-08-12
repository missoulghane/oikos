import { httpClient } from '@/shared/api/httpClient';
import type { StartBoardConversationPayload, StartConversationResult } from '@/features/messaging/types/messaging.types';

export async function startBoardConversation(
  propertyId: string,
  payload: StartBoardConversationPayload,
): Promise<StartConversationResult> {
  const { data } = await httpClient.post<StartConversationResult>(
    `/properties/${propertyId}/board-conversations`,
    payload,
  );
  return data;
}
