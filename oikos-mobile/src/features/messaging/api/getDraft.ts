import { httpClient } from '@/shared/api/httpClient';
import type { MessageDraft } from '@/features/messaging/types/messaging.types';

export async function getDraft(draftId: string): Promise<MessageDraft> {
  const { data } = await httpClient.get<MessageDraft>(`/message-drafts/${draftId}`);
  return data;
}
