import { useQuery } from '@tanstack/react-query';
import { getUnits } from '@/features/property-mngt/properties/api/getUnits';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { OwnershipStatus } from '@/features/property-mngt/properties/types/property.types';

const DEFAULT_PAGE_SIZE = 5;

export function useUnits(
  buildingId: string,
  page: number,
  search?: string,
  ownershipStatus?: OwnershipStatus,
  size: number = DEFAULT_PAGE_SIZE,
) {
  return useQuery({
    queryKey: queryKeys.buildings.units(buildingId, page, size, search, ownershipStatus),
    queryFn: () => getUnits({ buildingId, page, size, search, ownershipStatus }),
    enabled: Boolean(buildingId),
    placeholderData: (previousData) => previousData,
  });
}
