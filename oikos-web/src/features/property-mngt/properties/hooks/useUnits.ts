import { useQuery } from '@tanstack/react-query';
import { getUnits } from '@/features/property-mngt/properties/api/getUnits';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 20;

export function useUnits(buildingId: string, page: number, size: number = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: queryKeys.buildings.units(buildingId, page, size),
    queryFn: () => getUnits({ buildingId, page, size }),
    placeholderData: (previousData) => previousData,
  });
}
