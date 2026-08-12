import { httpClient } from '@/shared/api/httpClient';
import type { CurrentUser } from '@/features/identity/me/types/me.types';

export async function updateAvatar(file: File): Promise<CurrentUser> {
  const formData = new FormData();
  formData.append('file', file);
  const { data } = await httpClient.put<CurrentUser>('/users/me/avatar', formData);
  return data;
}
