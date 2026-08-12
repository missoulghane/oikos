import { httpClient } from '@/shared/api/httpClient';

export async function getMyAvatar(): Promise<Blob> {
  const { data } = await httpClient.get<Blob>('/users/me/avatar', { responseType: 'blob' });
  return data;
}
