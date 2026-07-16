import { httpClient } from '@/shared/api/httpClient';
import type { RegisterPropertyManagerPayload } from '@/features/register/types/register.types';

export async function registerPropertyManager(payload: RegisterPropertyManagerPayload): Promise<void> {
  await httpClient.post('/users/register-property-manager', payload);
}
