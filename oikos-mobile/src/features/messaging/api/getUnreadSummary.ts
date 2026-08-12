import { httpClient } from '@/shared/api/httpClient';
import type { UnreadSummary } from '@/features/messaging/types/messaging.types';

export async function getUnreadSummary(): Promise<UnreadSummary> {
  const { data } = await httpClient.get<UnreadSummary>('/users/me/conversations/unread-summary');
  return data;
}
