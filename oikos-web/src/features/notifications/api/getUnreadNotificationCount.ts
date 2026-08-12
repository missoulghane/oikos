import { httpClient } from '@/shared/api/httpClient';
import type { UnreadNotificationCount } from '@/features/notifications/types/notification.types';

export async function getUnreadNotificationCount(): Promise<UnreadNotificationCount> {
  const { data } = await httpClient.get<UnreadNotificationCount>('/users/me/notifications/unread-count');
  return data;
}
