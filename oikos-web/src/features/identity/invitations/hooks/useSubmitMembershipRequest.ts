import { useMutation } from '@tanstack/react-query';
import { submitMembershipRequest } from '@/features/identity/invitations/api/submitMembershipRequest';

export function useSubmitMembershipRequest() {
  return useMutation({
    mutationFn: submitMembershipRequest,
  });
}
