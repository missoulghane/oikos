import { useMutation } from '@tanstack/react-query';
import { acceptInvitation } from '@/features/identity/register/api/acceptInvitation';

export function useAcceptInvitation() {
  return useMutation({
    mutationFn: acceptInvitation,
  });
}
