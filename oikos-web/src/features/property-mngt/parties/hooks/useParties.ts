import { useQuery } from '@tanstack/react-query';
import { getParties } from '@/features/property-mngt/parties/api/getParties';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { PartyListFilters } from '@/features/property-mngt/parties/types/party.types';

const DEFAULT_PAGE_SIZE = 5;

export function useParties(
  propertyId: string | undefined,
  page: number,
  filters: PartyListFilters,
  size: number = DEFAULT_PAGE_SIZE,
) {
  return useQuery({
    queryKey: queryKeys.parties.list(propertyId, page, size, filters),
    queryFn: () => getParties({ propertyId: propertyId as string, page, size, ...filters }),
    enabled: Boolean(propertyId),
    placeholderData: (previousData) => previousData,
  });
}
