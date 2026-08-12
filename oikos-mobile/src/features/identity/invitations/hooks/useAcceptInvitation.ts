import { useMutation } from '@tanstack/react-query';
import { acceptInvitation } from '@/features/identity/invitations/api/acceptInvitation';

export function useAcceptInvitation() {
  return useMutation({
    mutationFn: acceptInvitation,
  });
}
