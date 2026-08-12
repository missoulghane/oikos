import { httpClient } from '@/shared/api/httpClient';
import type { CaptureOnboardingLeadPayload } from '@/features/identity/onboarding/types/onboarding.types';

/**
 * Étape 1 du wizard : l'adresse est enregistrée avant même qu'un compte puisse
 * exister (il n'est créé qu'à la fin de l'étape 2), pour pouvoir relancer un
 * visiteur qui abandonne en cours de route.
 */
export async function captureOnboardingLead(payload: CaptureOnboardingLeadPayload): Promise<void> {
  await httpClient.post('/users/onboarding-leads', payload);
}
