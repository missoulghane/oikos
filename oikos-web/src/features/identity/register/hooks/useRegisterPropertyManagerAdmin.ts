import { useMutation } from '@tanstack/react-query';
import { registerPropertyManagerAdmin } from '@/features/identity/register/api/registerPropertyManagerAdmin';

export function useRegisterPropertyManagerAdmin() {
  return useMutation({
    mutationFn: registerPropertyManagerAdmin,
  });
}
