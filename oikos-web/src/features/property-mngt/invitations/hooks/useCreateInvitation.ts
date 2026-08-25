import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createInvitation } from '@/features/property-mngt/invitations/api/createInvitation';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useCreateInvitation(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createInvitation,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.invitations.publicLink(propertyId) });
      queryClient.invalidateQueries({ queryKey: ['invitations', propertyId, 'list'] });
    },
  });
}
