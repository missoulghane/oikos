import { useMutation, useQueryClient } from '@tanstack/react-query';
import { addBoardMember } from '@/features/property-mngt/board-members/api/addBoardMember';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useAddBoardMember(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: addBoardMember,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.boardMembers.list(propertyId) });
    },
  });
}
