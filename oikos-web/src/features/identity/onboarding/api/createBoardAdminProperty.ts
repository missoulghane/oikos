import { httpClient } from '@/shared/api/httpClient';
import type {
  CreateBoardAdminPropertyPayload,
  CreatedBoardAdminProperty,
} from '@/features/identity/onboarding/types/onboarding.types';

export interface CreateBoardAdminPropertyParams {
  payload: CreateBoardAdminPropertyPayload;
  /**
   * Jeton d'onboarding renvoyé par l'étape 1. Absent quand l'utilisateur
   * reprend le wizard connecté : l'intercepteur pose alors son jeton de session,
   * que l'API accepte aussi (voir PropertyAccessEvaluator.canCreateBoardAdminProperty).
   */
  onboardingToken?: string;
}

/**
 * Crée la copropriété du compte né à l'étape 1, avec la party qui l'y relie et
 * son siège au conseil syndical (les trois sont indissociables côté API : une
 * party est portée par une copropriété). Renvoie le jeton qui autorise la
 * configuration des étapes suivantes tant que le compte n'est pas vérifié.
 */
export async function createBoardAdminProperty({
  payload,
  onboardingToken,
}: CreateBoardAdminPropertyParams): Promise<CreatedBoardAdminProperty> {
  const { data } = await httpClient.post<CreatedBoardAdminProperty>('/users/me/board-admin-property', payload, {
    headers: onboardingToken ? { Authorization: `Bearer ${onboardingToken}` } : undefined,
    // Même raison que configureProperty : pas de refresh possible sur ce jeton.
    skipSessionRefresh: true,
  });
  return data;
}
