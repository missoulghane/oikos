import { httpClient } from '@/shared/api/httpClient';
import type { PagedMessages } from '@/features/messaging/types/messaging.types';

// v1 scope cut: only page 0 (the 50 most recent messages, already in
// ascending chronological order) is ever fetched - no infinite scroll/"load
// more" UI. The API stays paginated for future use.
const MESSAGES_PAGE_SIZE = 50;

export async function listConversationMessages(conversationId: string): Promise<PagedMessages> {
  const { data } = await httpClient.get<PagedMessages>(`/conversations/${conversationId}/messages`, {
    params: { page: 0, size: MESSAGES_PAGE_SIZE },
  });
  return data;
}
