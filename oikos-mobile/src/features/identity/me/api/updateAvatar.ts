import { httpClient } from '@/shared/api/httpClient';
import type { CurrentUser } from '@/features/identity/me/types/me.types';
import type { PickedImageFile } from '@/features/identity/me/types/avatar.types';

// React Native's FormData accepts { uri, name, type } in place of a web File/Blob -
// axios forwards it as-is to the native multipart implementation.
export async function updateAvatar(file: PickedImageFile): Promise<CurrentUser> {
  const formData = new FormData();
  formData.append('file', file as unknown as Blob);
  const { data } = await httpClient.put<CurrentUser>('/users/me/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}
