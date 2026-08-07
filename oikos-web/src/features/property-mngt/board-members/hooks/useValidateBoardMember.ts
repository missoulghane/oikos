import { useMutation, useQueryClient } from '@tanstack/react-query';
import { validateBoardMember } from '@/features/property-mngt/board-members/api/validateBoardMember';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useValidateBoardMember(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (boardMemberId: string) => validateBoardMember({ propertyId, boardMemberId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.boardMembers.list(propertyId) });
    },
  });
}
