import { httpClient } from '@/shared/api/httpClient';
import type { RegisterBoardAdminPayload } from '@/features/identity/register/types/register.types';
import type { RegisteredBoardAdmin } from '@/features/identity/onboarding/types/onboarding.types';

/**
 * Étape 1 du wizard : crée le compte du syndic bénévole, seul - sa copropriété
 * vient à l'étape 2. Renvoie le jeton d'onboarding qui autorise l'étape 2 tant
 * que le compte n'est pas vérifié.
 */
export async function registerPropertyBoardAdmin(payload: RegisterBoardAdminPayload): Promise<RegisteredBoardAdmin> {
  const { data } = await httpClient.post<RegisteredBoardAdmin>('/users/register-property-board-admin', payload);
  return data;
}
