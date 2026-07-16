import { useMutation } from '@tanstack/react-query';
import { registerPropertyManager } from '@/features/register/api/registerPropertyManager';

export function useRegisterPropertyManager() {
  return useMutation({
    mutationFn: registerPropertyManager,
  });
}
