import { useQuery } from '@tanstack/react-query';
import { getBuilding } from '@/features/property-mngt/properties/api/getBuilding';
import { queryKeys } from '@/shared/constants/queryKeys';

/** One building - its floor count bounds the floors a lot can be put on. */
export function useBuilding(buildingId: string) {
  return useQuery({
    queryKey: queryKeys.buildings.detail(buildingId),
    queryFn: () => getBuilding(buildingId),
    enabled: Boolean(buildingId),
  });
}
