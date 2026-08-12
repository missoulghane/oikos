import { httpClient } from '@/shared/api/httpClient';
import type { CurrentUser } from '@/features/identity/me/types/me.types';

export async function removeAvatar(): Promise<CurrentUser> {
  const { data } = await httpClient.delete<CurrentUser>('/users/me/avatar');
  return data;
}
