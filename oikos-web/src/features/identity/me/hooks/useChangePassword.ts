import { useMutation } from '@tanstack/react-query';
import { changePassword } from '@/features/identity/me/api/changePassword';

export function useChangePassword() {
  return useMutation({
    mutationFn: changePassword,
  });
}
