import { httpClient } from '@/shared/api/httpClient';
import type { RegisterPropertyAdminPayload } from '@/features/identity/register/types/register.types';

export async function registerPropertyBoardAdmin(payload: RegisterPropertyAdminPayload): Promise<void> {
  await httpClient.post('/users/register-property-board-admin', payload);
}
