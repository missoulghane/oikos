import { httpClient } from '@/shared/api/httpClient';
import type { ForgotPasswordPayload } from '@/features/identity/auth/types/auth.types';

/**
 * Répond 202 que l'adresse soit connue ou non : l'API refuse de dire si un compte
 * existe (RequestPasswordResetService). L'interface doit tenir la même ligne - un
 * écran qui distinguerait les deux cas rendrait la précaution inutile.
 */
export async function forgotPassword(payload: ForgotPasswordPayload): Promise<void> {
  await httpClient.post('/auth/forgot-password', payload);
}
