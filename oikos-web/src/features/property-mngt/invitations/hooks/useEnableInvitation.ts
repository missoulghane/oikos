import { useMutation, useQueryClient } from '@tanstack/react-query';
import { enableInvitation } from '@/features/property-mngt/invitations/api/enableInvitation';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useEnableInvitation(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: enableInvitation,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.invitations.publicLink(propertyId) });
      queryClient.invalidateQueries({ queryKey: ['invitations', propertyId, 'list'] });
    },
  });
}
