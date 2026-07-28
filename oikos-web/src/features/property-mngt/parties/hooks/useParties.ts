import { useQuery } from '@tanstack/react-query';
import { getParties } from '@/features/property-mngt/parties/api/getParties';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 20;

export function useParties(page: number, search?: string, size: number = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: queryKeys.parties.list(page, size, search),
    queryFn: () => getParties({ page, size, search }),
    placeholderData: (previousData) => previousData,
  });
}
