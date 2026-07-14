import { httpClient } from '@/shared/api/httpClient';
import type { AuthTokens, LoginCredentials } from '@/features/auth/types/auth.types';

export async function login(credentials: LoginCredentials): Promise<AuthTokens> {
  const { data } = await httpClient.post<AuthTokens>('/auth/login', credentials);
  return data;
}
