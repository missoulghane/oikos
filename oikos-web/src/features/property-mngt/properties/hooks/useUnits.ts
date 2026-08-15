import { useQuery } from '@tanstack/react-query';
import { getUnits } from '@/features/property-mngt/properties/api/getUnits';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { UnitListFilters } from '@/features/property-mngt/properties/types/property.types';

const DEFAULT_PAGE_SIZE = 5;

export function useUnits(
  buildingId: string,
  page: number,
  filters: UnitListFilters,
  size: number = DEFAULT_PAGE_SIZE,
) {
  return useQuery({
    queryKey: queryKeys.buildings.units(buildingId, page, size, filters),
    queryFn: () => getUnits({ buildingId, page, size, ...filters }),
    enabled: Boolean(buildingId),
    placeholderData: (previousData) => previousData,
  });
}
