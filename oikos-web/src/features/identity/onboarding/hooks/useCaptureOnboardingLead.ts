import { useMutation } from '@tanstack/react-query';
import { captureOnboardingLead } from '@/features/identity/onboarding/api/captureOnboardingLead';

/**
 * Effet de bord d'acquisition : volontairement sans `retry`, et son échec n'est
 * jamais montré au visiteur - rater une relance marketing ne justifie pas de
 * l'arrêter au milieu de son inscription.
 */
export function useCaptureOnboardingLead() {
  return useMutation({ mutationFn: captureOnboardingLead, retry: false });
}
