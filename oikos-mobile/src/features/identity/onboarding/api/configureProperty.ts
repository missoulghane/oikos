import { httpClient } from '@/shared/api/httpClient';
import type { ConfigurePropertyPayload } from '@/features/identity/onboarding/types/onboarding.types';

export interface ConfigurePropertyParams {
  propertyId: string;
  payload: ConfigurePropertyPayload;
  /**
   * Jeton d'onboarding renvoyé par l'inscription. Absent quand l'utilisateur
   * reprend le wizard connecté : l'intercepteur pose alors son jeton de session,
   * que l'API accepte aussi (voir PropertyAccessEvaluator.canConfigureOnboarding).
   */
  onboardingToken?: string;
}

export async function configureProperty({
  propertyId,
  payload,
  onboardingToken,
}: ConfigurePropertyParams): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/configuration`, payload, {
    headers: onboardingToken ? { Authorization: `Bearer ${onboardingToken}` } : undefined,
    // Pas de refresh possible sur ce jeton : sans ce drapeau une 401 anonyme
    // viderait une session par ailleurs valide au lieu de montrer l'erreur.
    skipSessionRefresh: true,
  });
}
