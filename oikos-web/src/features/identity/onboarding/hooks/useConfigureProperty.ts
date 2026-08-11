import { useMutation } from '@tanstack/react-query';
import { configureProperty } from '@/features/identity/onboarding/api/configureProperty';

export function useConfigureProperty() {
  return useMutation({ mutationFn: configureProperty });
}
