import { useMutation } from '@tanstack/react-query';
import { resetPassword } from '@/features/identity/auth/api/resetPassword';

export function useResetPassword() {
  return useMutation({
    mutationFn: resetPassword,
  });
}
