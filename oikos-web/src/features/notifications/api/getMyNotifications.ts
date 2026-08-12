import { httpClient } from '@/shared/api/httpClient';
import type { PagedNotifications } from '@/features/notifications/types/notification.types';

export async function getMyNotifications(page: number, size: number): Promise<PagedNotifications> {
  const { data } = await httpClient.get<PagedNotifications>('/users/me/notifications', { params: { page, size } });
  return data;
}
