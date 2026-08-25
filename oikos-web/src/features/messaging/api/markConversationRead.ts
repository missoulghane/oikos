import { httpClient } from '@/shared/api/httpClient';

export async function markConversationRead(conversationId: string): Promise<void> {
  await httpClient.post(`/conversations/${conversationId}/read`);
}

/** Repasse la conversation en non lue pour l'appelant - l'inverse du geste ci-dessus. */
export async function markConversationUnread(conversationId: string): Promise<void> {
  await httpClient.post(`/conversations/${conversationId}/unread`);
}
