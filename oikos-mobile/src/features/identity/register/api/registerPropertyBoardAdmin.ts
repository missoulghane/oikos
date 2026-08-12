import { httpClient } from '@/shared/api/httpClient';
import type { RegisterPropertyAdminPayload } from '@/features/identity/register/types/register.types';
import type { RegisteredBoardAdmin } from '@/features/identity/onboarding/types/onboarding.types';

/**
 * Crée le compte, la copropriété et la party du syndic bénévole d'un bloc (les
 * trois sont indissociables côté API : une party est portée par une propriété).
 * Renvoie le jeton d'onboarding qui autorise la suite du wizard tant que le
 * compte n'est pas vérifié.
 */
export async function registerPropertyBoardAdmin(
  payload: RegisterPropertyAdminPayload,
): Promise<RegisteredBoardAdmin> {
  const { data } = await httpClient.post<RegisteredBoardAdmin>('/users/register-property-board-admin', payload);
  return data;
}
