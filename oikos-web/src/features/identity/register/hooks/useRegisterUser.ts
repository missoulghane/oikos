import { useMutation } from '@tanstack/react-query';
import { registerUser } from '@/features/identity/register/api/registerUser';

export function useRegisterUser() {
  return useMutation({
    mutationFn: registerUser,
  });
}
