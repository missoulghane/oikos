import { httpClient } from '@/shared/api/httpClient';
import type { RegisterUserPayload } from '@/features/identity/register/types/register.types';

export async function registerUser(payload: RegisterUserPayload): Promise<void> {
  await httpClient.post('/users/register-property-user', payload);
}
