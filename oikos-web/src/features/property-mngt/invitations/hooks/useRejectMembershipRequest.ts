import { useMutation, useQueryClient } from '@tanstack/react-query';
import { rejectMembershipRequest } from '@/features/property-mngt/invitations/api/rejectMembershipRequest';

export function useRejectMembershipRequest(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: rejectMembershipRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['invitations', propertyId, 'membership-requests'] });
    },
  });
}
