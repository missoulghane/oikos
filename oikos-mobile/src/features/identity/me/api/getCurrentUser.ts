import { httpClient } from '@/shared/api/httpClient';
import type { CurrentUser } from '@/features/identity/me/types/me.types';

export async function getCurrentUser(): Promise<CurrentUser> {
  const { data } = await httpClient.get<CurrentUser>('/users/me');
  return data;
}
