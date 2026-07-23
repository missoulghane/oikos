import { useQuery } from '@tanstack/react-query';
import { getBuildings } from '@/features/property-mngt/properties/api/getBuildings';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 50;

export function useBuildings(propertyId: string, page = 0, size: number = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: queryKeys.properties.buildings(propertyId, page, size),
    queryFn: () => getBuildings({ propertyId, page, size }),
  });
}
