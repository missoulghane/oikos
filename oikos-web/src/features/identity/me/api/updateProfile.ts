import { httpClient } from '@/shared/api/httpClient';
import type { CurrentUser } from '@/features/identity/me/types/me.types';
import type { UpdateProfileFormValues } from '@/features/identity/me/schemas/updateProfileSchema';

export async function updateProfile(payload: UpdateProfileFormValues): Promise<CurrentUser> {
  const { data } = await httpClient.patch<CurrentUser>('/users/me/profile', payload);
  return data;
}
