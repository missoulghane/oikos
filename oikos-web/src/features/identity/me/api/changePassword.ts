import { httpClient } from '@/shared/api/httpClient';

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
}

export async function changePassword(payload: ChangePasswordPayload): Promise<void> {
  await httpClient.patch('/users/me/password', payload);
}
