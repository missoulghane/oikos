import { httpClient } from '@/shared/api/httpClient';
import type {
  CreateBoardAdminPropertyPayload,
  CreatedBoardAdminProperty,
} from '@/features/identity/onboarding/types/onboarding.types';

export interface CreateBoardAdminPropertyParams {
  payload: CreateBoardAdminPropertyPayload;
  /** Jeton d'onboarding renvoyé par l'étape 1. */
  onboardingToken: string;
}

/**
 * Crée la copropriété du compte né à l'étape 1, avec la party qui l'y relie et
 * son siège au conseil syndical. Renvoie le jeton qui autorise la configuration
 * des étapes suivantes tant que le compte n'est pas vérifié.
 */
export async function createBoardAdminProperty({
  payload,
  onboardingToken,
}: CreateBoardAdminPropertyParams): Promise<CreatedBoardAdminProperty> {
  const { data } = await httpClient.post<CreatedBoardAdminProperty>('/users/me/board-admin-property', payload, {
    headers: { Authorization: `Bearer ${onboardingToken}` },
    // Même raison que configureProperty : pas de refresh possible sur ce jeton.
    skipSessionRefresh: true,
  });
  return data;
}
