import { httpClient } from '@/shared/api/httpClient';
import type { Message, SendMessagePayload } from '@/features/messaging/types/messaging.types';

export async function sendMessage(conversationId: string, payload: SendMessagePayload): Promise<Message> {
  const { data } = await httpClient.post<Message>(`/conversations/${conversationId}/messages`, payload);
  return data;
}
