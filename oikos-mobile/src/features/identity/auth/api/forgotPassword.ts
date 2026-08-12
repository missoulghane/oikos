import { httpClient } from '@/shared/api/httpClient';
import type { ForgotPasswordPayload } from '@/features/identity/auth/types/auth.types';

export async function forgotPassword(payload: ForgotPasswordPayload): Promise<void> {
  await httpClient.post('/auth/forgot-password', payload);
}
