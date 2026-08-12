import { httpClient } from '@/shared/api/httpClient';
import type { ResetPasswordPayload } from '@/features/identity/auth/types/auth.types';

export async function resetPassword(payload: ResetPasswordPayload): Promise<void> {
  await httpClient.post('/auth/reset-password', payload);
}
