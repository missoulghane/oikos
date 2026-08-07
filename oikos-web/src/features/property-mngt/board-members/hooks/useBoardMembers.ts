import { useQuery } from '@tanstack/react-query';
import { getBoardMembers } from '@/features/property-mngt/board-members/api/getBoardMembers';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useBoardMembers(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.boardMembers.list(propertyId),
    queryFn: () => getBoardMembers(propertyId),
  });
}
