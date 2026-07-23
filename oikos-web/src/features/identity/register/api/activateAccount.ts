import { httpClient } from '@/shared/api/httpClient';
import type { ActivateAccountPayload, MessageResponse } from '@/features/identity/register/types/register.types';

export async function activateAccount(payload: ActivateAccountPayload): Promise<MessageResponse> {
  const { data } = await httpClient.post<MessageResponse>('/users/activate-account', payload);
  return data;
}
