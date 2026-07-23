import { httpClient } from '@/shared/api/httpClient';
import type { MessageResponse, VerifyAccountPayload } from '@/features/identity/register/types/register.types';

export async function verifyAccount(payload: VerifyAccountPayload): Promise<MessageResponse> {
  const { data } = await httpClient.post<MessageResponse>('/users/verify', payload);
  return data;
}
