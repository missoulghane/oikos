import { httpClient } from '@/shared/api/httpClient';
import type { StartConversationResult } from '@/features/messaging/types/messaging.types';

export async function sendDraft(draftId: string): Promise<StartConversationResult> {
  const { data } = await httpClient.post<StartConversationResult>(`/message-drafts/${draftId}/send`);
  return data;
}
