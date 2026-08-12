import { httpClient } from '@/shared/api/httpClient';

export async function markNotificationRead(notificationId: string): Promise<void> {
  await httpClient.post(`/notifications/${notificationId}/read`);
}
