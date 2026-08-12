import { httpClient } from '@/shared/api/httpClient';

export async function markConversationRead(conversationId: string): Promise<void> {
  await httpClient.post(`/conversations/${conversationId}/read`);
}
