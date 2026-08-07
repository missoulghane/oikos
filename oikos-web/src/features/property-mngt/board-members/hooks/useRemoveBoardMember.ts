import { useMutation, useQueryClient } from '@tanstack/react-query';
import { removeBoardMember } from '@/features/property-mngt/board-members/api/removeBoardMember';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useRemoveBoardMember(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (boardMemberId: string) => removeBoardMember({ propertyId, boardMemberId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.boardMembers.list(propertyId) });
    },
  });
}
