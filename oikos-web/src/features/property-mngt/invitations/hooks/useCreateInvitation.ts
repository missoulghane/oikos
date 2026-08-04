import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createInvitation } from '@/features/property-mngt/invitations/api/createInvitation';

export function useCreateInvitation(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createInvitation,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['invitations', propertyId, 'list'] });
    },
  });
}
