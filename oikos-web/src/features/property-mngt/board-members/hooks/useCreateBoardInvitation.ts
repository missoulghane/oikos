import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createBoardInvitation } from '@/features/property-mngt/board-members/api/createBoardInvitation';

export function useCreateBoardInvitation(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createBoardInvitation,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['invitations', propertyId, 'list'] });
    },
  });
}
