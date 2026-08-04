import { useMutation, useQueryClient } from '@tanstack/react-query';
import { acceptMembershipRequest } from '@/features/property-mngt/invitations/api/acceptMembershipRequest';

export function useAcceptMembershipRequest(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: acceptMembershipRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['invitations', propertyId, 'membership-requests'] });
    },
  });
}
