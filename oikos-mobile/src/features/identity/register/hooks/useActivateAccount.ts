import { useMutation } from '@tanstack/react-query';
import { activateAccount } from '@/features/identity/register/api/activateAccount';

export function useActivateAccount() {
  return useMutation({
    mutationFn: activateAccount,
  });
}
