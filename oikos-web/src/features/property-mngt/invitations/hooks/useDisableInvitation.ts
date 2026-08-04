import { useMutation, useQueryClient } from '@tanstack/react-query';
import { disableInvitation } from '@/features/property-mngt/invitations/api/disableInvitation';

export function useDisableInvitation(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: disableInvitation,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['invitations', propertyId, 'list'] });
    },
  });
}
