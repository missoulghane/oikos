import { useMutation } from '@tanstack/react-query';
import { registerPropertyBoardAdmin } from '@/features/identity/register/api/registerPropertyBoardAdmin';

export function useRegisterPropertyBoardAdmin() {
  return useMutation({
    mutationFn: registerPropertyBoardAdmin,
  });
}
